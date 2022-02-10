package jayfeng.barcode.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存日志
 * 库存出库入库日志
 * 包括从某个仓库移动到某个仓库，要先出库，再入库
 * @author JayFeng
 * @date 2021/10/21
 */
@Data
public class StockLog {

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
     * 数量
     */
    private Integer amount;

    /**
     * 库存类型
     * 0 -- 袋
     * 1 -- 盒
     * 2 -- 箱
     */
    private Byte type;

//    /**
//     * 库存状态
//     * 0 -- 在库
//     * 1 -- 待出库
//     * 2 -- 已出库
//     * 3 -- 不良品待销毁
//     */
//    private Byte status;

    /**
     * 操作类型
     * 0 -- 出货
     * 1 -- 入库
     * 2 -- 出库
     * 3 -- 上架
     * 4 -- 下架
     * 5 -- 变更货架
     * 6 -- 变更仓库
     * 7 -- 拼盒
     * 8 -- 拼箱
     */
    private Byte operationType;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 出货单号
     */
    private Long goodsOrderNumber;

    /**
     * 拼盒编号
     */
    private String packBoxNumber;

    /**
     * 拼箱编号
     */
    private String packCaseNumber;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 如果拼盒或者拼箱编号不为空，那么这个值会为空
     * 如果没有拼盒或拼箱，stockId 去 stock 表映射
     */
    @TableField(exist = false)
    private Stock stock;

//    /**
//     * 如果是拼盒，利用 stockId 去 packBoxStockLog 映射
//     */
//    @TableField(exist = false)
//    private PackBoxStockLog packBoxStockLog;
//
//    /**
//     * 如果是拼箱，利用 stockId 去 packCaseStockLog 映射
//     */
//    @TableField(exist = false)
//    private PackCaseStockLog packCaseStockLog;

}
