package ink.goco.agent;

import ink.goco.agent.entity.User;
import ink.goco.agent.repository.UserRepository;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
@Slf4j
public class CrudTests {

    @Autowired
    private UserRepository userRepository;

    @BeforeAll
    static void setUp() {
        Dotenv dotenv = Dotenv.load();
        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue()));
    }

    @Test
    void crud_test() {
// ---------- Create ----------
        User user = User.builder()
                .password("abc123")
                .build();

        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();

        // ---------- Read ----------
        Optional<User> foundOpt = userRepository.findById(saved.getId());
        assertThat(foundOpt).isPresent();

        User found = foundOpt.get();
        log.info("EA-DEBUG {}", found);
        assertThat(found.getPassword()).isEqualTo("abc123");

        // ---------- Update ----------
        found.setPassword("abc456");
        User updated = userRepository.save(found);

        assertThat(updated.getPassword()).isEqualTo("abc456");

        // ---------- Delete ----------
        userRepository.deleteById(updated.getId());

        Optional<User> deleted = userRepository.findById(updated.getId());
        assertThat(deleted).isEmpty();
    }

}
