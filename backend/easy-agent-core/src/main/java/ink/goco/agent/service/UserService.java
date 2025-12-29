package ink.goco.agent.service;

import ink.goco.agent.entity.User;

public interface UserService {

    /**
     * 登录
     * @param field 登录字段，可选：手机号、邮箱、用户名
     * @param password 密码
     * @return 用户信息
     */
    User login(String field, String password);
}
