package jayfeng.barcode.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色访问控制实体
 * @author JayFeng
 * @date 2021/10/16
 */
@Data
public class RoleAccessControl {

    /**
     * id
     */
    @TableId(type= IdType.AUTO)
    private Integer id;

    /**
     * 角色
     * 0 -- 系统管理员（开发人员）
     * 1 -- 仓库管理员（有权力注册账号，允许删除操作）
     * 2 -- 仓库工作人员（不允许执行删除操作）
     * 3 -- 公司管理层（不允许执行删除操作）
     */
    private Byte role;

    /**
     * 角色名
     */
    private String roleName;

    /**
     * 允许访问的模块基础路径
     */
    private String accessModelBasePath;

    /**
     * 模块图标名称
     */
    private String modelImage;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

}
