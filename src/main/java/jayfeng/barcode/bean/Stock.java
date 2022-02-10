package jayfeng.barcode.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存数据实体类
 * @author JayFeng
 * @date 2021/10/21
 */
@Data
public class Stock {

    /**
     * id
     */
    @TableId(type= IdType.AUTO)
    private Integer id;

    /**
     * 扫码出库入库这些操作，会给前端返回一个 stock 对象，用于展示，
     * 如果某一个货物点击取消出库，需要像后端发送 stockId，和刚才的出库日志id，因为需要删除这条日志
     */
    @TableField(exist = false)
    private Integer stockLogId;

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

    /**
     * 库存状态
     * 0 -- 在库
     * 1 -- 待出库
     * 2 -- 已出库
     * 3 -- 不良品待销毁
     */
    private Byte status;

    /**
     * 出货单号
     */
    private Long goodsOrderNumber;

    /**
     * 是否拼盒
     * 0 -- 否
     * 1 -- 是
     */
    private Byte isPackBox;

    /**
     * 拼盒编号
     */
    private String packBoxNumber;

    /**
     * 是否拼箱
     * 0 -- 否
     * 1 -- 是
     */
    private Byte isPackCase;

    /**
     * 拼箱编号
     */
    private String packCaseNumber;

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
     * 入库日期
     */
    private LocalDateTime inTime;

    /**
     * 序列号
     */
    private Long serialNumber;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

}
