package jayfeng.barcode.controller;

import jayfeng.barcode.bean.Stock;
import jayfeng.barcode.bean.bo.StockLabelBo;
import jayfeng.barcode.response.ResponseMessage;
import jayfeng.barcode.service.StockLabelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存标签控制层
 * @author JayFeng
 * @date 2021/11/8
 */
@Slf4j
@RestController
@RequestMapping("/barcode/stockLabel")
public class StockLabelController extends BaseController {

    @Autowired
    private StockLabelService stockLabelService;

    /**
     * 打印库存标签：袋、盒、箱
     * @param stockLabelBo 库存标签信息
     * @return 返回
     */
    @PostMapping("/printStockLabel")
    public ResponseMessage printStockLabel(@RequestBody StockLabelBo stockLabelBo) {
        log.info("printStockLabel  打印库存标签 stockLabelBo: {}", stockLabelBo);
        return requestSuccess(stockLabelService.printStockLabel(stockLabelBo));
    }

}
