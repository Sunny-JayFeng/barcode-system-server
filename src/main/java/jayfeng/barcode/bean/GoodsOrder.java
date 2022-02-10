package jayfeng.barcode.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 出货单实体类
 * @author JayFeng
 * @date 2021/11/4
 */
@Data
public class GoodsOrder {

    /**
     * id
     */
    @TableId(type= IdType.AUTO)
    private Integer id;

    /**
     * 创建订单的用户
     */
    private String createUserName;

    /**
     * 订单客户
     */
    private String customerName;

    /**
     * 料号
     */
    private String proMaterialNumber;

    /**
     * 型号
     */
    private String proModel;

    /**
     * 数量
     */
    private Integer amount;

    /**
     * 出货单编号
     */
    private Long goodsOrderNumber;

    /**
     * 状态
     * 0 -- 未出货
     * 1 -- 已出货
     */
    private Byte status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

}
