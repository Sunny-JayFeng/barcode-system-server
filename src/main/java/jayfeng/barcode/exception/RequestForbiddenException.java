package jayfeng.barcode.exception;

/**
 * 拒绝处理请求，抛出这个异常
 * @author JayFeng
 * @date 2021/10/12
 */
public class RequestForbiddenException extends RuntimeException {

    public RequestForbiddenException(String message) {
        super(message);
    }

}
