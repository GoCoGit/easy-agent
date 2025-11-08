package ink.goco.agent.controller;

import ink.goco.agent.utils.LeeResult;
import ink.goco.agent.utils.SSEServer;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class ChatController {

    private final ChatClient client;
    private final RedisVectorStore redisVectorStore;

    public ChatController(ChatClient.Builder chatClientBuilder, RedisVectorStore redisVectorStore) {
        this.client = chatClientBuilder.defaultSystem("Your name is WangHuaHua").build();
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
            log.info(s);
            SSEServer.send("123", s, "message");
        }).toList();
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
}
