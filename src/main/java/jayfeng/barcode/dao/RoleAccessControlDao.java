package jayfeng.barcode.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jayfeng.barcode.bean.RoleAccessControl;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色访问权限管理持久层
 * @author JayFeng
 * @date 2021/10/16
 */
@Repository
public interface RoleAccessControlDao extends BaseMapper<RoleAccessControl> {

    /**
     * 查询所有角色允许访问的模块
     * @return 返回
     */
    @Select("SELECT `id`, `role`, `role_name`, `access_model_base_bath`, `model_image`, `create_time`, `update_time` " +
            "FROM `role_access_control`")
    List<RoleAccessControl> findAllRoleAccessControl();

    /**
     * 根据角色查询允许访问的模块路径
     * @param role 角色
     * @return 返回
     */
    @Select("SELECT `access_model_base_bath` FROM `role_access_control` WHERE `role` = #{role}")
    List<String> findModelBasePath(@Param("role") Byte role);

    /**
     * 用于判断权限是否已存在
     * @param role 角色
     * @param accessModelBasePath 模块基础路径
     * @return
     */
    @Select("SELECT `id` FROM `role_access_control` " +
            "WHERE `role` = #{role} AND `access_model_base_path` = #{accessModelBasePath}")
    Integer findIdByRoleAndPath(@Param("role") Byte role, @Param("accessModelBasePath") String accessModelBasePath);

}
