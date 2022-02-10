package jayfeng.barcode.exception;

/**
 * 参数为空异常
 * @author JayFeng
 * @date 2021/10/12
 */
public class ParamEmptyException extends RuntimeException {

    public ParamEmptyException(String message) {
        super(message);
    }

}
