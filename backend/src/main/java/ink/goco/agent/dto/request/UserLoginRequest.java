package ink.goco.agent.dto.request;

import lombok.Data;

@Data
public class UserLoginRequest {
    private String phone;
    private String password;
}
