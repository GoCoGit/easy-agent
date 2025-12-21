package ink.goco.agent.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

@Slf4j
@Order(1)
@Component
public class PrefixRedisSerializer extends StringRedisSerializer {
    @Value("${spring.data.redis.key-prefix:EA:}")
    private String KEY_PREFIX;

    /**
     * 序列化
     *
     * @param s key
     * @return 结果
     */
    @Override
    public byte[] serialize(String s) {
        if (s == null) {
            return new byte[0];
        }
        // 这里加上你需要加上的key前缀
        String realKey = KEY_PREFIX + s;
        return super.serialize(realKey);
    }

    /**
     * 反序列化
     *
     * @param bytes 数据
     * @return 结果
     */
    @Override
    public String deserialize(byte[] bytes) {
        String s = bytes == null ? null : new String(bytes);
        if (StringUtils.isBlank(s)) {
            return s;
        }
        int index = s.indexOf(KEY_PREFIX);
        if (index != -1) {
            return s.substring(KEY_PREFIX.length());
        }
        return s;
    }
}
