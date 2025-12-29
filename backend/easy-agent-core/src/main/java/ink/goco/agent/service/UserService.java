package ink.goco.agent.service;

import ink.goco.agent.entity.User;

public interface UserService {

    /**
     * 登录
     * @param field 登录字段，可选：手机号、邮箱、用户名
     * @param password 密码
     * @return jwt令牌
     */
    String login(String field, String password);
}
