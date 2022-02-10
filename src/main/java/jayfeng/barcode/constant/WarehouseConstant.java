package jayfeng.barcode.constant;

/**
 * @author JayFeng
 * @date 2021/10/25
 */
public enum WarehouseConstant {

    QP_STARTS_WIDTH("QP"), // 良品仓仓库编码前缀
    QUALIFIED_PRODUCTS_WAREHOUSE_1("良品仓1", "QP01"),
    QUALIFIED_PRODUCTS_WAREHOUSE_2("良品仓2", "QP02"),
    QUALIFIED_PRODUCTS_WAREHOUSE_3("良品仓3", "QP03"),

    UQP_STARTS_WIDTH("UQP"), // 不良品仓仓库编码前缀
    UNQUALIFIED_PRODUCTS_WAREHOUSE_1("不良品仓1", "UQP01"),
    UNQUALIFIED_PRODUCTS_WAREHOUSE_2("不良品仓2", "UQP02"),

    INS_STARTS_WIDTH("INS"), // 检测仓仓库编码前缀
    INSPECTION_WAREHOUSE_1("检测仓1", "INS01"),
    INSPECTION_WAREHOUSE_2("检测仓2", "INS02"),
    INSPECTION_WAREHOUSE_3("检测仓3", "INS03"),

    SHIP_WIDTH("SHIP"), // 出货仓仓库编码前缀
    SHIPMENT_WAREHOUSE_1("出货仓1", "SHIP01"),
    SHIPMENT_WAREHOUSE_2("出货仓2", "SHIP02"),
    SHIPMENT_WAREHOUSE_3("出货仓3", "SHIP03");

    private String wareName;

    private String wareCode;

    private String startsWidth;

    WarehouseConstant(String startsWidth) {
        this.startsWidth = startsWidth;
    }

    WarehouseConstant(String wareName, String wareCode) {
        this.wareName = wareName;
        this.wareCode = wareCode;
    }

    public String getWareName() {
        return this.wareName;
    }

    public String getWareCode() {
        return this.wareCode;
    }

    public String getStartsWidth() {
        return this.startsWidth;
    }

}
