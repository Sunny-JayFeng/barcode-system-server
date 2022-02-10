package jayfeng.barcode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jayfeng.barcode.bean.InventoryLog;
import jayfeng.barcode.bean.Stock;
import jayfeng.barcode.bean.StockLog;
import jayfeng.barcode.constant.ResponseFailTypeConstant;
import jayfeng.barcode.dao.InventoryLogDao;
import jayfeng.barcode.handler.QueryConditionHandler;
import jayfeng.barcode.response.ResponseData;
import jayfeng.barcode.service.InventoryLogService;
import jayfeng.barcode.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 盘点日志数据业务逻辑层
 * @author JayFeng
 * @date 2021/11/2
 */
@Slf4j
@Service
public class InventoryLogServiceImpl implements InventoryLogService {

    @Autowired
    private InventoryLogDao inventoryLogDao;
    @Autowired
    private StockService stockService;
    @Autowired
    private QueryConditionHandler queryConditionHandler;

    /**
     * 分页查询盘点日志数据
     * @param requestParams 请求参数
     * @param page 页参数
     * @return 返回
     */
    @Override
    public ResponseData findInventoryLogPage(Map<String, String> requestParams, Page<InventoryLog> page) {
        QueryWrapper<InventoryLog> queryWrapper = new QueryWrapper<>();
        Map<String, String> queryParams = new HashMap<>(8);
        queryParams.put("proMaterialNumber", requestParams.get("proMaterialNumber"));
        queryParams.put("proModel", requestParams.get("proModel"));
        queryParams.put("lotNumber", requestParams.get("lotNumber"));
        queryParams.put("wareCode", requestParams.get("wareCode"));
        queryParams.put("shelfCode", requestParams.get("shelfCode"));
        queryConditionHandler.handleEqualQueryCondition(queryWrapper, queryParams);

        queryParams.clear();
        queryParams.put("different", requestParams.get("different"));
        queryConditionHandler.handleByteNumberQueryCondition(queryWrapper, queryParams);

        Page<InventoryLog> dataPage = inventoryLogDao.selectPage(page, queryWrapper);
        log.info("findInventoryLogPage 分页查询盘点数据结果 total: {}", dataPage.getTotal());
        return ResponseData.createSuccessResponseData("findInventoryLogPageInfo", dataPage);
    }

    /**
     * 扫码盘点
     * @param qrCodeValue 二维码信息
     * @return 返回
     */
    @Override
    public ResponseData scanQrCodeInventory(String qrCodeValue) {
        String[] stockFieldValue = qrCodeValue.split("-");
        Stock parseStock = null;
        try {
            parseStock = stockService.parseQrCodeValueToStock(stockFieldValue); // 解析二维码得到的库存
        } catch (Exception e) {
            log.info("scanQrCodeInventory 扫码盘点失败，二维码数据有误 qrCodeValue: {}", qrCodeValue);
            return ResponseData.createFailResponseData("scanQrCodeInventoryInfo", "扫码盘点失败，二维码数据有误", ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }
        String serialNumber = stockFieldValue[5];
        Stock stock = stockService.serialNumberFindStock(serialNumber); // 实际库存
        if (stock.getProMaterialNumber().equals(parseStock.getProMaterialNumber()) && // 料号必须一致
            stock.getProModel().equals(parseStock.getProModel()) &&  // 型号必须一致
            stock.getLotNumber().equals(parseStock.getLotNumber())) { // 批号必须一致
            // 生成盘点日志对象
            InventoryLog inventoryLog = stockToCreateInventoryLog(stock);
            inventoryLog.setRealAmount(stock.getAmount()); // 实际数量
            inventoryLog.setQrCodeAmount(parseStock.getAmount()); // 标签二维码上的数量
            inventoryLog.setAmountDifference(stock.getAmount() - parseStock.getAmount()); // 数量差异
            if (parseStock.getAmount().equals(stock.getAmount())) {
                inventoryLog.setDifferent(NO_DIFFERENCE); // 没差异
            } else {
                inventoryLog.setDifferent(DIFFERENCE); // 有差异
            }
            log.info("scanQrCodeInventory 扫码盘点成功 inventoryLog: {}", inventoryLog);
            return ResponseData.createSuccessResponseData("scanQrCodeInventoryInfo", "盘点成功");
        } else {
            log.info("scanQrCodeInventory 扫码盘点失败，解析得到的数据：parseStock: {}, 数据库中的数据：stock: {}", parseStock, stock);
            return ResponseData.createFailResponseData("scanQrCodeInventoryInfo", "扫码盘点失败，二维码数据跟库存数据不匹配", ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }
    }

    /**
     * 根据库存对象生成盘点日志对象
     * @param stock 库存对象
     * @return 返回盘点日志对象
     */
    private InventoryLog stockToCreateInventoryLog(Stock stock) {
        InventoryLog inventoryLog = new InventoryLog();
        inventoryLog.setStockId(stock.getId());
        inventoryLog.setProMaterialNumber(stock.getProMaterialNumber());
        inventoryLog.setProModel(stock.getProModel());
        inventoryLog.setLotNumber(stock.getLotNumber());
        inventoryLog.setWareCode(stock.getWareCode());
        inventoryLog.setWareName(stock.getWareName());
        inventoryLog.setShelfCode(stock.getShelfCode());
        inventoryLog.setSerialNumber(stock.getSerialNumber());
        inventoryLog.setCreateTime(LocalDateTime.now());
        inventoryLog.setUpdateTime(LocalDateTime.now());
        return inventoryLog;
    }

}
