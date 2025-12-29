package ink.goco.agent.controller;

import ink.goco.agent.dto.request.UserLoginRequest;
import ink.goco.agent.entity.User;
import ink.goco.agent.service.UserService;
import ink.goco.agent.utils.GResult;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/user")
@RestController
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/login")
    public GResult login(@RequestBody UserLoginRequest request) {
        if (request.getPhone() == null || request.getPassword() == null) {
            return GResult.errorMsg("手机号或密码不能为空");
        }

        User login = userService.login(request.getPhone(), request.getPassword());

        return GResult.ok(login);
    }
}
