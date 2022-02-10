package jayfeng.barcode.controller;

import jayfeng.barcode.response.ResponseMessage;
import jayfeng.barcode.service.PackCaseStockLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 盒库存拼箱日志数据控制层
 * @author JayFeng
 * @date 2021/10/21
 */
@Slf4j
@RestController
@RequestMapping("/barcode/packCaseStockLog")
public class PackCaseStockLogController extends BaseController {

    @Autowired
    private PackCaseStockLogService packCaseStockLogService;

    /**
     * 分页查询盒库存拼箱日志数据
     * @param requestParams 请求参数
     * @return
     */
    @GetMapping("/findPackCaseStockLogPage")
    public ResponseMessage findPackCaseStockLogPage(@RequestParam Map<String, String> requestParams) {
        log.info("findPackCaseStockLogPage 分页查询盒库存拼箱日志数据 requestParams: {}", requestParams);
        return requestSuccess(packCaseStockLogService.findPackCaseStockLogPage(requestParams, getPage(requestParams)));
    }

    /**
     * 根据拼箱编号查询拼箱日志数据
     * @param packCaseNumber 拼箱编号
     * @return 返回
     */
    @GetMapping("/findByPackCaseNumber/{packCaseNumber}")
    public ResponseMessage findByPackCaseNumber(@PathVariable("packCaseNumber") String packCaseNumber) {
        log.info("findByPackCaseNumber 根据拼箱编号查询拼箱数据 packCaseNumber: {}", packCaseNumber);
        return requestSuccess(packCaseStockLogService.findByPackCaseNumber(packCaseNumber));
    }

}
