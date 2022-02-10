package jayfeng.barcode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jayfeng.barcode.bean.RoleAccessControl;
import jayfeng.barcode.bean.User;
import jayfeng.barcode.constant.ResponseFailMessageConstant;
import jayfeng.barcode.constant.ResponseFailTypeConstant;
import jayfeng.barcode.dao.RoleAccessControlDao;
import jayfeng.barcode.response.ResponseData;
import jayfeng.barcode.service.RoleAccessControlService;
import jayfeng.barcode.service.UserService;
import jayfeng.barcode.util.EncryptUtil;
import jayfeng.barcode.handler.QueryConditionHandler;
import jayfeng.barcode.util.UserPermissionCheck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色访问权限管理控制层
 * @author JayFeng
 * @date 2021/10/16
 */
@Slf4j
@Service
public class RoleAccessControlServiceImpl implements RoleAccessControlService {

    @Autowired
    private RoleAccessControlDao roleAccessControlDao;
    @Autowired
    private UserService userService;
    @Autowired
    private UserPermissionCheck userPermissionCheck;
    @Autowired
    private EncryptUtil encryptUtil;
    @Autowired
    private QueryConditionHandler queryConditionHandler;

    /**
     * 分页查询角色访问模块
     * @param requestParams 请求参数
     * @param page 分页参数
     * @return 返回
     */
    @Override
    public ResponseData findRoleAccessControlPage(Map<String, String> requestParams, Page<RoleAccessControl> page) {
        log.info("findRoleAccessControlPage 分页查询角色访问模块信息： requestParams: {}, page: {}", requestParams, page);
        Map<String, String> queryTypeMap = new HashMap<>(8);
        queryTypeMap.put("roleName", QueryConditionHandler.EQUAL);
        queryTypeMap.put("accessModelBasePath", QueryConditionHandler.EQUAL);
        QueryWrapper<RoleAccessControl> queryWrapper = new QueryWrapper<>();
        queryConditionHandler.handleQueryCondition(queryWrapper, queryTypeMap, requestParams);
        Page<RoleAccessControl> dataPage = roleAccessControlDao.selectPage(page, queryWrapper);
        log.info("findRoleAccessControlPage 分页查询角色访问模块信息结果 total: {}", dataPage.getTotal());
        return ResponseData.createSuccessResponseData("findRoleAccessControlPageInfo", dataPage);
    }

    /**
     * 添加角色访问模块
     * @param roleAccessControl 新的访问模块
     * @param password 用户的登录密码
     * @return 返回
     */
    @Override
    @Transactional
    public ResponseData addRoleAccessControl(RoleAccessControl roleAccessControl, String password) {
        User nowLoginUser = userService.getNowLoginUser();
        if (userPermissionCheck.haveAddPermission(nowLoginUser)) {
            if (encryptUtil.matches(password, nowLoginUser.getPassword())) {
                if (roleAccessControlDao.findIdByRoleAndPath(roleAccessControl.getRole(), roleAccessControl.getAccessModelBasePath()) != null) {
                    log.info("addRoleAccessControl 添加角色访问模块失败，权限已存在");
                    return ResponseData.createFailResponseData("addRoleAccessControlInfo",
                            "添加失败，访问权限已存在",
                            ResponseFailTypeConstant.DATA_ALREADY_EXIST.getFailType());
                }
                // 分配的角色前端是多选的，为了不发多次请求，用 id 暂存选中的角色，这里需要拆开来
                int tempRoles = roleAccessControl.getId();
                roleAccessControl.setId(null);
                List<Byte> roleList = new ArrayList<>(4);
                roleList.add((byte)0); // 系统管理员
                while (tempRoles != 0) {
                    byte role = (byte)(tempRoles % 10);
                    tempRoles /= 10;
                    if (role < 1 || role > 3) { // 0 是系统管理员，前端不用手动分配
                        log.info("addRoleAccessControl 添加角色访问模块失败，角色类型有误: role: {}", role);
                        return ResponseData.createFailResponseData("addRoleAccessControlInfo", "添加失败，角色分配有误", "role_type_error");
                    }
                    roleList.add(role);
                }
                // 角色都没有错误，可以添加
                for (Byte role : roleList) {
                    roleAccessControl.setRole(role);
                    roleAccessControl.setRoleName(getRoleName(role));
                    roleAccessControl.setCreateTime(LocalDateTime.now());
                    roleAccessControl.setCreateTime(LocalDateTime.now());
                    roleAccessControlDao.insert(roleAccessControl);
                }
                log.info("addRoleAccessControl 添加角色访问模块成功 operator: {}, roleAccessControl: {}", nowLoginUser.getUserName(), roleAccessControl);
                return ResponseData.createSuccessResponseData("addRoleAccessControlInfo", "角色访问模块添加成功");
            } else {
                log.info("addRoleAccessControl 添加角色访问模块失败,密码不匹配");
                return ResponseData.createFailResponseData("addRoleAccessControlInfo",
                        ResponseFailMessageConstant.ADD_PASSWORD_ERROR.getFailMessage(),
                        ResponseFailTypeConstant.PASSWORD_ERROR.getFailType());
            }
        } else { // 没有权限执行添加操作
            log.info("addRoleAccessControl 添加角色访问模块失败，没有权限");
            return ResponseData.createFailResponseData("addRoleAccessControlInfo",
                    ResponseFailMessageConstant.NO_ADD_PERMISSION.getFailMessage(),
                    ResponseFailTypeConstant.NO_OPERATION_PERMISSION.getFailType());
        }
    }

    /**
     * 更新角色访问模块
     * @param roleAccessControl 待更新的访问模块
     * @param password 用户的登录密码
     * @return 返回
     */
    @Override
    @Transactional
    public ResponseData updateRoleAccessControl(RoleAccessControl roleAccessControl, String password) {
        User nowLoginUser = userService.getNowLoginUser();
        if (userPermissionCheck.haveUpdatePermission(nowLoginUser)) {
            if (encryptUtil.matches(password, nowLoginUser.getPassword())) {
                RoleAccessControl oldRoleAccessControl = roleAccessControlDao.selectById(roleAccessControl.getId());
                roleAccessControl.setUpdateTime(LocalDateTime.now());
                roleAccessControlDao.updateById(roleAccessControl);
                log.info("updateRoleAccessControl 更新角色访问模块成功，operator: {}, oldRoleAccessControl: {}, newRoleAccessControl: {}", nowLoginUser.getUserName(), oldRoleAccessControl, roleAccessControl);
                return ResponseData.createSuccessResponseData("updateRoleAccessControlInfo", "角色访问模块更新成功");
            } else {
                log.info("updateRoleAccessControl 更新角色访问模块失败，密码不匹配");
                return ResponseData.createFailResponseData("updateRoleAccessControlInfo",
                        ResponseFailMessageConstant.UPDATE_PASSWORD_ERROR.getFailMessage(),
                        ResponseFailTypeConstant.PASSWORD_ERROR.getFailType());
            }
        } else {
            log.info("updateRoleAccessControl 更新角色访问模块失败，没有权限");
            return ResponseData.createFailResponseData("updateRoleAccessControlInfo",
                    ResponseFailMessageConstant.NO_UPDATE_PERMISSION.getFailMessage(),
                    ResponseFailTypeConstant.NO_OPERATION_PERMISSION.getFailType());
        }
    }

    /**
     * 删除角色访问模块
     * @param roleAccessControl 待删除的访问模块
     * @param password 用户的登录密码
     * @return 返回
     */
    @Override
    @Transactional
    public ResponseData deleteRoleAccessControl(RoleAccessControl roleAccessControl, String password) {
        User nowLoginUser = userService.getNowLoginUser();
        if (userPermissionCheck.haveDeletePermission(nowLoginUser)) {
            if (encryptUtil.matches(password, nowLoginUser.getPassword())) {
                roleAccessControlDao.deleteById(roleAccessControl.getId());
                log.info("deleteRoleAccessControl 删除角色访问模块成功, operator: {}, roleAccessControl: {}", nowLoginUser.getUserName(), roleAccessControl);
                return ResponseData.createSuccessResponseData("deleteRoleAccessControlInfo", "角色访问模块删除成功");

            } else {
                log.info("deleteRoleAccessControl 删除角色访问模块失败, 密码不匹配");
                return ResponseData.createFailResponseData("deleteRoleAccessControlInfo",
                        ResponseFailMessageConstant.DELETE_PASSWORD_ERROR.getFailMessage(),
                        ResponseFailTypeConstant.PASSWORD_ERROR.getFailType());
            }
        } else {
            log.info("deleteRoleAccessControl 删除角色访问模块失败，没有权限");
            return ResponseData.createFailResponseData("deleteRoleAccessControlInfo",
                    ResponseFailMessageConstant.NO_DELETE_PERMISSION.getFailMessage(),
                    ResponseFailTypeConstant.NO_OPERATION_PERMISSION.getFailType());
        }
    }

    /**
     * 根据角色类型返回角色名称
     * @param role 角色类型
     * @return
     */
    private String getRoleName(byte role) {
        if (role == 0) return "系统管理员";
        else if (role == 1) return "仓库管理员";
        else if (role == 2) return "仓库工作人员";
        else if (role == 3) return "公司管理层";
        return null;
    }

}
