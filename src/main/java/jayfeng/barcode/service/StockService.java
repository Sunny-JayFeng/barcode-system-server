package jayfeng.barcode.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jayfeng.barcode.bean.Stock;
import jayfeng.barcode.response.ResponseData;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.Response;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 库存数据业务逻辑层
 * @author JayFeng
 * @date 2021/10/21
 */
@Service
public interface StockService {

    /**
     * 分页查询库存数据
     * @param requestParams 请求参数
     * @param page 分页参数
     * @return 返回
     */
    ResponseData findStockPage(Map<String, String> requestParams, Page<Stock> page);

    /**
     * 库存货物扫码上架
     * @param serialNumber 扫码上架的库存的二维码信息中的序列号
     * @param shelfId 货架 id
     * @return 返回
     */
    ResponseData stockPutOnTheShelf(String serialNumber, Integer shelfId);

    /**
     * 取消库存货物扫码上架的操作
     * @param stockId 库存 id
     * @param stockLogId 操作日志 id
     * @return 返回
     */
    ResponseData cancelStockPutOnTheShelf(Integer stockId, Integer stockLogId);

    /**
     * 库存货物扫码下架
     * @param serialNumber 扫码下架的库存的二维码信息中的序列号
     * @param shelfId 货架 id
     * @return 返回
     */
    ResponseData stockRemoveFromShelf(String serialNumber, Integer shelfId);

    /**
     * 取消库存货物扫码下架的操作
     * @param shelfCode 货架编码
     * @param stockId 库存 id
     * @param stockLogId 操作日志 id
     * @return 返回
     */
    ResponseData cancelStockRemoveFromShelf(String shelfCode, Integer stockId, Integer stockLogId);

    /**
     * 货物扫码入库
     * @param qrCodeValue 扫码入库的货物的二维码信息
     * @param wareCode 检测仓仓库编码
     * @return 返回
     */
    ResponseData stockIntoWarehouse(String qrCodeValue, String wareCode);

    /**
     * 已检测的库存扫码入库
     * @param serialNumber 扫码出库的货物二维码信息中的序列号
     * @param targetWarehouseId 目标仓库id
     * @return 返回
     */
    ResponseData inspectedStockIntoWarehouse(String serialNumber, Integer targetWarehouseId);

    /**
     * 取消货物扫码入库的操作
     * @param stockId 库存 id
     * @param stockLogId 操作日志 id
     * @return 返回
     */
    ResponseData cancelStockIntoWarehouse(Integer stockId, Integer stockLogId);

    /**
     * 货物扫码出库
     * @param serialNumber 扫码出库的货物二维码信息中的序列号
     * @return 返回
     */
    ResponseData stockOutOfWarehouse(String serialNumber);

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
    ResponseData cancelStockOutOfWarehouse(Map<String, String> requestParams);

    /**
     * 库存货物变更货架
     * @param serialNumber  待变更库存的二维码信息中的序列号
     * @param targetShelfId 目标货架 id
     * @return 返回
     */
    ResponseData stockChangeShelf(String serialNumber, Integer targetShelfId);

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
    ResponseData cancelStockChangeShelf(Map<String, String> requestParams);

    /**
     * 库存货物变更仓库
     * @param serialNumber 待变更库存的二维码信息中的序列号
     * @param targetWarehouseId 目标仓库的 id
     * @return 返回
     */
    ResponseData stockChangeWarehouse(String serialNumber, Integer targetWarehouseId);

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
    ResponseData cancelStockChangeWarehouse(Map<String, String> requestParams);

    /**
     * 扫码获取库存信息，拼盒
     * 如果扫的不是袋，则返回错误，因为只有袋能拼盒
     * 这里只返回展示数据，要点击确认拼盒之后才会触发拼盒功能
     * @param serialNumber 解析二维码得到的序列号，用于获取库存信息
     * @return 返回库存信息
     */
    ResponseData scanStockToPackBox(String serialNumber);

    /**
     * 取消某个库存的拼盒操作
     * @param stockId 库存 id
     * @return 返回
     */
    ResponseData cancelTheStockPackBox(Integer stockId);

    /**
     * 点击确认拼盒
     * @param stockIds 库存 id 列表
     * @return 返回拼盒是否成功
     */
    ResponseData confirmStockPackBox(String stockIds);

    /**
     * 扫码获取库存信息，拼箱
     * 如果扫的是盒，则可以，如果扫的是袋，则返回错误
     * 因为拼箱只能由盒拼成箱
     * 这里只返回展示数据，要点击确认拼盒之后才会触发拼箱功能
     * @param serialNumber 解析二维码得到的序列号，用于获取库存信息
     * @return 返回库存信息
     */
    ResponseData scanStockToPackCase(String serialNumber);

    /**
     * 取消某个库存的拼箱操作
     * @param stockId 库存 id
     * @return 返回
     */
    ResponseData cancelTheStockPackCase(Integer stockId);

    /**
     * 点击确认拼箱
     * @param stockIds 库存 id 列表
     * @return 返回拼箱是否成功
     */
    ResponseData confirmStockPackCase(String stockIds);

    /**
     * 根据出货单号查询库存数据
     * @param goodsOrderNumber 出货单号
     * @return 返回
     */
    ResponseData findByGoodsOrderNumber(String goodsOrderNumber);

    /**
     * 移货架的时候，货架上所有库存的货架信息都必须修改
     * @param shelfCode 货架编码
     * @param newWareCode 新仓库编码
     */
    void updateStockWareCode(String shelfCode, String newWareCode);

    /**
     * 根据序列号查询库存
     * @param serialNumber 序列号
     * @return 返回库存
     */
    Stock serialNumberFindStock(String serialNumber);

    /**
     * 解析二维码获得库存对象
     * @param qrCodeValue 二维码信息
     * @return 返回
     */
    Stock parseQrCodeValueToStock(String qrCodeValue);

    /**
     * 解析二维码获得库存对象
     * @param stockFieldValue 已根据 “-” 拆分的二维码数据
     * @return 返回库存对象
     */
    Stock parseQrCodeValueToStock(String[] stockFieldValue) throws Exception;

    /**
     * 解析库存 id 列表
     * @param stockIds 库存 id 列表
     * @return 返回 list
     */
    List<Integer> parseStockIds(String stockIds);
}
