package jayfeng.barcode.util;

import jayfeng.barcode.bean.User;
import jayfeng.barcode.service.RoleAccessControlService;
import org.springframework.stereotype.Component;


/**
 * 当前登录的用户权限验证
 * 是否有权限删除
 * 是否有权限修改
 * @author JayFeng
 * @date 2021/10/16
 */
@Component
public class UserPermissionCheck {

    /**
     * 是否有添加数据的权限
     * @param nowLoginUser 当前已登录用户
     * @return 返回
     */
    public Boolean haveAddPermission(User nowLoginUser) {
        return permissionCheck(nowLoginUser);
    }

    /**
     * 是否有注册账号的权限
     * @param nowLoginUser 当前已登录用户
     * @return 返回
     */
    public Boolean haveRegistryPermission(User nowLoginUser) {
        return permissionCheck(nowLoginUser);
    }

    /**
     * 是否有删除的权限 删除用户、删除产品信息等这些比较有影响的删除操作
     * @param nowLoginUser 当前已登录用户
     * @return 返回
     */
    public Boolean haveDeletePermission(User nowLoginUser) {
        return permissionCheck(nowLoginUser);
    }

    /**
     * 是否有修改的权限  修改短信服务密钥、修改角色信息等比较有影响的操作
     * @param nowLoginUser 当前已登录用户
     * @return 返回
     */
    public Boolean haveUpdatePermission(User nowLoginUser) {
        return permissionCheck(nowLoginUser);
    }

    /**
     * 当前登录的用户是否是开发人员或者仓库管理员
     * @param nowLoginUser 当前登录用户
     * @return 返回
     */
    private Boolean permissionCheck(User nowLoginUser) {
        return nowLoginUser != null && // 已经登录
                (nowLoginUser.getRole().equals(RoleAccessControlService.WARE_HOUSE_MANAGER) || // 并且角色是仓库管理员
                        nowLoginUser.getRole().equals(RoleAccessControlService.DEVELOPERS)); // 或开发人员。则有权限
    }

}
