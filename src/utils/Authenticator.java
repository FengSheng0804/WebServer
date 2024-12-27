package utils;

import java.util.Map;
import java.util.Objects;
import java.util.HashMap;

/**
 * 认证器类，用于验证用户名和密码。
 */
public class Authenticator {
    /**
     * 存储用户名和密码的映射。
     */
    private static final Map<String, String> users = new HashMap<>();

    // 这里可能要改
    static {
        users.put("admin", "admin");
        users.put("user", "user");
    }

    /**
     * 验证用户名和密码是否匹配。
     *
     * @param username 用户名
     * @param password 密码
     * @return 如果用户名和密码匹配，返回true；否则返回false
     */
    public static boolean authenticate(String username, String password) {
        return Objects.equals(users.get(username), password);
    }
}
