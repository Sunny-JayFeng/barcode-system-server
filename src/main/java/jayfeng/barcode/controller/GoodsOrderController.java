package jayfeng.barcode.controller;

import jayfeng.barcode.response.ResponseMessage;
import jayfeng.barcode.service.GoodsOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 出货单控制层
 * @author JayFeng
 * @date 2021/11/5
 */
@Slf4j
@RestController
@RequestMapping("/barcode/goodsOrder")
public class GoodsOrderController extends BaseController {

    @Autowired
    private GoodsOrderService goodsOrderService;

    /**
     * 分页查询出货单信息
     * @param requestParams 请求参数
     * @return 返回
     */
    @GetMapping("/findGoodsOrderPage")
    public ResponseMessage findGoodsOrderPage(@RequestParam Map<String, String> requestParams) {
        log.info("findGoodsOrderPage 分页查询出货单信息 requestParams: {}", requestParams);
        return requestSuccess(goodsOrderService.findGoodsOrderPage(requestParams, getPage(requestParams)));
    }

    /**
     * 获取库存用于创建出货单
     * @param requestParams
     * String proMaterialNumber 料号
     * String proModel 型号
     * String lotNumber 批号-可选
     * @return 返回
     */
    @GetMapping("/findStockToCreateOrder")
    public ResponseMessage findStockToCreateOrder(@RequestParam Map<String, String> requestParams) {
        log.info("findStockToCreateOrder 查找库存信息用于生成出货单 requestParams: {}", requestParams);
        return requestSuccess(goodsOrderService.findStockToCreateOrder(requestParams));
    }

    /**
     * 库存加锁，用于创建出货单
     * @param stockId 库存 id
     * @return 返回
     */
    @PostMapping("/lockStockToCreateOrder/{stockId}")
    public ResponseMessage lockStockToCreateOrder(@PathVariable("stockId") Integer stockId) {
        log.info("lockStockToCreateOrder 构建出货单过程, 库存加锁 stockId: {}", stockId);
        return requestSuccess(goodsOrderService.lockStockToCreateOrder(stockId));
    }

    /**
     * 库存释放锁，取消创建出货单
     * @param stockId 库存 id
     * @return 返回
     */
    @PostMapping("/unLockStockOfCreateOrder/{stockId}")
    public ResponseMessage unLockStockOfCreateOrder(@PathVariable("stockId") Integer stockId) {
        log.info("unLockStockOfCreateOrder 构建出货单过程，取消勾选，释放锁 stockId: {}", stockId);
        return requestSuccess(goodsOrderService.unLockStockOfCreateOrder(stockId));
    }

    /**
     * 没有点击确认生成出货单就退出页面 -- 释放所有锁
     * @param stockIds 库存 id 列表
     * @return 返回
     */
    @PutMapping("/unLockGoodsOrderStock")
    public ResponseMessage unLockGoodsOrderStock(@RequestBody String stockIds) {
        log.info("unLockGoodsOrderStock 取消构建出货单，释放所有锁 stockIds: {}", stockIds);
        return requestSuccess(goodsOrderService.unLockGoodsOrderStock(stockIds));
    }

    /**
     * 生成出货单
     * @param requestParams 请求参数
     * String customerName 客户名称
     * String proMaterialNumber 料号
     * String proModel 型号
     * Integer amount 出货数量
     * @return 返回
     */
    @PostMapping("/createGoodsOrder")
    public ResponseMessage createGoodsOrder(@RequestParam Map<String, String> requestParams) {
        log.info("createGoodsOrder 生成出货单: {}", requestParams);
        return requestSuccess(goodsOrderService.createGoodsOrder(requestParams));
    }

    /**
     * 确认出货
     * @param goodsOrderId 出货单 id
     * @param stockIds 库存 id 列表
     * @return 返回
     */
    @PutMapping("/confirmOutStock/{goodsOrderId}")
    public ResponseMessage confirmOutStock(@RequestBody String stockIds, @PathVariable("goodsOrderId") Integer goodsOrderId) {
        log.info("confirmOutStock 确认出货单出货 stockIds: {}, goodsOrderId: {}", stockIds, goodsOrderId);
        return requestSuccess(goodsOrderService.confirmOutStock(goodsOrderId, stockIds));
    }

}
