package jayfeng.barcode.controller;

import jayfeng.barcode.response.ResponseMessage;
import jayfeng.barcode.service.PackBoxStockLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 袋库存拼盒日志数据控制层
 * @author JayFeng
 * @date 2021/10/21
 */
@Slf4j
@RestController
@RequestMapping("/barcode/packBoxStockLog")
public class PackBoxStockLogController extends BaseController {

    @Autowired
    private PackBoxStockLogService packBoxStockLogService;

    /**
     * 分页查询袋库存拼盒日志数据
     * @param requestParams 请求参数
     * @return
     */
    @GetMapping("/findPackBoxStockLogPage")
    public ResponseMessage findPackBoxStockLogPage(@RequestParam Map<String, String> requestParams) {
        log.info("findPackBoxStockLogPage 分页查询袋库存拼盒日志数据 requestParams: {}", requestParams);
        return requestSuccess(packBoxStockLogService.findPackBoxStockLogPage(requestParams, getPage(requestParams)));
    }

    /**
     * 根据拼盒编号查询拼盒日志数据
     * @param packBoxNumber 拼盒编号
     * @return 返回
     */
    @GetMapping("/findByPackBoxNumber/{packBoxNumber}")
    public ResponseMessage findByPackBoxNumber(@PathVariable("packBoxNumber") String packBoxNumber) {
        log.info("findByPackBoxNumber 根据拼盒编号查询拼盒数据 packBoxNumber: {}", packBoxNumber);
        return requestSuccess(packBoxStockLogService.findByPackBoxNumber(packBoxNumber));
    }

}
