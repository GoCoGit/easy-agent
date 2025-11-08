package ink.goco.agent.controller;

import ink.goco.agent.utils.SSEServer;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
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

    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.client = chatClientBuilder.defaultSystem("Your name is WangHuaHua").build();
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
}
