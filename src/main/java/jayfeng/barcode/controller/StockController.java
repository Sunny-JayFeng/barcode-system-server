package jayfeng.barcode.controller;

import jayfeng.barcode.response.ResponseMessage;
import jayfeng.barcode.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 库存数据控制层
 * @author JayFeng
 * @date 2021/10/21
 */
@Slf4j
@RestController
@RequestMapping("/barcode/stock")
public class StockController extends BaseController {

    @Autowired
    private StockService stockService;

    /**
     * 分页查询库存数据
     * @param requestParams 请求参数
     * @return 返回
     */
    @GetMapping("/findStockPage")
    public ResponseMessage findStockPage(@RequestParam Map<String, String> requestParams) {
        log.info("findStockPage 分页查询库存数据 requestParams: {}", requestParams);
        return requestSuccess(stockService.findStockPage(requestParams, getPage(requestParams)));
    }

    /**
     * 库存货物扫码上架
     * @param serialNumber 扫码上架的库存的二维码信息中的序列号
     * @param shelfId 货架 id
     * @return 返回
     */
    @PutMapping("/stockPutOnTheShelf/{shelfId}")
    public ResponseMessage stockPutOnTheShelf(@RequestParam String serialNumber,
                                              @PathVariable("shelfId") Integer shelfId) {
        log.info("stockPutOnTheShelf 库存货物扫码上架 shelfId: {}, serialNumber: {}", shelfId, serialNumber);
        return requestSuccess(stockService.stockPutOnTheShelf(serialNumber, shelfId));
    }

    /**
     * 取消库存货物扫码上架的操作
     * @param stockId 库存 id
     * @param stockLogId 操作日志 id
     * @return 返回
     */
    @PutMapping("/cancelStockPutOnTheShelf/{stockId}/{stockLogId}")
    public ResponseMessage cancelStockPutOnTheShelf(@PathVariable("stockId") Integer stockId, @PathVariable("stockLogId") Integer stockLogId) {
        log.info("cancelStockPutOnTheShelf 取消库存货物扫码上架的操作 stockId: {}, stockLogId: {}", stockId, stockLogId);
        return requestSuccess(stockService.cancelStockPutOnTheShelf(stockId, stockLogId));
    }

    /**
     * 库存货物扫码下架
     * @param serialNumber 扫码下架的库存的二维码信息中的序列号
     * @param shelfId 货架 id
     * @return 返回
     */
    @PutMapping("/stockRemoveFromShelf/{shelfId}")
    public ResponseMessage stockRemoveFromShelf(@RequestParam String serialNumber,
                                                @PathVariable("shelfId") Integer shelfId) {
        log.info("stockRemoveFromShelf 库存货物扫码下架 shelfId: {}, serialNumber: {}", shelfId, serialNumber);
        return requestSuccess(stockService.stockRemoveFromShelf(serialNumber, shelfId));
    }

    /**
     * 取消库存货物扫码下架的操作
     * @param shelfCode 货架编码
     * @param stockId 库存 id
     * @param stockLogId 操作日志 id
     * @return 返回
     */
    @PutMapping("/cancelStockRemoveFromShelf/{shelfCode}/{stockId}/{stockLogId}")
    public ResponseMessage cancelStockRemoveFromShelf(@PathVariable("shelfCode") String shelfCode,
                                                      @PathVariable("stockId") Integer stockId,
                                                      @PathVariable("stockLogId") Integer stockLogId) {
        log.info("cancelStockRemoveFromShelf 取消库存货物扫码下架的操作 shelfCode: {}, stockId: {}, stockLogId: {}", shelfCode, stockId, stockLogId);
        return requestSuccess(stockService.cancelStockRemoveFromShelf(shelfCode, stockId, stockLogId));
    }

    /**
     * 货物扫码入库
     * @param qrCodeValue 扫码入库的货物的二维码信息
     * @return 返回
     */
    @PostMapping("/stockIntoWarehouse/{wareCode}")
    public ResponseMessage stockIntoWarehouse(@RequestParam String qrCodeValue, @PathVariable("wareCode") String wareCode) {
        log.info("stockIntoWarehouse 货物扫码入库 qrCodeValue: {}, wareCode: {}", qrCodeValue, wareCode);
        return requestSuccess(stockService.stockIntoWarehouse(qrCodeValue, wareCode));
    }

    /**
     * 已检测的库存扫码入库
     * @param serialNumber 扫码出库的货物二维码信息中的序列号
     * @param targetWarehouseId 目标仓库id
     * @return 返回
     */
    @PutMapping("/inspectedStockIntoWarehouse/{targetWarehouseId}")
    public ResponseMessage inspectedStockIntoWarehouse(@RequestParam String serialNumber, @PathVariable("targetWarehouseId") Integer targetWarehouseId) {
        log.info("inspectedStockIntoWarehouse 已检测的库存扫码入库 serialNumber: {}, targetWarehouseId: {}", serialNumber, targetWarehouseId);
        return requestSuccess(stockService.inspectedStockIntoWarehouse(serialNumber, targetWarehouseId));
    }

    /**
     * 取消货物扫码入库的操作
     * @param stockId 库存 id
     * @param stockLogId 操作日志 id
     * @return 返回
     */
    @PutMapping("/cancelStockIntoWarehouse/{stockId}/{stockLogId}")
    public ResponseMessage cancelStockIntoWarehouse(@PathVariable("stockId") Integer stockId, @PathVariable("stockLogId") Integer stockLogId) {
        log.info("cancelStockIntoWarehouse 取消货物扫码入库的操作 stockId: {}, stockLogId: {}", stockId, stockLogId);
        return requestSuccess(stockService.cancelStockIntoWarehouse(stockId, stockLogId));
    }

    /**
     * 货物扫码出库
     * @param serialNumber 扫码出库的货物二维码信息中的序列号
     * @return 返回
     */
    @PostMapping("/stockOutOfWarehouse")
    public ResponseMessage stockOutOfWarehouse(@RequestParam String serialNumber) {
        log.info("stockOutOfWarehouse 货物扫码出库 serialNumber: {}", serialNumber);
        return requestSuccess(stockService.stockOutOfWarehouse(serialNumber));
    }

    /**
     * 取消货物扫码出库的操作
     * @param requestParams
     * String originShelfCode, 原来的货架编码
     * String originWareName, 原来的仓库名称
     * String originWareCode, 原来的仓库编码
     * Integer stockId, 库存 id
     * Integer stockLogId 操作日志 id
     * @return 返回
     */
    @PutMapping("/cancelStockOutOfWarehouse")
    public ResponseMessage cancelStockOutOfWarehouse(@RequestBody Map<String, String> requestParams) {
        log.info("cancelStockOutOfWarehouse 取消货物扫码出库的操作 requestParams: {}", requestParams);
        return requestSuccess(stockService.cancelStockOutOfWarehouse(requestParams));
    }

    /**
     * 库存货物变更货架
     * @param serialNumber  待变更库存的二维码信息中的序列号
     * @param targetShelfId 目标货架 id
     * @return 返回
     */
    @PutMapping("/stockChangeShelf/{targetShelfId}")
    public ResponseMessage stockChangeShelf(@RequestParam String serialNumber, @PathVariable("targetShelfId") Integer targetShelfId) {
        log.info("stockChangeShelf 库存货物变更货架，库存序列号serialNumber: {}, targetShelfId: {}", serialNumber, targetShelfId);
        return requestSuccess(stockService.stockChangeShelf(serialNumber, targetShelfId));
    }

    /**
     * 取消库存货物变更货架的操作
     * @param requestParams
     * String originShelfCode, 原来的货架编码
     * String originWareName, 原来的仓库名称
     * String originWareCode, 原来的仓库编码
     * Integer stockId, 库存 id
     * Integer stockLogId 操作日志 id
     * @return 返回
     */
    @PutMapping("/cancelStockChangeShelf")
    public ResponseMessage cancelStockChangeShelf(@RequestBody Map<String, String> requestParams) {
        log.info("cancelStockChangeShelf 取消库存货物变更货架的操作 requestParams: {}", requestParams);
        return requestSuccess(stockService.cancelStockChangeShelf(requestParams));
    }

    /**
     * 库存货物变更仓库
     * @param serialNumber 待变更库存的二维码信息中的序列号
     * @param targetWarehouseId 目标仓库的 id
     * @return 返回
     */
    @PutMapping("/stockChangeWarehouse/{targetWarehouseId}")
    public ResponseMessage stockChangeWarehouse(@RequestParam String serialNumber, @PathVariable("targetWarehouseId") Integer targetWarehouseId) {
        log.info("stockChangeWarehouse 库存货物变更仓库，库存序列号serialNumber: {}, targetWarehouseId: {}", serialNumber, targetWarehouseId);
        return requestSuccess(stockService.stockChangeWarehouse(serialNumber, targetWarehouseId));
    }

    /**
     * 取消库存货物变更仓库的操作
     * @param requestParams
     * String originShelfCode, 原来的货架编码
     * String originWareName, 原来的仓库名称
     * String originWareCode, 原来的仓库编码
     * Integer stockId, 库存 id
     * Integer stockLogId 操作日志 id
     * @return 返回
     */
    @PutMapping("/cancelStockChangeWarehouse")
    public ResponseMessage cancelStockChangeWarehouse(@RequestBody Map<String, String> requestParams) {
        log.info("cancelStockChangeWarehouse 取消库存货物变更仓库的操作 requestParams: {}", requestParams);
        return requestSuccess(stockService.cancelStockChangeWarehouse(requestParams));
    }

    /**
     * 扫码获取库存信息，拼盒
     * 如果扫的不是袋，则返回错误，因为只有袋能拼盒
     * 这里只返回展示数据，要点击确认拼盒之后才会触发拼盒功能
     * @param serialNumber 解析二维码得到的序列号，用于获取库存信息
     * @return 返回库存信息
     */
    @GetMapping("/scanStockToPackBox/{serialNumber}")
    public ResponseMessage scanStockToPackBox(@PathVariable("serialNumber") String serialNumber) {
        log.info("scanStockToPackBox 拼盒操作，扫码获取库存信息 serialNumber: {}", serialNumber);
        return requestSuccess(stockService.scanStockToPackBox(serialNumber));
    }

    /**
     * 取消某个库存的拼盒操作
     * @param stockId 库存 id
     * @return 返回
     */
    @PutMapping("/cancelTheStockPackBox/{stockId}")
    public ResponseMessage cancelTheStockPackBox(@PathVariable("stockId") Integer stockId) {
        log.info("cancelTheStockPackBox 取消某个库存的拼盒操作 stockId: {}", stockId);
        return requestSuccess(stockService.cancelTheStockPackBox(stockId));
    }

    /**
     * 点击确认拼盒
     * @param stockIds 库存 id 列表
     * @return 返回拼盒是否成功
     */
    @PostMapping("/confirmStockPackBox")
    public ResponseMessage confirmStockPackBox(@RequestParam String stockIds) {
        log.info("confirmStockPackBox 确认拼盒 stockIds: {}", stockIds);
        return requestSuccess(stockService.confirmStockPackBox(stockIds));
    }

    /**
     * 扫码获取库存信息，拼箱
     * 如果扫的是盒，则可以，如果扫的是袋，则返回错误
     * 因为拼箱只能由盒拼成箱
     * 这里只返回展示数据，要点击确认拼盒之后才会触发拼箱功能
     * @param serialNumber 解析二维码得到的序列号，用于获取库存信息
     * @return 返回库存信息
     */
    @GetMapping("/scanStockToPackCase/{serialNumber}")
    public ResponseMessage scanStockToPackCase(@PathVariable("serialNumber") String serialNumber) {
        log.info("scanStockToPackCase 拼箱操作，扫码获取库存信息 serialNumber: {}", serialNumber);
        return requestSuccess(stockService.scanStockToPackCase(serialNumber));
    }

    /**
     * 取消某个库存的拼箱操作
     * @param stockId 库存 id
     * @return 返回
     */
    @PutMapping("/cancelTheStockPackCase/{stockId}")
    public ResponseMessage cancelTheStockPackCase(@PathVariable("stockId") Integer stockId) {
        log.info("cancelTheStockPackCase 取消某个库存的拼箱操作 stockId: {}", stockId);
        return requestSuccess(stockService.cancelTheStockPackCase(stockId));
    }

    /**
     * 点击确认拼箱
     * @param stockIds 库存 id 列表
     * @return 返回拼箱是否成功
     */
    @PostMapping("/confirmStockPackCase")
    public ResponseMessage confirmStockPackCase(@RequestParam String stockIds) {
        log.info("confirmStockPackCase 确认拼箱 stockIds: {}", stockIds);
        return requestSuccess(stockService.confirmStockPackCase(stockIds));
    }

    /**
     * 根据出货单号查询库存数据
     * @param goodsOrderNumber 出货单号
     * @return 返回
     */
    @GetMapping("/findByGoodsOrderNumber")
    public ResponseMessage findByGoodsOrderNumber(@RequestParam String goodsOrderNumber) {
        log.info("findByGoodsOrderNumber 根据出货单号查询出货数据明细 goodsOrderNumber: {}", goodsOrderNumber);
        return requestSuccess(stockService.findByGoodsOrderNumber(goodsOrderNumber));
    }

}
