package ink.goco.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DateTool {

    @Tool(description = "获取当前时间")
    public String getCurrentTime() {
        log.info("======= 调用MCP工具 getCurrentTime ========");
        return java.time.LocalDateTime.now().toString();
    }
}
