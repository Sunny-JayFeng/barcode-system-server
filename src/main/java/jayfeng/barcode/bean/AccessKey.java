package jayfeng.barcode.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 阿里短信服务密钥实体
 * @author JayFeng
 * @date 2021/10/14
 */
@Data
public class AccessKey {

    /**
     * 主键 id
     */
    @TableId(type= IdType.AUTO)
    private Integer id;

    /**
     * 发送源 id
     */
    private String regionId;

    /**
     * 密钥 id
     */
    private String accessKeyId;

    /**
     * 密钥
     */
    private String secret;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

}
