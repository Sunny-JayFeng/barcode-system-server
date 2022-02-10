package jayfeng.barcode.constant;

/**
 * 响应失败类型常量
 * @author JayFeng
 * @date 2021/10/19
 */
public enum ResponseFailTypeConstant {

    DATA_ERROR("data_error"), // 数据错误

    PARAMS_ERROR("params_error"), // 参数错误

    DATA_ALREADY_EXIST("data_already_exist"), // 待添加的数据已存在

    DATA_NOT_EXIST("data_not_exist"), // 数据不存在

    PASSWORD_ERROR("password_error"), // 密码错误

    NO_OPERATION_PERMISSION("no_operation_permission"), // 没有权限执行操作

    QUANTITY_PARAMS_ERROR("quantity_params_error"), // 数量参数错误

    USER_NAME_OR_PASSWORD_ERROR("user_name_or_password_error"), // 用户名或密码错误

    WARE_HOUSE_SPACE_NOT_ENOUGH("ware_house_space_not_enough"), // 仓库空间不足

    SHELF_SPACE_NOT_ENOUGH("shelf_space_not_enough"), // 货架空间不足

    NEVER_LOGIN("never_login"), // 未登录

    UNKNOWN_ERROR("unknown_error"), // 未知错误

    SYSTEM_BUSY("system_busy"); // 系统繁忙

    private String failType;

    ResponseFailTypeConstant(String failType) {
        this.failType = failType;
    }

    public String getFailType() {
        return this.failType;
    }

}
