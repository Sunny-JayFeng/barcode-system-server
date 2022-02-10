package jayfeng.barcode.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品信息实体
 * @author JayFeng
 * @date 2021/10/13
 */
@Data
public class Product {

    /**
     * id
     */
    @TableId(type= IdType.AUTO)
    private Integer id;

    /**
     * 产品每盒标准数量
     */
    private Integer boxStandardQuantity;

    /**
     * 产品每箱标准数量
     */
    private Integer caseStandardQuantity;

    /**
     * 产品料号
     */
    private String proMaterialNumber;

    /**
     * 产品型号
     */
    private String proModel;

    /**
     * 产品序列号
     */
    private String serialNumber;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

}
