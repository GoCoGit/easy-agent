package ink.goco.agent.service.impl;

import cn.hutool.json.JSONUtil;
import ink.goco.agent.constant.MyPrompt;
import ink.goco.agent.dto.SearXNGResponse;
import ink.goco.agent.dto.SearchResult;
import ink.goco.agent.service.ChatService;
import ink.goco.agent.utils.SSEServer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {
    @Resource
    private OkHttpClient okHttpClient;

    @Value("${searxng.url}")
    private String SEARXNG_URL;
    @Value("${searxng.count}")
    private int SEARXNG_COUNT;

    private final ChatClient client;

    @Resource
    private final RedisTemplate<String, Object> redisTemplate;

    private final RedisVectorStore redisVectorStore;

    public ChatServiceImpl(ChatClient.Builder chatClientBuilder,
                           ToolCallbackProvider tools,
                           RedisTemplate<String, Object> redisTemplate,
                           RedisVectorStore redisVectorStore) {
        this.client = chatClientBuilder
                .defaultToolCallbacks(tools)
                .defaultSystem(MyPrompt.SYSTEM_PROMPT)
                .build();
        this.redisTemplate = redisTemplate;
        this.redisVectorStore = redisVectorStore;
    }

    @Override
    public void common(String msg) {
        Flux<String> stringFlux = client.prompt(msg).stream().content();
        stringFlux.toStream().forEach(s -> {
            SSEServer.send("123", s, "message");
        });
    }

    @Override
    public void rag(String msg) {
        List<Document> documents = redisVectorStore.similaritySearch(msg);

        String context = documents.stream().map(Document::getText).collect(Collectors.joining("\n"));

        Prompt prompt = new Prompt(MyPrompt.RAG_PROMPT.replace("{context}", context).replace("{question}", msg));

        Flux<String> stringFlux = client.prompt(prompt).stream().content();
        stringFlux.toStream().forEach(s -> {
            SSEServer.send("123", s, "message");
        });
    }

    @Override
    public void web(String msg) throws IOException {
        SSEServer.send("123", "正在搜索中...", "message");

        // 先判断缓存中是否已有联网搜索的结果
        String cacheKey = "web_" + msg;
        Object cacheValue = redisTemplate.opsForValue().get(cacheKey);

        if (cacheValue != null) {
            log.info("{} 联网搜索结果缓存命中", msg);

            Prompt prompt = new Prompt(MyPrompt.SEARXNG_PROMPT.replace("{context}", cacheValue.toString()).replace("{question}", msg));

            Flux<String> stringFlux = client.prompt(prompt).stream().content();
            stringFlux.toStream().forEach(s -> {
                SSEServer.send("123", s, "message");
            });
        } else {
            HttpUrl httpUrl = HttpUrl.get(SEARXNG_URL)
                    .newBuilder()
                    .addQueryParameter("q", msg)
                    .addQueryParameter("format", "json")
                    .build();

            log.info("httpUrl: {}", httpUrl);

            Request request = new Request.Builder()
                    .url(httpUrl)
                    .get()
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("联网搜索失败");
                }

                if (response.body() != null) {
                    String responseBody = response.body().string();
                    SearXNGResponse bean = JSONUtil.toBean(responseBody, SearXNGResponse.class);

                    List<SearchResult> results = bean.getResults();
                    if (!results.isEmpty()) {
                        Prompt prompt = getPrompt(msg, results, cacheKey);

                        Flux<String> stringFlux = client.prompt(prompt).stream().content();
                        stringFlux.toStream().forEach(s -> {
                            SSEServer.send("123", s, "message");
                        });
                    } else {
                        SSEServer.send("123", "没有搜索到结果", "message");
                    }
                } else {
                    throw new RuntimeException("联网搜索失败");
                }
            }
        }
    }

    private Prompt getPrompt(String msg, List<SearchResult> results, String cacheKey) {
        StringBuilder context = new StringBuilder();

        results.forEach(searchResult -> {
            context.append(String.format("<context>\n[来源] %s \n [摘要] %s \n </context>\n",
                    searchResult.getUrl(),
                    searchResult.getContent()));
        });

        redisTemplate.opsForValue().set(cacheKey, context);

        return new Prompt(MyPrompt.SEARXNG_PROMPT.replace("{context}", context).replace("{question}", msg));
    }

    private List<SearchResult> dealResult(List<SearchResult> results) {
        return results.subList(0, Math.min(results.size(), SEARXNG_COUNT))
                .parallelStream()
                .sorted(Comparator.comparingDouble(SearchResult::getScore))
                .limit(SEARXNG_COUNT).toList();
    }
}
