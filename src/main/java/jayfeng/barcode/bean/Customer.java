package jayfeng.barcode.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客户实体类
 * @author JayFeng
 * @date 2021/10/19
 */
@Data
public class Customer {

    /**
     * id
     */
    @TableId(type= IdType.AUTO)
    private Integer id;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 客户地址
     */
    private String address;

    /**
     * 客户电话
     */
    private String phone;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

}
