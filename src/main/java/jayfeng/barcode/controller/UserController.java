package jayfeng.barcode.controller;

import jayfeng.barcode.bean.User;
import jayfeng.barcode.response.ResponseMessage;
import jayfeng.barcode.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 用户控制层
 * @author JayFeng
 * @date 2021/10/14
 */
@Slf4j
@RestController
@RequestMapping("/barcode/user")
public class UserController extends BaseController {

    @Autowired
    UserService userService;

    /**
     * 分页查询系统用户数据
     * @param requestParams 请求参数
     * @return 返回
     */
    @GetMapping("/findUserPage")
    public ResponseMessage findUserPage(@RequestParam Map<String, String> requestParams) {
        log.info("findUserPage 分页查询系统用户信息 requestParams: {}", requestParams);
        return requestSuccess(userService.findUserPage(requestParams, getPage(requestParams)));
    }

    /**
     * 根据角色查询用户信息
     * @param role 角色
     * @return 返回
     */
    @GetMapping("/findUserByRole/{role}")
    public ResponseMessage findUserByRole(@PathVariable("role") Byte role) {
        log.info("findUserByRole 根据角色查询用户信息 role: {}", role);
        return requestSuccess(userService.findUserByRole(role));
    }

    /**
     * 用户登录
     * @param response 用于设置 cookie
     * @param loginUser 登录用户
     * @return 返回
     */
    @PostMapping("/login")
    public ResponseMessage login(HttpServletResponse response,
                          @RequestBody User loginUser) {
        log.info("login 用户登录 loginUser: {}", loginUser);
        return requestSuccess(userService.login(response, loginUser));
    }

    /**
     * 用户注册
     * @param user 新用户信息
     * @param password 已登录的用户的密码
     * @return 返回
     */
    @PostMapping("/registry/{password}")
    public ResponseMessage registry(@RequestBody User user,
                                    @PathVariable("password") String password) {
        log.info("registry 用户注册 newUser: {}", user);
        return requestSuccess(userService.registry(user, password));
    }

    /**
     * 更新用户角色
     * @param user 新用户信息
     * @param password 已登录的用户的密码
     * @return 返回
     */
    @PutMapping("/updateUserRole/{password}")
    public ResponseMessage updateUserRole(@RequestBody User user,
                                          @PathVariable("password") String password) {
        log.info("updateUserRole 修改用户角色信息 newUser: {}", user);
        return requestSuccess(userService.updateUserRole(user, password));
    }

    /**
     * 删除用户
     * @param user 新用户信息
     * @param password 已登录的用户的密码
     * @return 返回
     */
    @DeleteMapping("/deleteUser/{password}")
    public ResponseMessage deleteUser(@RequestBody User user,
                                      @PathVariable("password") String password) {
        log.info("deleteUser 删除用户信息 user: {}", user);
        return requestSuccess(userService.deleteUser(user, password));
    }

}
