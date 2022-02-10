package jayfeng.barcode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import jayfeng.barcode.bean.Stock;
import jayfeng.barcode.bean.User;
import jayfeng.barcode.constant.RedisConstant;
import jayfeng.barcode.constant.ResponseFailMessageConstant;
import jayfeng.barcode.constant.ResponseFailTypeConstant;
import jayfeng.barcode.dao.UserDao;
import jayfeng.barcode.handler.QueryConditionHandler;
import jayfeng.barcode.response.ResponseData;
import jayfeng.barcode.service.UserService;
import jayfeng.barcode.util.CookieManagement;
import jayfeng.barcode.util.EncryptUtil;
import jayfeng.barcode.util.UserPermissionCheck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 用户逻辑层
 * @author JayFeng
 * @date 2021/10/14
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;
    @Autowired
    private EncryptUtil encryptUtil;
    @Autowired
    private UserPermissionCheck userPermissionCheck;
    @Autowired
    private CookieManagement cookieManagement;
    @Autowired
    private QueryConditionHandler queryConditionHandler;

    private Gson gson = new Gson();
    private Map<Long, ThreadLocal<User>> userThreadLocalMap = new HashMap<>(64);

    /**
     * 添加一个 threadLocal
     * @param user 当前已登录的用户
     */
    public void addUserThreadLocal(User user) {
        ThreadLocal<User> threadLocal = new ThreadLocal<>();
        threadLocal.set(user);
        userThreadLocalMap.put(Thread.currentThread().getId(), threadLocal);
    }

    /**
     * 移除 threadLocal
     * @param threadId 线程id
     */
    public void removeUserThreadLocal(Long threadId) {
        ThreadLocal<User> threadLocal = userThreadLocalMap.get(threadId);
        if (threadLocal == null) return;
        userThreadLocalMap.remove(threadId);
        threadLocal.remove();
    }

    /**
     * 分页查询系统用户数据
     * @param requestParams 请求参数
     * @param page 分页参数
     * @return 返回
     */
    public ResponseData findUserPage(Map<String, String> requestParams, Page<User> page) {
        Map<String, String> queryParamsMap = new HashMap<>(4);
        // 精确匹配查询条件
        queryParamsMap.put("userName", requestParams.get("userName"));
        queryParamsMap.put("phone", requestParams.get("phone"));
        queryParamsMap.put("role", requestParams.get("role"));
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryConditionHandler.handleEqualQueryCondition(queryWrapper, queryParamsMap);
        Page<User> dataPage = userDao.selectPage(page, queryWrapper);
        log.info("findUserPage 分页查询系统用户数据结果 total: {}", dataPage.getTotal());
        return ResponseData.createSuccessResponseData("findUserPageInfo", dataPage);
    }

    /**
     * 根据角色查询用户信息
     * @param role 角色
     * @return 返回
     */
    @Override
    public ResponseData findUserByRole(Byte role) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role", role);
        List<User> userList = userDao.selectList(queryWrapper);
        log.info("findUserByRole 根据角色查询用户信息结果 role: {}, userListSize: {}", role, userList.size());
        return ResponseData.createSuccessResponseData("findUserByRoleInfo", userList);
    }

    /**
     * 用户登录
     * @param response 用于设置 cookie
     * @param loginUser 登录用户
     * @return 返回
     */
    @Override
    public ResponseData login(HttpServletResponse response, User loginUser) {
        if (loginUser != null) {
            String userName = loginUser.getUserName();
            String password = loginUser.getPassword();
            User user = userDao.findUserByUserName(userName);
            if (user != null && encryptUtil.matches(password, user.getPassword())) {
                String value = UUID.randomUUID().toString();
                // 设置 cookie
                cookieManagement.setCookie(response, CookieManagement.USER_KEY, value);
                // 用户信息存到 redis
                cookieManagement.setLoginUser(value, RedisConstant.USER_UUID_MAP.getRedisMapKey(), gson.toJson(user));
                log.info("login 登录成功 user: {}", user);
                return ResponseData.createSuccessResponseData("loginInfo", user);
            }
        }
        log.info("login 登录失败, 用户名或密码错误 loginUser{}", loginUser);
        return ResponseData.createFailResponseData("loginInfo",
                "登录失败,用户名或密码错误",
                ResponseFailTypeConstant.USER_NAME_OR_PASSWORD_ERROR.getFailType());
    }

    /**
     * 获取当前已登录用户
     * @param request 用于获取已登录用户
     * @return 返回用户
     */
    public User getNowLoginUser(HttpServletRequest request) {
        Object object = cookieManagement.getLoginUser(request, CookieManagement.USER_KEY, RedisConstant.USER_UUID_MAP.getRedisMapKey());
        if (object == null) return null;
        User nowLoginUser = gson.fromJson(object.toString(), User.class);
        log.info("getNowLoginUser 获取当前已登录的用户 nowLoginUser: {}", nowLoginUser);
        return nowLoginUser;
    }

    /**
     * 获取当前已登录的用户
     * @return 返回用户
     */
    public User getNowLoginUser() {
        return userThreadLocalMap.get(Thread.currentThread().getId()).get();
    }

    /**
     * 用户注册
     * @param user 新用户信息
     * @param password 已登录的用户的密码
     * @return 返回
     */
    @Override
    @Transactional
    public ResponseData registry(User user, String password) {
        User nowLoginUser = getNowLoginUser();
        if (userPermissionCheck.haveRegistryPermission(nowLoginUser)) { // 是否有注册的权限
            User user2 = userDao.findUserByUserName(user.getUserName());
            if (user2 != null) {
                log.info("registry 注册失败，用户名已存在 userName: {}", user.getUserName());
                return ResponseData.createFailResponseData("registryInfo",
                        "注册失败，用户名已存在",
                        ResponseFailTypeConstant.DATA_ALREADY_EXIST.getFailType());
            }
            if (encryptUtil.matches(password, nowLoginUser.getPassword())) { // 密码是否匹配
                user.setPassword(encryptUtil.encrypt(user.getPassword()));
                user.setCreateTime(LocalDateTime.now());
                user.setUpdateTime(user.getCreateTime());
                userDao.insert(user);
                log.info("registry 注册成功 operator: {}, 新用户 user: {}", nowLoginUser.getUserName(), user);
                return ResponseData.createSuccessResponseData("registryInfo", "用户注册成功");
            }
            // 密码不匹配
            log.info("registry 注册失败, 密码校验不通过");
            return ResponseData.createFailResponseData("registryInfo",
                    "注册失败, 密码错误",
                    ResponseFailTypeConstant.PASSWORD_ERROR.getFailType());
        } else { // 没有权限注册
            log.info("registry 注册失败, 没有权限");
            return ResponseData.createFailResponseData("registryInfo",
                    "注册失败, 没有权限",
                    ResponseFailTypeConstant.NO_OPERATION_PERMISSION.getFailType());
        }
    }

    /**
     * 更新用户角色
     * @param user 新用户信息
     * @param password 已登录的用户的密码
     * @return 返回
     */
    @Override
    @Transactional
    public ResponseData updateUserRole(User user, String password) {
        User nowLoginUser = getNowLoginUser();
        if (userPermissionCheck.haveUpdatePermission(nowLoginUser)) { // 是否有权限修改
            if (encryptUtil.matches(password, nowLoginUser.getPassword())) { // 密码是否匹配
                User oldUser = userDao.selectById(user.getId());
                user.setUpdateTime(LocalDateTime.now());
                userDao.updateById(user);
                log.info("updateUserRole 修改用户角色成功, operator: {}, oldUser: {}, newUser: {}", nowLoginUser.getUserName(), oldUser, user);
                return ResponseData.createSuccessResponseData("updateUserRoleInfo", "用户信息修改成功");
            }
            // 密码不匹配
            log.info("updateUserRole 修改用户角色失败, 密码不匹配");
            return ResponseData.createFailResponseData("updateUserRoleInfo",
                    ResponseFailMessageConstant.UPDATE_PASSWORD_ERROR.getFailMessage(),
                    ResponseFailTypeConstant.PASSWORD_ERROR.getFailType());
        }
        // 没有权限修改
        log.info("updateUserRole 修改用户角色失败, 没有权限");
        return ResponseData.createFailResponseData("updateUserRoleInfo",
                ResponseFailMessageConstant.NO_UPDATE_PERMISSION.getFailMessage(),
                ResponseFailTypeConstant.NO_OPERATION_PERMISSION.getFailType());
    }

    /**
     * 删除用户
     * @param user 新用户信息
     * @param password 已登录的用户的密码
     * @return 返回
     */
    @Override
    @Transactional
    public ResponseData deleteUser(User user, String password) {
        User nowLoginUser = getNowLoginUser();
        if (userPermissionCheck.haveDeletePermission(nowLoginUser)) { // 是否有权限删除
            if (encryptUtil.matches(password, nowLoginUser.getPassword())) { // 密码是否匹配
                userDao.deleteById(user.getId());
                log.info("deleteUserInfo 删除用户信息成功, operator: {}", nowLoginUser.getUserName());
                return ResponseData.createSuccessResponseData("deleteUserInfo", "用户信息删除成功");
            }
            // 密码不匹配
            log.info("deleteUser 删除用户信息失败, 密码不匹配");
            return ResponseData.createFailResponseData("deleteUserInfo",
                    ResponseFailMessageConstant.DELETE_PASSWORD_ERROR.getFailMessage(),
                    ResponseFailTypeConstant.PASSWORD_ERROR.getFailType());
        }
        // 没有权限删除
        log.info("deleteUser 删除用户信息失败, 没有权限");
        return ResponseData.createFailResponseData("deleteUserInfo",
                ResponseFailMessageConstant.NO_DELETE_PERMISSION.getFailMessage(),
                ResponseFailTypeConstant.NO_OPERATION_PERMISSION.getFailType());
    }

}
