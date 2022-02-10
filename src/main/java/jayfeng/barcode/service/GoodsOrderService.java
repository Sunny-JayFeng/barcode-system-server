package jayfeng.barcode.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jayfeng.barcode.bean.GoodsOrder;
import jayfeng.barcode.response.ResponseData;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 出货单业务逻辑层
 * @author JayFeng
 * @date 2021/11/5
 */
@Service
public interface GoodsOrderService {

    Byte WAIT_OUT = 0;
    Byte ALREADY_OUT = 1;

    /**
     * 分页查询出货单信息
     * @param requestParams 请求参数
     * @param page 分页参数
     * @return 返回
     */
    ResponseData findGoodsOrderPage(Map<String, String> requestParams, Page<GoodsOrder> page);

    /**
     * 获取库存用于创建出货单
     * @param requestParams
     * String proMaterialNumber 料号
     * String proModel 型号
     * String lotNumber 批号-可选
     * @return 返回
     */
    ResponseData findStockToCreateOrder(Map<String, String> requestParams);

    /**
     * 库存加锁，用于创建出货单
     * @param stockId 库存 id
     * @return 返回
     */
    ResponseData lockStockToCreateOrder(Integer stockId);

    /**
     * 库存释放锁，取消创建出货单
     * @param stockId 库存 id

     * @return 返回
     */
    ResponseData unLockStockOfCreateOrder(Integer stockId);

    /**
     * 超时，释放所有锁
     * @param stockIds 库存 id 列表
     * @return 返回
     */
    ResponseData unLockGoodsOrderStock(String stockIds);

    /**
     * 生成出货单
     * @param requestParams 请求参数
     * String customerName 客户名称
     * String proMaterialNumber 料号
     * String proModel 型号
     * String stockIds 锁定的库存 id 列表
     * Integer amount 出货数量
     * @return 返回
     */
    ResponseData createGoodsOrder(Map<String, String> requestParams);

    /**
     * 确认出货
     * @param goodsOrderId 出货单 id
     * @param stockIds 库存 id 列表
     * @return 返回
     */
    ResponseData confirmOutStock(Integer goodsOrderId, String stockIds);

}
