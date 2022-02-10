package jayfeng.barcode.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jayfeng.barcode.bean.User;
import jayfeng.barcode.response.ResponseData;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户逻辑层
 * @author JayFeng
 * @date 2021/10/14
 */
@Service
public interface UserService {


    /**
     * 分页查询系统用户数据
     * @param requestParams 请求参数
     * @param page 分页参数
     * @return 返回
     */
    ResponseData findUserPage(Map<String, String> requestParams, Page<User> page);

    /**
     * 根据角色查询用户信息
     * @param role 角色
     * @return 返回
     */
    ResponseData findUserByRole(Byte role);

    /**
     * 用户登录
     * @param response 用于设置 cookie
     * @param loginUser 登录用户
     * @return 返回
     */
    ResponseData login(HttpServletResponse response, User loginUser);

    /**
     * 获取当前已登录用户
     * @param request 用于获取已登录用户
     * @return 返回用户
     */
    User getNowLoginUser(HttpServletRequest request);

    /**
     * 获取当前已登录的用户
     * @return 返回用户
     */
    User getNowLoginUser();

    /**
     * 用户注册
     * @param user 新用户信息
     * @param password 已登录的用户的密码
     * @return 返回
     */
    ResponseData registry(User user, String password);

    /**
     * 更新用户角色
     * @param user 新用户信息
     * @param password 已登录的用户的密码
     * @return 返回
     */
    ResponseData updateUserRole(User user, String password);

    /**
     * 删除用户
     * @param user 新用户信息
     * @param password 已登录的用户的密码
     * @return 返回
     */
    ResponseData deleteUser(User user, String password);

}
