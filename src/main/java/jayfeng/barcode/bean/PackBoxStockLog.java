package jayfeng.barcode.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 袋库存拼盒日志实体类
 * 当多袋拼成一盒的时候，stock 表的数据会有删除和新增操作。
 * 比如：5 袋拼成 1 盒。这个实体类会新增 5 条数据，就是那 5 袋，
 * stock 表中会删除那 5 袋的数据，新增 1 条盒的数据
 * @author JayFeng
 * @date 2021/10/21
 */
@Data
public class PackBoxStockLog {

    /**
     * id
     */
    @TableId(type= IdType.AUTO)
    private Integer id;

    /**
     * 原先这个库存在 stock 表中的库存 id
     * 拼后就无法映射，这里是为了给 StockLog 映射
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
     * 拼盒编号
     */
    private String packBoxNumber;

    /**
     * 所在仓库编码
     */
    private String wareCode;

    /**
     * 所在仓库名称
     */
    private String wareName;

    /**
     * 所在货架编码
     */
    private String shelfCode;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

}
