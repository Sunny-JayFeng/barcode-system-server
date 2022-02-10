package jayfeng.barcode.constant;

/**
 * Redis 常量
 * @author JayFeng
 * @date 2021/10/14
 */
public enum RedisConstant {

    USER_UUID_MAP("userUUIDMap"),

    IDENTIFY_TIMEOUT(60 * 10L), // 验证码的过期时间

    ACCOUNT_SAFE_TIMEOUT(24 * 3600L); // 次数超过时，限制 24 小时

    private String message;

    private Long timeout;

    RedisConstant(String message) {
        this.message = message;
    }
    RedisConstant(Long timeout) {
        this.timeout = timeout;
    }

    public String getRedisMapKey() {
        return this.message;
    }

    public String getValue() {
        return this.message;
    }

    public Long getTimeout() {
        return this.timeout;
    }

}
