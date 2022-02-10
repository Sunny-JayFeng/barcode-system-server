package jayfeng.barcode.util;

import jayfeng.barcode.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * cookie 管理
 * @author JayFeng
 * @date 2021/10/14
 */
@Component
@Slf4j
public class CookieManagement {

    @Autowired
    private RedisService redisService;

    public static final String USER_KEY = "useruuid";
    public static final String DO_MAIN = "127.0.0.1";
    public static final String PATH = "/";
    public static final Integer MAX_AGE = 3600 * 24; // cookie 在 24 小时后过期，后端有定时任务夜间清除redis中的session

    /**
     * 创建一个 cookie
     * @param key cookie 的 key
     * @param value cookie 的值
     * @return 返回
     */
    public Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(MAX_AGE);
        cookie.setPath(PATH);
        cookie.setDomain(DO_MAIN);
        return cookie;
    }

    /**
     * 将当前登录的用户信息存到 redis
     * @param cookieValue cookie value
     * @param redisMapKey redisMap key
     * @param nowLoginUserJson 用户信息JSON
     */
    public void setLoginUser(String cookieValue, String redisMapKey, String nowLoginUserJson) {
        redisService.setUserJSON(redisMapKey, cookieValue, nowLoginUserJson);
    }

    /**
     * 获取当前用户已登录的 cookie
     * @param request request
     * @param cookieKey cookie key
     * @param redisMapKey redisMap key
     * @return 返回 cookie
     */
    public Object getLoginUser(HttpServletRequest request, String cookieKey, String redisMapKey) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length != 0) {
            for (Cookie cookie : cookies) {
                String key = cookie.getName();
                if (cookieKey.equals(key)) {
                    return redisService.getUserJSON(redisMapKey, cookie.getValue());
                }
            }
        }
        return null;
    }

    /**
     * 删除一个 cookie
     * @param cookies 所有 cookie
     * @param key cookie 的 key
     * @return 返回
     */
    public String removeCookie(HttpServletResponse response, Cookie[] cookies, String key) {
        if (cookies != null && cookies.length != 0) {
            for (Cookie cookie : cookies) {
                if (key.equals(cookie.getName())) {
                    log.info("removeCookie, key: {}", key);
                    cookie.setMaxAge(0);
                    // 必须设置相同的 path 和 domain， setMaxAge 才有效
                    cookie.setPath(PATH);
                    cookie.setDomain(DO_MAIN);
                    response.addCookie(cookie);
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 给请求添加一个 cookie
     * @param response 响应，设置 cookie
     * @param key cookie 的 key
     * @param value cookie 的值
     */
    public void setCookie(HttpServletResponse response, String key, String value) {
        Cookie cookie = createCookie(key, value);
        setCookie(response, cookie);
    }

    /**
     * 给请求添加一个 cookie
     * @param response 响应，设置 cookie
     * @param cookie cookie
     */
    public void setCookie(HttpServletResponse response, Cookie cookie) {
        log.info("setCookie, key: {}, value: {}", cookie.getName(), cookie.getValue());
        response.addCookie(cookie);
    }

}
