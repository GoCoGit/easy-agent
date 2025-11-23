package ink.goco.agent.controller;

import ai.djl.util.JsonUtils;
import cn.hutool.json.JSONUtil;
import ink.goco.agent.entity.SearXNGResponse;
import ink.goco.agent.entity.SearchResult;
import ink.goco.agent.utils.LeeResult;
import ink.goco.agent.utils.SSEServer;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bouncycastle.cert.ocsp.Req;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class ChatController {

    private final ChatClient client;
    private final RedisVectorStore redisVectorStore;

    public ChatController(ChatClient.Builder chatClientBuilder, RedisVectorStore redisVectorStore) {
        this.client = chatClientBuilder.defaultSystem("Your name is WangHuaHua, The King of Code!").build();
        this.redisVectorStore = redisVectorStore;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String msg) {
        return client.prompt(msg).call().content();
    }

    @GetMapping("/chat2")
    public Flux<ChatResponse> chat2(@RequestParam String msg) {
        return client.prompt(msg).stream().chatResponse();
    }

    @GetMapping("/chat3")
    public void chat3(@RequestParam String msg, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        Flux<String> stringFlux = client.prompt(msg).stream().content();
        List<String> list = stringFlux.toStream().peek(s -> {
            SSEServer.send("123", s, "message");
        }).toList();

        String fullContent = String.join("", list);
        log.info(fullContent);
    }

    private static final String RAG_PROMPT = """
            基于上下文的知识库回答问题：
            【上下文】
            {context}
            
            【问题】
            {question}
            
            【输出】
            如果没有查到，请回复：不知道。
            如果查到，请回复具体的内容。不相关的近似内容不必提到。
            """;

    @GetMapping("/search")
    public LeeResult search(@RequestParam String query) {
        List<Document> documents = redisVectorStore.similaritySearch(query);

        System.out.println("documents");
        System.out.println(documents);

        String context = documents.stream().map(Document::getText).collect(Collectors.joining("\n"));

        Prompt prompt = new Prompt(RAG_PROMPT.replace("{context}", context).replace("{question}", query));

        Flux<String> stringFlux = client.prompt(prompt).stream().content();
        List<String> list = stringFlux.toStream().peek(s -> {
            SSEServer.send("123", s, "message");
        }).toList();

        return LeeResult.ok();
    }

    @Resource
    private OkHttpClient okHttpClient;

    @Value("${searxng.url}")
    private String SEARXNG_URL;
    @Value("${searxng.count}")
    private int SEARXNG_COUNT;

    private static final String SEARXNG_PROMPT = """
            你是一个互联网搜索大师，请基于以下互联网返回的结果作为上下文，根据你的理解结合用户的提问综合后，生成并且输出专业的回答：
            【上下文】
            {context}
            
            【问题】
            {question}
            
            【输出】
            如果没有查到，请回复：不知道。
            如果查到，请回复具体的内容。
            """;

    @GetMapping("/web_search")
    public LeeResult webSearch(@RequestParam String query) {
        HttpUrl httpUrl = HttpUrl.get(SEARXNG_URL)
                .newBuilder()
                .addQueryParameter("q", query)
                .addQueryParameter("format", "json")
                .build();

        log.info("httpUrl: {}", httpUrl);

        Request request = new Request.Builder()
                .url(httpUrl)
                .get()
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("请求失败");
            }

            if (response.body() != null) {
                String responseBody = response.body().string();
                SearXNGResponse bean = JSONUtil.toBean(responseBody, SearXNGResponse.class);

                List<SearchResult> results = bean.getResults();

                StringBuilder context = new StringBuilder();

                results.forEach(searchResult -> {
                    context.append(String.format("<context>\n[来源] %s \n [摘要] %s \n </context>\n",
                            searchResult.getUrl(),
                            searchResult.getContent()));
                });

                Prompt prompt = new Prompt(SEARXNG_PROMPT.replace("{context}", context).replace("{question}", query));

                Flux<String> stringFlux = client.prompt(prompt).stream().content();
                List<String> list = stringFlux.toStream().peek(s -> {
                    SSEServer.send("123", s, "message");
                }).toList();

                return LeeResult.ok();
            }
            return LeeResult.errorMsg("搜索失败");
        } catch (IOException e) {
            return LeeResult.errorException("请求失败");
        }
    }

    private List<SearchResult> dealResult(List<SearchResult> results) {
        return results.subList(0, Math.min(results.size(), SEARXNG_COUNT))
                .parallelStream()
                .sorted(Comparator.comparingDouble(SearchResult::getScore))
                .limit(SEARXNG_COUNT).toList();
    }
}
