package ink.goco.agent.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nickname;
    @Column(nullable = false, unique = true)
    private String phone;
    private String avatar;
    private String email;
    @Column(nullable = false)
    private String password;
    private String passwordSalt;
    @Column(nullable = false)
    private LocalDateTime createdAt;

    public User(Long id, String nickname) {
        this.id = id;
        this.nickname = nickname;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
