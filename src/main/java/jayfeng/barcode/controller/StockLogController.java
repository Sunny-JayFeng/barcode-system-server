package jayfeng.barcode.controller;

import jayfeng.barcode.bean.StockLog;
import jayfeng.barcode.response.ResponseMessage;
import jayfeng.barcode.service.StockLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 库存操作日志数据控制层
 * @author JayFeng
 * @date 2021/10/21
 */
@Slf4j
@RestController
@RequestMapping("/barcode/stockLog")
public class StockLogController extends BaseController {

    @Autowired
    private StockLogService stockLogService;

    /**
     * 分页查询库存操作日志数据
     * @param requestParams 请求参数
     * @return 返回
     */
    @GetMapping("/findStockLogPage")
    public ResponseMessage findStockLogPage(@RequestParam Map<String, String> requestParams) {
        log.info("findStockLogPage 分页查询库存操作日志数据 requestParams: {}", requestParams);
        return requestSuccess(stockLogService.findStockLogPage(requestParams, getPage(requestParams)));
    }

}
