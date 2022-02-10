package jayfeng.barcode.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jayfeng.barcode.bean.AccessKey;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 阿里短信服务密钥数据持久层
 * @author JayFeng
 * @date 2021/10/14
 */
@Repository
public interface AccessKeyDao extends BaseMapper<AccessKey> {

    /**
     * 查询所有短信服务密钥
     * @return
     */
    @Select("SELECT `id`, `region_id`, `access_key_id`, `secret`, `create_time`, `update_time` " +
            "FROM `access_key`")
    List<AccessKey> findAllAccessKey();

    /**
     * 根据密钥 id 查询数据 id。用于判断数据是否存在
     * @param accessKeyId 密钥 id
     * @return
     */
    @Select("SELECT `id` FROM `access_key` WHERE `access_key_id` = #{accessKeyId}")
    Integer findIdByAccessKeyId(@Param("accessKeyId") String accessKeyId);

}
