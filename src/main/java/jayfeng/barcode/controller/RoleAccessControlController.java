package jayfeng.barcode.controller;

import jayfeng.barcode.bean.RoleAccessControl;
import jayfeng.barcode.response.ResponseMessage;
import jayfeng.barcode.service.RoleAccessControlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 角色访问权限管理控制层
 * @author JayFeng
 * @date 2021/10/16
 */
@Slf4j
@RestController
@RequestMapping("/barcode/roleAccessControl")
public class RoleAccessControlController extends BaseController {

    @Autowired
    private RoleAccessControlService roleAccessControlService;

    /**
     * 分页查询角色访问模块
     * @param requestParams 请求参数
     * @return 返回
     */
    @GetMapping("/findRoleAccessControlPage")
    public ResponseMessage findRoleAccessControlPage(@RequestParam Map<String, String> requestParams) {
        log.info("findRoleAccessControlPage 分页查询角色访问模块 requestParams: {}", requestParams);
        return requestSuccess(roleAccessControlService.findRoleAccessControlPage(requestParams, getPage(requestParams)));
    }

    /**
     * 添加角色访问模块
     * @param roleAccessControl 新的访问模块
     * @param password 用户的登录密码
     * @return 返回
     */
    @PostMapping("/addRoleAccessControl/{password}")
    public ResponseMessage addRoleAccessControl(@RequestBody RoleAccessControl roleAccessControl,
                                                @PathVariable("password") String password) {
        log.info("addRoleAccessControl 添加角色访问模块 roleAccessControl: {}", roleAccessControl);
        return requestSuccess(roleAccessControlService.addRoleAccessControl(roleAccessControl, password));
    }

    /**
     * 更新角色访问模块
     * @param roleAccessControl 待更新的访问模块
     * @param password 用户的登录密码
     * @return 返回
     */
    @PutMapping("/updateRoleAccessControl/{password}")
    public ResponseMessage updateRoleAccessControl(@RequestBody RoleAccessControl roleAccessControl,
                                                   @PathVariable("password") String password) {
        log.info("updateRoleAccessControl 更新角色访问模块信息 roleAccessControl: {}", roleAccessControl);
        return requestSuccess(roleAccessControlService.updateRoleAccessControl(roleAccessControl, password));
    }

    /**
     * 删除角色访问模块
     * @param roleAccessControl 待删除的访问模块
     * @param password 用户的登录密码
     * @return 返回
     */
    @DeleteMapping("/deleteRoleAccessControl/{password}")
    public ResponseMessage deleteRoleAccessControl(@RequestBody RoleAccessControl roleAccessControl,
                                                   @PathVariable("password") String password) {
        log.info("deleteRoleAccessControl 删除角色访问模块 roleAccessControl: {}", roleAccessControl);
        return requestSuccess(roleAccessControlService.deleteRoleAccessControl(roleAccessControl, password));
    }

}
