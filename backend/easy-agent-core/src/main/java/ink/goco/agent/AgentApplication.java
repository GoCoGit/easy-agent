package ink.goco.agent;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class AgentApplication {

	public static void main(String[] args) {

        Dotenv dotenv = Dotenv.load();
        dotenv.entries().forEach(dotenvEntry -> {
            log.info("[环境变量注册] key:{} value: {}", dotenvEntry.getKey(), dotenvEntry.getValue());
            System.setProperty(dotenvEntry.getKey(), dotenvEntry.getValue());
        });

		SpringApplication.run(AgentApplication.class, args);
	}

}