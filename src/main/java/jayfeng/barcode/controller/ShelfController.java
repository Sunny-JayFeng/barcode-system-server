package jayfeng.barcode.controller;

import jayfeng.barcode.bean.Shelf;
import jayfeng.barcode.response.ResponseMessage;
import jayfeng.barcode.service.ShelfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 货架数据控制层
 * @author JayFeng
 * @date 2021/10/20
 */
@Slf4j
@RestController
@RequestMapping("/barcode/shelf")
public class ShelfController extends BaseController {

    @Autowired
    private ShelfService shelfService;

    /**
     * 分页查询货架信息
     * @param requestParams 请求参数
     * @return 返回
     */
    @GetMapping("/findShelfPage")
    public ResponseMessage findShelfPage(@RequestParam Map<String, String> requestParams) {
        log.info("findShelfPage 分页查询货架数据 requestParams: {}", requestParams);
        return requestSuccess(shelfService.findShelfPage(requestParams, getPage(requestParams)));
    }

    /**
     * 根据仓库编码，查询最新的货架编号
     * @param wareCode 仓库编码
     * @return 返回
     */
    @GetMapping("/findShelfNumberByWareCode/{wareCode}")
    public ResponseMessage findShelfNumberByWareCode(@PathVariable("wareCode") String wareCode) {
        log.info("findShelfNumberByWareCode 根据仓库编码查询最新的货架编号 wareCode: {}", wareCode);
        return requestSuccess(shelfService.findShelfNumberByWareCode(wareCode));
    }

    /**
     * 添加货架信息
     * @param shelf 待添加的货架
     * @param password 当前已登录的用户的密码
     * @return 返回
     */
    @PostMapping("/addShelf/{password}")
    public ResponseMessage addShelf(@RequestBody Shelf shelf,
                                    @PathVariable("password") String password) {
        log.info("addShelf 添加货架信息 shelf: {}", shelf);
        return requestSuccess(shelfService.addShelf(shelf, password));
    }

    /**
     * @param shelf 待更新的货架
     * @param password 当前已登录的用户的密码
     * @return 返回
     */
    @PutMapping("/updateShelf/{password}")
    public ResponseMessage updateShelf(@RequestBody Shelf shelf,
                                       @PathVariable("password") String password) {
        log.info("updateShelf 更新货架信息 shelf: {}", shelf);
        return requestSuccess(shelfService.updateShelf(shelf, password));
    }

    /**
     * @param shelf 待删除的货架
     * @param password 当前已登录的用户的密码
     * @return 返回
     */
    @DeleteMapping("/deleteShelf/{password}")
    public ResponseMessage deleteShelf(@RequestBody Shelf shelf,
                                       @PathVariable("password") String password) {
        log.info("deleteShelf 删除货架信息 shelf: {}", shelf);
        return requestSuccess(shelfService.deleteShelf(shelf, password));
    }

    /**
     * 移货架
     * @param shelf 货架
     * @param targetWarehouseCode 目标仓库
     * @return 返回
     */
    @PutMapping("/moveShelf/{targetWarehouseCode}")
    public ResponseMessage moveShelf(@RequestBody Shelf shelf, @PathVariable("targetWarehouseCode") String targetWarehouseCode) {
        log.info("moveShelf 移货架 shelf: {}, targetWarehouseCode: {}", shelf, targetWarehouseCode);
        return requestSuccess(shelfService.moveShelf(shelf, targetWarehouseCode));
    }

    /**
     * 打印货架标签
     * @param shelf 货架
     * @return 返回
     */
    @PostMapping("/printShelfLabel")
    public ResponseMessage printShelfLabel(@RequestBody Shelf shelf) {
        log.info("printShelfLabel 打印货架标签 shelf: {}", shelf);
        return requestSuccess(shelfService.printShelfLabel(shelf));
    }

}
