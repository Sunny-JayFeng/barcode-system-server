package jayfeng.barcode.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jayfeng.barcode.bean.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 用户数据持久层
 * @author JayFeng
 * @date 2021/10/14
 */
@Repository
public interface UserDao extends BaseMapper<User> {

    /**
     * 通过用户名查询用户
     * @param userName 用户名
     * @return 返回用户信息
     */
    @Select("SELECT `id`, `user_name`, `password`, `role`, `role_name` FROM `user` WHERE `user_name` = #{userName}")
    User findUserByUserName(@Param("userName") String userName);

}
