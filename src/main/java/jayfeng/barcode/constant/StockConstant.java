package jayfeng.barcode.constant;

/**
 * 库存常量：库存类型、库存状态、库存操作类型
 * @author JayFeng
 * @date 2021/10/23
 */
public enum StockConstant {

    BAG("袋", (byte)0), // 库存类型：袋
    BOX("盒", (byte)1), // 库存类型：盒
    CASE("箱", (byte)2), // 库存类型：箱

    IN_WARE_HOUSE("在库", (byte)0), // 库存状态：在库
    WAIT_OUT_WARE_HOUSE("待出库", (byte)1), // 库存状态：待出库
    ALREADY_OUT_WARE_HOUSE("已出库", (byte)2), // 库存状态： 已出库
    DEFECTIVE_PRO_WAIT_DESTROYED("不良品待销毁", (byte)3), // 库存状态：不良品待销毁

    UN_PACK_BOX("没有拼盒", (byte)0), // 没有拼盒
    IS_PACK_BOX("拼盒", (byte)1), // 拼盒

    UN_PACK_CASE("没有拼箱", (byte)0), // 没有拼箱
    IS_PACK_CASE("拼箱", (byte)1), // 拼箱


    SHIPMENT("出货", (byte)0), // 操作类型：出货
    INTO_THE_WAREHOUSE("入库", (byte)1), // 操作类型：入库
    OUT_OF_WAREHOUSE("出库", (byte)2), // 操作类型：出库
    PUT_ON_THE_SHELF("上架", (byte)3), // 操作类型：上架
    REMOVE_FROM_THE_SHELF("下架", (byte)4), // 操作类型：下架
    CHANGE_SHELF("变更货架", (byte) 5), // 操作类型：变更货架
    CHANGE_WAREHOUSE("变更仓库", (byte) 6), // 操作类型：变更仓库
    PACK_BOX("拼盒", (byte) 7), // 操作类型：拼盒
    PACK_CASE("拼箱", (byte)8); // 操作类型：拼箱

    private Byte value;

    StockConstant(String type, Byte value) {
        this.value = value;
    }

    public Byte getType() {
        return this.value;
    }

    public Byte getStatus() {
        return this.value;
    }

    public Byte getPackStatus() {
        return this.value;
    }

    public Byte getOperationType() {
        return this.value;
    }

}
