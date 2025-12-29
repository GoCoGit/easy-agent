package ink.goco.agent.service.impl;

import ink.goco.agent.constant.SystemConfig;
import ink.goco.agent.entity.User;
import ink.goco.agent.repository.UserRepository;
import ink.goco.agent.service.UserService;
import ink.goco.agent.utils.JwtUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Resource
    private UserRepository userRepository;

    @Override
    public String login(String phone, String password) {
        // 1. 根据field，查询username、email、phone字段是否存在
        User user = userRepository.findByPhone(phone)
                .orElse(null);

        if (user != null) {
            // 2. 如存在，查询该用户的password_salt字段值，拼接password_salt和password，进行md5加密
            String passwordSalt = user.getPasswordSalt();
            String encryptedPassword = encryptPassword(password, passwordSalt);

            // 3. 验证密码是否一致,一致则返回用户信息，不一致则抛出异常（密码错误）
            if (encryptedPassword.equals(user.getPassword())) {
                return JwtUtil.generateTokenForUser(user.getId(), user.getNickname());
            } else {
                throw new RuntimeException("用户名或密码错误");
            }
        } else {
            // 4. 如不存在，则新建用户，随机生成6位密码盐，默认密码：123456，密码进行md5加密，保存用户信息
            String passwordSalt = generateRandomSalt();
            String defaultEncryptedPassword = encryptPassword(SystemConfig.DEFAULT_USER_PASSWORD, passwordSalt);

            User newUser = User.builder()
                    .nickname(SystemConfig.DEFAULT_USER_NICKNAME_PREFIX + phone)
                    .phone(phone)
                    .password(defaultEncryptedPassword)
                    .passwordSalt(passwordSalt)
                    .build();

            user = userRepository.save(newUser);
            // 5. 注册成功，自动登录返回jwt令牌
            return JwtUtil.generateTokenForUser(user.getId(), user.getNickname());
        }
    }

    /**
     * 使用密码盐对密码进行加密
     */
    private String encryptPassword(String password, String salt) {
        // 实现MD5加密逻辑，这里需要根据你的具体加密方式实现
        // 示例：return MD5Util.md5(password + salt);
        return org.apache.commons.codec.digest.DigestUtils.md5Hex(password + salt);
    }

    /**
     * 生成随机密码盐
     */
    private String generateRandomSalt() {
        return java.util.UUID.randomUUID().toString().substring(0, 6);
    }

}
