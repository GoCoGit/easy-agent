package ink.goco;

import ink.goco.tool.DateTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@Slf4j
public class McpServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpServerApplication.class, args);
	}

    @Bean
    public ToolCallbackProvider registerMCPTools(DateTool dateTool) {
        return MethodToolCallbackProvider.builder().toolObjects(dateTool).build();
    }
}