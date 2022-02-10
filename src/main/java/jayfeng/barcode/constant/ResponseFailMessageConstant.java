package jayfeng.barcode.constant;

/**
 * 响应失败信息常量
 * @author JayFeng
 * @date 2021/10/21
 */
public enum ResponseFailMessageConstant {

    NO_ADD_PERMISSION("添加失败，没有权限"),
    NO_UPDATE_PERMISSION("修改失败，没有权限"),
    NO_DELETE_PERMISSION("删除失败，没有权限"),

    ADD_PASSWORD_ERROR("添加失败，密码错误"),
    UPDATE_PASSWORD_ERROR("修改失败，密码错误"),
    DELETE_PASSWORD_ERROR("删除失败，密码错误"),

    ADD_DATA_ERROR("添加失败，数据错误"),
    UPDATE_DATA_ERROR("修改失败，数据错误"),
    DELETE_DATA_ERROR("删除失败，数据错误"),

    NEVER_LOGIN("操作失败，未登录");

    private String failMessage;

    ResponseFailMessageConstant(String failMessage) {
        this.failMessage = failMessage;
    }

    public String getFailMessage() {
        return this.failMessage;
    }

}
