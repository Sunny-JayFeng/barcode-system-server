package jayfeng.barcode.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jayfeng.barcode.bean.RoleAccessControl;
import jayfeng.barcode.response.ResponseData;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 角色访问权限管理业务逻辑层
 * @author JayFeng
 * @date 2021/10/16
 */
@Service
public interface RoleAccessControlService {

    Byte DEVELOPERS = 0; // 系统管理员（开发人员）
    Byte WARE_HOUSE_MANAGER = 1; // 仓库管理员（有权力注册账号，允许删除操作）
    Byte WARE_HOUSE_WORKER = 2; // 仓库工作人员（不允许执行删除操作）
    Byte COMPANY_MANAGER = 3; // 公司管理层（不允许执行删除操作）

    /**
     * 分页查询访问控制模块
     * @param requestParams 请求参数
     * @param page 分页参数
     * @return 返回
     */
    ResponseData findRoleAccessControlPage(Map<String, String> requestParams, Page<RoleAccessControl> page);

    /**
     * 添加访问模块
     * @param roleAccessControl 新的访问模块
     * @param password 用户的登录密码
     * @return 返回
     */
    ResponseData addRoleAccessControl(RoleAccessControl roleAccessControl, String password);

    /**
     * 更新访问模块
     * @param roleAccessControl 待更新的访问模块
     * @param password 用户的登录密码
     * @return 返回
     */
    ResponseData updateRoleAccessControl(RoleAccessControl roleAccessControl, String password);

    /**
     * 删除访问模块
     * @param roleAccessControl 待删除的访问模块
     * @param password 用户的登录密码
     * @return 返回
     */
    ResponseData deleteRoleAccessControl(RoleAccessControl roleAccessControl, String password);

}
