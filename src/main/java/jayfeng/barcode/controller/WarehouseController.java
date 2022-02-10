package jayfeng.barcode.controller;

import jayfeng.barcode.bean.Warehouse;
import jayfeng.barcode.response.ResponseMessage;
import jayfeng.barcode.service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 仓库信息控制层
 * @author JayFeng
 * @date 2021/10/21
 */
@Slf4j
@RestController
@RequestMapping("/barcode/warehouse")
public class WarehouseController extends BaseController {

    @Autowired
    private WarehouseService warehouseService;

    /**
     * 分页查询仓库信息
     * @param requestParams 查询参数
     * @return 返回
     */
    @GetMapping("/findWarehousePage")
    public ResponseMessage findWarehousePage(@RequestParam Map<String, String> requestParams) {
        log.info("findWarehousePage 分页查询仓库信息 requestParams: {}", requestParams);
        return requestSuccess(warehouseService.findWarehousePage(requestParams, getPage(requestParams)));
    }

    /**
     * 查询所有仓库信息
     * @return 返回
     */
    @GetMapping("/findWarehouse")
    public ResponseMessage findWarehouse() {
        log.info("findWarehouse 查询所有仓库信息");
        return requestSuccess(warehouseService.findWarehouse());
    }

    /**
     * 添加仓库信息
     * @param warehouse 待添加的仓库信息
     * @param password 当前已登录的用户的密码
     * @return 返回
     */
    @PostMapping("/addWarehouse/{password}")
    public ResponseMessage addWarehouse(@RequestBody Warehouse warehouse,
                                        @PathVariable("password") String password) {
        log.info("addWarehouse 添加仓库信息 warehouse: {}", warehouse);
        return requestSuccess(warehouseService.addWarehouse(warehouse, password));
    }

    /**
     * 修改仓库信息
     * @param warehouse 待添加的仓库信息
     * @param password 当前已登录的用户的密码
     * @return 返回
     */
    @PutMapping("/updateWarehouse/{password}")
    public ResponseMessage updateWarehouse(@RequestBody Warehouse warehouse,
                                           @PathVariable("password") String password) {
        log.info("updateWarehouse 修改仓库信息 warehouse: {}", warehouse);
        return requestSuccess(warehouseService.updateWarehouse(warehouse, password));
    }

    /**
     * 删除仓库信息
     * @param warehouse 待添加的仓库信息
     * @param password 当前已登录的用户的密码
     * @return 返回
     */
    @DeleteMapping("/deleteWarehouse/{password}")
    public ResponseMessage deleteWarehouse(@RequestBody Warehouse warehouse,
                                           @PathVariable("password") String password) {
        log.info("deleteWarehouse 删除仓库信息 warehouse: {}", warehouse);
        return requestSuccess(warehouseService.deleteWarehouse(warehouse, password));
    }

    /**
     * 打印仓库标签
     * @param warehouse 仓库信息
     * @return 返回
     */
    @PostMapping("/printWarehouseLabel")
    public ResponseMessage printWarehouseLabel(@RequestBody Warehouse warehouse) {
        log.info("printWarehouseLabel 打印仓库标签 warehouse: {}", warehouse);
        return requestSuccess(warehouseService.printWarehouseLabel(warehouse));
    }

}
