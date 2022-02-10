package jayfeng.barcode.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jayfeng.barcode.bean.Stock;
import jayfeng.barcode.bean.StockLog;
import jayfeng.barcode.response.ResponseData;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 库存操作日志数据业务逻辑层
 * @author JayFeng
 * @date 2021/10/21
 */
@Service
public interface StockLogService {

    /**
     * 分页查询库存操作日志数据
     * @param requestParams 请求参数
     * @param page 分页参数
     * @return 返回
     */
    ResponseData findStockLogPage(Map<String, String> requestParams, Page<StockLog> page);

    /**
     * 库存对象转库存操作日志对象
     * @param stock 库存对象
     * @return 返回库存操作日志对象
     */
    StockLog stockToStockLog(Stock stock);

    /**
     * 库存对象集合转库存操作日志对象集合
     * @param stockList 库存对象集合
     * @return 返回库存操作日志对象集合
     */
    List<StockLog> stockListToStockLogList(List<Stock> stockList);

}
