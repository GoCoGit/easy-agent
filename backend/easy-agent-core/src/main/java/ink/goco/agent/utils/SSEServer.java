package ink.goco.agent.utils;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
public class SSEServer {

    private static final Map<String, SseEmitter> sseClients = new ConcurrentHashMap<>();

    public static SseEmitter connect(String userId) {
        SseEmitter sseEmitter = new SseEmitter(0L);
        sseEmitter.onCompletion(completionCallback(userId));
        sseEmitter.onTimeout(timeoutCallback(userId));
        sseEmitter.onError(errorCallback(userId));

        sseClients.put(userId, sseEmitter);
        log.info("add sse userId: {}", userId);

        return sseEmitter;
    }

    public static Runnable timeoutCallback(String userId) {
        return () -> {
            remove(userId);
        };
    }

    public static Runnable completionCallback(String userId) {
        return () -> {
            remove(userId);
        };
    }

    public static Consumer<Throwable> errorCallback(String userId) {
         return throwable -> {
            remove(userId);
        };
    }

    public static void remove(String userId) {
        sseClients.remove(userId);
        log.info("remove sse userId: {}", userId);
    }

    public static void send(String userId, String message, String messageType) {
        if (CollectionUtils.isEmpty(sseClients)) {
            return;
        }

        if (sseClients.containsKey(userId)) {
            sendEmitterMessage(sseClients.get(userId), userId, message, messageType);
        }
    }

    public static void sendEmitterMessage(SseEmitter sseEmitter,
                                          String userId,
                                          String message,
                                          String messageType) {

        try {
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .id(userId)
                    .data(" " + message)
                    .name(messageType);
            sseEmitter.send(event);
        } catch (Exception e) {
            log.error("send message error: {}", e.getMessage());
            remove(userId);
        }
    }

}
