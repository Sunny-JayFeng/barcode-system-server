package jayfeng.barcode.bean.bo;

import lombok.Data;

/**
 * 客户定制标签
 * @author JayFeng
 * @date 2021/11/8
 */
@Data
public class CustomerLabelBo {


    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 公司名称
     */
    private String companyName;

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
    private String amount;

    /**
     * 序列号
     */
    private String serialNumber;

    /**
     * 地址
     */
    private String address;

    /**
     * 打印数量
     */
    private Integer total;

}
