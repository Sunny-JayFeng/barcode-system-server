package jayfeng.barcode.interceptor;

import jayfeng.barcode.bean.User;
import jayfeng.barcode.service.impl.UserServiceImpl;
import org.apache.log4j.helpers.ThreadLocalMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 所有请求的拦截，放通登录注册请求
 * 用于判断是否已经登录
 * @author JayFeng
 * @date 2021/10/24
 */
@Component
public class AllRequestInterceptor implements HandlerInterceptor {

    @Autowired
    private UserServiceImpl userServiceImpl;

    /**
     * 未登录拦截
     * @param request 获取当前已登录的用户
     * @param response 用于未登录重定向
     * @param handler 处理
     * @return 返回是否放行
     * @throws IOException
     */
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws IOException {
        User user = userServiceImpl.getNowLoginUser(request);
        if (user == null) {
            response.sendRedirect("/barcode/login.html");
        } else {
            userServiceImpl.addUserThreadLocal(user);
        }
        return user != null;
    }

}
