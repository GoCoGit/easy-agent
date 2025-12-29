package ink.goco.agent;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class AgentApplicationTests {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeAll
    static void setUp() {
        Dotenv dotenv = Dotenv.load();
        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue()));
    }

	@Test
	void contextLoads() {

    }

    @Test
    void redisTest() {
        redisTemplate.opsForValue().set("test", "test123");
        Object test = redisTemplate.opsForValue().get("test");

        System.out.println(test);
    }

    @Test
    void dbTest() {

    }
}
