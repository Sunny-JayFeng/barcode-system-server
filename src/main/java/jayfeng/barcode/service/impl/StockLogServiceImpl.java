package jayfeng.barcode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jayfeng.barcode.bean.Stock;
import jayfeng.barcode.bean.StockLog;
import jayfeng.barcode.dao.StockDao;
import jayfeng.barcode.dao.StockLogDao;
import jayfeng.barcode.handler.QueryConditionHandler;
import jayfeng.barcode.response.ResponseData;
import jayfeng.barcode.service.StockLogService;
import jayfeng.barcode.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 库存操作日志数据业务逻辑层
 * @author JayFeng
 * @date 2021/10/21
 */
@Slf4j
@Service
public class StockLogServiceImpl implements StockLogService {

    @Autowired
    private StockLogDao stockLogDao;
    @Autowired
    private StockDao stockDao;
    @Autowired
    private QueryConditionHandler queryConditionHandler;
    @Autowired
    private StockService stockService;

    /**
     * 分页查询库存操作日志数据
     * @param requestParams 请求参数
     * @param page 分页参数
     * @return 返回
     */
    @Override
    public ResponseData findStockLogPage(Map<String, String> requestParams, Page<StockLog> page) {
        Map<String, String> queryParamsMap = new HashMap<>(4);
        // 精确匹配查询条件
        queryParamsMap.put("proMaterialNumber", requestParams.get("proMaterialNumber")); // 料号
        queryParamsMap.put("proModel", requestParams.get("proModel")); // 型号
        queryParamsMap.put("lotNumber", requestParams.get("lotNumber")); // 批号
        queryParamsMap.put("operator", requestParams.get("operator")); // 操作人
        QueryWrapper<StockLog> queryWrapper = new QueryWrapper<>();
        queryConditionHandler.handleEqualQueryCondition(queryWrapper, queryParamsMap);

        // 模糊查询条件
        queryParamsMap.clear();
        queryParamsMap.put("shipmentNumber", requestParams.get("shipmentNumber")); // 出货单号
        queryParamsMap.put("packBoxNumber", requestParams.get("packBoxNumber")); // 拼盒编号
        queryParamsMap.put("packCaseNumber", requestParams.get("packCaseNumber")); // 拼箱编号
        queryConditionHandler.handleLikeQueryCondition(queryWrapper, queryParamsMap);

        // byte 状态查询条件
        queryParamsMap.clear();
        queryParamsMap.put("type", requestParams.get("type")); // 库存类型
        queryParamsMap.put("status", requestParams.get("status")); // 库存状态
        queryParamsMap.put("operationType", requestParams.get("operationType")); // 操作类型
        queryConditionHandler.handleByteNumberQueryCondition(queryWrapper, queryParamsMap);

        Page<StockLog> dataPage = stockLogDao.selectPage(page, queryWrapper);
        // 处理数据
        List<StockLog> stockLogList = dataPage.getRecords();
        // 临时 stockId <--> stock 集合
        Map<Integer, StockLog> temp = stockLogList.stream().collect(Collectors.toMap(StockLog::getStockId, StockLog -> StockLog));
        if (temp.isEmpty()) {
            log.info("findStockLogPage 分页查询库存操作日志数据结果 total: {}， 没有数据", dataPage.getTotal());
            return ResponseData.createSuccessResponseData("findStockLogPageInfo", dataPage);
        }
        // 查找数据
        List<Stock> stockList = stockDao.selectBatchIds(temp.keySet());
        // 赋值处理 stock
        for (Stock stock : stockList) {
            temp.get(stock.getId()).setStock(stock);
        }

        log.info("findStockLogPage 分页查询库存操作日志数据结果 total: {}", dataPage.getTotal());
        return ResponseData.createSuccessResponseData("findStockLogPageInfo", dataPage);
    }

    /**
     * 库存对象转库存操作日志对象
     * @param stock 库存对象
     * @return 返回库存操作日志对象
     */
    @Override
    public StockLog stockToStockLog(Stock stock) {
        StockLog stockLog = new StockLog();
        stockLog.setStockId(stock.getId());
        stockLog.setProMaterialNumber(stock.getProMaterialNumber());
        stockLog.setProModel(stock.getProModel());
        stockLog.setLotNumber(stock.getLotNumber());
        stockLog.setAmount(stock.getAmount());
        stockLog.setType(stock.getType());
        stockLog.setGoodsOrderNumber(stock.getGoodsOrderNumber());
        stockLog.setPackBoxNumber(stock.getPackBoxNumber());
        stockLog.setPackCaseNumber(stock.getPackCaseNumber());
        return stockLog;
    }

    /**
     * 库存对象集合转库存操作日志对象集合
     * @param stockList 库存对象集合
     * @return 返回库存操作日志对象集合
     */
    @Override
    public List<StockLog> stockListToStockLogList(List<Stock> stockList) {
        List<StockLog> stockLogList = new ArrayList<>(stockList.size());
        for (Stock stock : stockList) {
            stockLogList.add(stockToStockLog(stock));
        }
        return stockLogList;
    }

}
