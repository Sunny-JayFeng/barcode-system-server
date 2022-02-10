package jayfeng.barcode.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 盘点日志实体类
 * @author JayFeng
 * @date 2021/11/2
 */
@Data
public class InventoryLog {

    /**
     * id
     */
    @TableId(type= IdType.AUTO)
    private Integer id;

    /**
     * 库存 id
     */
    private Integer stockId;

    /**
     * 产品料号
     */
    private String proMaterialNumber;

    /**
     * 产品型号
     */
    private String proModel;

    /**
     * 批号
     */
    private String lotNumber;

    /**
     * 实际数量
     */
    private Integer realAmount;

    /**
     * 二维码上标的数量
     */
    private Integer qrCodeAmount;

    /**
     * 是否有差异
     * 0 -- 否
     * 1 -- 有
     */
    private Byte different;

    /**
     * 差异数量
     */
    private Integer amountDifference;


    /**
     * 仓库编码
     */
    private String wareCode;

    /**
     * 仓库名称
     */
    private String wareName;

    /**
     * 货架编码
     */
    private String shelfCode;

    /**
     * 序列号
     */
    private Long serialNumber;

    /**
     * 创建时间 / 盘点日期
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

}
