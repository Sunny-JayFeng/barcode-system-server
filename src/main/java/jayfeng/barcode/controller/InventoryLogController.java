package jayfeng.barcode.controller;

import jayfeng.barcode.response.ResponseMessage;
import jayfeng.barcode.service.InventoryLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 盘点日志控制层
 * @author JayFeng
 * @date 2021/11/2
 */
@Slf4j
@RestController
@RequestMapping("/barcode/inventoryLog")
public class InventoryLogController extends BaseController {

    @Autowired
    private InventoryLogService inventoryLogService;

    /**
     * 分页查询盘点日志数据
     * @param requestParams 请求参数
     * @return 返回
     */
    @GetMapping("/findInventoryLogPage")
    public ResponseMessage findInventoryLogPage(@RequestParam Map<String, String> requestParams) {
        log.info("findInventoryLogPage 分页查询盘点数据 requestParams: {}", requestParams);
        return requestSuccess(inventoryLogService.findInventoryLogPage(requestParams, getPage(requestParams)));
    }

    /**
     * 扫码盘点
     * @param qrCodeValue 二维码信息
     * @return 返回
     */
    @PostMapping("/scanQrCodeInventory")
    public ResponseMessage scanQrCodeInventory(@RequestParam String qrCodeValue) {
        log.info("scanQrCodeInventory 扫码盘点 qrCodeValue: {}", qrCodeValue);
        return requestSuccess(inventoryLogService.scanQrCodeInventory(qrCodeValue));
    }

}
