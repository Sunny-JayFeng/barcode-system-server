package jayfeng.barcode.util;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * 打印标签工具类
 * @author JayFeng
 * @date 2021/10/29
 */
@Component
public class PrintLabelUtil {

    private List<Object> printerList = new ArrayList<>();

    /**
     * 扫描电脑连接的打印机
     */
    @PostConstruct
    public void initPrinterList() {

    }

    public void connectPrinter() {

    }

    public void printLabel() {

    }

    public void printStockLabel() {

    }

    public void printCustomerLabel() {

    }

}
