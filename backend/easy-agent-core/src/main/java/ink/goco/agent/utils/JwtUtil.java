package ink.goco.agent.utils;

import cn.hutool.jwt.JWT;
import ink.goco.agent.constant.SystemConfig;
import ink.goco.agent.entity.User;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {
    /**
     * 密钥
     */
    private static final byte[] KEY = SystemConfig.JWT_KEY.getBytes();

    /**
     * 过期时间（秒）：7 天
     */
    public static final long EXPIRE = SystemConfig.JWT_EXPIRE;

    private JwtUtil() {
    }

    /**
     * 根据用户信息生成token
     * @return token
     */
    public static String generateTokenForUser(Long id, String nickname) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("nickname", nickname);
        return generateToken(map);
    }

    /**
     * 根据 map 生成 token 默认：HS265(HmacSHA256)算法
     *
     * @param map    携带数据
     * @return token
     */
    public static String generateToken(Map<String, Object> map) {
        JWT jwt = JWT.create();
        // 设置携带数据
        map.forEach(jwt::setPayload);
        // 设置密钥
        jwt.setKey(KEY);
        // 设置过期时间
        jwt.setExpiresAt(new Date(System.currentTimeMillis() + EXPIRE * 1000));
        return jwt.sign();
    }

    /**
     * token 校验
     * @param token token
     * @return 是否通过校验
     */
    public static boolean verify (String token) {
        if (StringUtils.isBlank(token)) return false;
        return JWT.of(token).setKey(KEY).verify();
    }

    /**
     * token 校验，并获取 userDto
     * @param token token
     * @return userDto
     */
    public static User verifyAndGetUser(String token) {
        if(!verify(token)) return null;
        // 解析数据
        JWT jwt = JWT.of(token);
        Long id = Long.valueOf(jwt.getPayload("id").toString());
        String nickname = jwt.getPayload("nickname").toString();
        // 返回用户信息
        return new User(id, nickname);
    }
}
