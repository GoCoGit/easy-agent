package ink.goco.agent.controller;

import ink.goco.agent.service.ChatService;
import ink.goco.agent.utils.GResult;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

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
