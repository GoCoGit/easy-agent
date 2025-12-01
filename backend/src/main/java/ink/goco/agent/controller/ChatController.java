package ink.goco.agent.controller;

import cn.hutool.json.JSONUtil;
import ink.goco.agent.constant.MyPrompt;
import ink.goco.agent.entity.SearXNGResponse;
import ink.goco.agent.entity.SearchResult;
import ink.goco.agent.service.ChatService;
import ink.goco.agent.utils.GResult;
import ink.goco.agent.utils.SSEServer;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
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
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("chat")
public class ChatController {

    @Resource
    private ChatService chatService;

    @GetMapping("/common")
    public GResult common(@RequestParam String msg, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");

        if (msg.isEmpty()) {
            return GResult.errorMsg("请输入内容");
        }

        chatService.common(msg);
        return GResult.ok();
    }

    @GetMapping("/rag")
    public GResult rag(@RequestParam String msg) {
        if (msg.isEmpty()) {
            return GResult.errorMsg("请输入内容");
        }

        chatService.rag(msg);
        return GResult.ok();
    }



    @GetMapping("/web")
    public GResult web(@RequestParam String msg) {
        if (msg.isEmpty()) {
            return GResult.errorMsg("请输入内容");
        }

        try {
            chatService.web(msg);
            return GResult.ok();
        } catch (IOException e) {
            return GResult.errorMsg("搜索引擎网络错误");
        }
    }


}
