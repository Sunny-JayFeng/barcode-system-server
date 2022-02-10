package jayfeng.barcode.handler;

import jayfeng.barcode.constant.ResponseFailCode;
import jayfeng.barcode.controller.BaseController;
import jayfeng.barcode.exception.RequestForbiddenException;
import jayfeng.barcode.exception.ServerBusyException;
import jayfeng.barcode.response.ResponseMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

/**
 * 全局异常处理
 * @author JayFeng
 * @date 2021/10/12
 */
@Slf4j
@RestController
@ControllerAdvice
public class GlobalExceptionHandler extends BaseController {

    @ExceptionHandler(Exception.class)
    public ResponseMessage handler(Exception e) {
        StackTraceElement stackTraceElement = e.getStackTrace()[0];
        log.info("出现异常, 异常类型: {}", e.toString());
        log.info("异常位置: {} 类的第 {} 行, 出现异常的方法: {}", stackTraceElement.getClassName(), stackTraceElement.getLineNumber(), stackTraceElement.getMethodName());
        // 请求方式不被支持
        if (e.getClass() == HttpRequestMethodNotSupportedException.class) {
            HttpRequestMethodNotSupportedException exception = (HttpRequestMethodNotSupportedException) e;
            return requestFail(ResponseFailCode.METHOD_NOT_SUPPORTED.getResponseCode(), "请求方式不被支持", "request_method_not_allowed");
        }
        // 无权限访问
        if (e.getClass() == RequestForbiddenException.class) {
            RequestForbiddenException exception = (RequestForbiddenException) e;
            return requestFail(ResponseFailCode.REQUEST_FORBIDDEN.getResponseCode(), exception.getMessage(), "request_forbidden");
        }
        // 服务端繁忙
        if (e.getClass() == ServerBusyException.class) {
            ServerBusyException exception = (ServerBusyException) e;
            return requestFail(ResponseFailCode.SERVER_BUSY.getResponseCode(), exception.getMessage(), "server_busy");
        }
        // 未知异常
        return requestFail(ResponseFailCode.UNKNOWN_EXCEPTION.getResponseCode(), "服务器崩溃", "server_crack_up");
    }

}
