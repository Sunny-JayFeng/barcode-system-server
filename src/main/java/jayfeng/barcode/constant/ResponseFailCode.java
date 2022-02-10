package jayfeng.barcode.constant;

/**
 * 请求失败响应码
 * @author JayFeng
 * @date 2021/10/12
 */
public enum ResponseFailCode {

    KNOWN_EXCEPTION(500),

    SERVER_BUSY(503),

    UNKNOWN_EXCEPTION(999),

    REQUEST_FORBIDDEN(403),

    METHOD_NOT_SUPPORTED(405);

    private Integer responseCode;

    ResponseFailCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public Integer getResponseCode() {
        return this.responseCode;
    }

}
