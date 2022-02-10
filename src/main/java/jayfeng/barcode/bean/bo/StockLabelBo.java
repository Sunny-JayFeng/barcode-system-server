package jayfeng.barcode.bean.bo;

import lombok.Data;

/**
 * 库存标签
 * @author JayFeng
 * @date 2021/11/8
 */
@Data
public class StockLabelBo {

    /**
     * 料号
     */
    private String proMaterialNumber;

    /**
     * 型号
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
     * 仓库名称
     */
    private String wareName;

    /**
     * 货架编码
     */
    private String shelfCode;

    /**
     * 类型
     */
    private Byte type;

    /**
     * 序列号
     */
    private String serialNumber;

    /**
     * 打印数量：多少张
     */
    private Integer total;

}
