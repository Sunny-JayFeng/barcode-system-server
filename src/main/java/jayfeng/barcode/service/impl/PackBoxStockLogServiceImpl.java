package jayfeng.barcode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jayfeng.barcode.bean.PackBoxStockLog;
import jayfeng.barcode.bean.Stock;
import jayfeng.barcode.constant.ResponseFailTypeConstant;
import jayfeng.barcode.dao.PackBoxStockLogDao;
import jayfeng.barcode.handler.QueryConditionHandler;
import jayfeng.barcode.response.ResponseData;
import jayfeng.barcode.service.PackBoxStockLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 袋库存拼盒日志数据业务逻辑层
 * @author JayFeng
 * @date 2021/10/21
 */
@Slf4j
@Service
public class PackBoxStockLogServiceImpl implements PackBoxStockLogService {

    @Autowired
    private PackBoxStockLogDao packBoxStockLogDao;
    @Autowired
    private QueryConditionHandler queryConditionHandler;

    /**
     * 分页查询袋库存拼盒日志数据
     * @param requestParams 请求参数
     * @param page 分页参数
     * @return 返回
     */
    @Override
    public ResponseData findPackBoxStockLogPage(Map<String, String> requestParams, Page<PackBoxStockLog> page) {
        Map<String, String> queryParamsMap = new HashMap<>(8);
        // 精确匹配查询条件
        queryParamsMap.put("proMaterialNumber", requestParams.get("proMaterialNumber")); // 料号
        queryParamsMap.put("proModel", requestParams.get("proModel")); // 型号
        queryParamsMap.put("lotNumber", requestParams.get("lotNumber")); // 批号
        QueryWrapper<PackBoxStockLog> queryWrapper = new QueryWrapper<>();
        queryConditionHandler.handleEqualQueryCondition(queryWrapper, queryParamsMap);

        // 模糊查询条件
        queryParamsMap.clear();
        queryParamsMap.put("shipmentNumber", requestParams.get("shipmentNumber")); // 出货单号
        queryParamsMap.put("packBoxNumber", requestParams.get("packBoxNumber")); // 拼盒编号
        queryConditionHandler.handleLikeQueryCondition(queryWrapper, queryParamsMap);

        // byte 状态查询条件
        queryParamsMap.clear();
        queryParamsMap.put("type", requestParams.get("type")); // 库存类型
        queryParamsMap.put("status", requestParams.get("status")); // 库存状态
        queryConditionHandler.handleByteNumberQueryCondition(queryWrapper, queryParamsMap);

        Page<PackBoxStockLog> dataPage = packBoxStockLogDao.selectPage(page, queryWrapper);
        log.info("findPackBoxStockLogPage 分页查询袋库存拼盒日志数据结果 total: {}", dataPage.getTotal());
        return ResponseData.createSuccessResponseData("findPackBoxStockLogPageInfo", dataPage);
    }

    /**
     * 通过 stockId 查询袋库存拼盒日志数据
     * @param stockIdList stockId 集合
     * @return 返回数据
     */
    @Override
    public List<PackBoxStockLog> findByStockIdList(Set<Integer> stockIdList) {
        log.info("findByStockIdList 通过 stockId 查询袋库存拼盒日志数据 stockIdList: {}", stockIdList);
        List<PackBoxStockLog> result = packBoxStockLogDao.selectBatchIds(stockIdList);
        log.info("findByStockIdList 查询袋库存拼盒日志数据结果 size: {}", result.size());
        return result;
    }

    /**
     * 库存对象转库存拼盒日志对象
     * @param stock 库存对象
     * @param packBoxNumber 拼盒编码
     * @return 返回库存拼盒日志对象
     */
    public PackBoxStockLog stockToPackBoxStockLog(Stock stock, String packBoxNumber) {
        PackBoxStockLog packBoxStockLog = new PackBoxStockLog();
        packBoxStockLog.setStockId(stock.getId());
        packBoxStockLog.setProMaterialNumber(stock.getProMaterialNumber());
        packBoxStockLog.setProModel(stock.getProModel());
        packBoxStockLog.setLotNumber(stock.getLotNumber());
        packBoxStockLog.setAmount(stock.getAmount());
        packBoxStockLog.setType(stock.getType());
        packBoxStockLog.setPackBoxNumber(packBoxNumber);
        packBoxStockLog.setWareCode(stock.getWareCode());
        packBoxStockLog.setWareName(stock.getWareName());
        packBoxStockLog.setShelfCode(stock.getShelfCode());
        packBoxStockLog.setCreateTime(LocalDateTime.now());
        packBoxStockLog.setUpdateTime(LocalDateTime.now());
        log.info("stockToPackBoxStockLog 库存对象转库存拼盒日志对象 packBoxStockLog: {}", packBoxStockLog);
        return packBoxStockLog;
    }

    /**
     * 根据拼盒编号查询拼盒日志数据
     * @param packBoxNumber 拼盒编号
     * @return 返回
     */
    @Override
    public ResponseData findByPackBoxNumber(String packBoxNumber) {
        if (ObjectUtils.isEmpty(packBoxNumber)) {
            log.info("findByPackBoxNumber 拼盒日志数据查找失败，拼盒编号为空");
            return ResponseData.createFailResponseData("findByPackBoxNumberInfo", "数据查找失败，拼盒编号有误", ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }
        try {
            Long.parseLong(packBoxNumber);
        } catch (NumberFormatException e) {
            log.info("findByPackBoxNumber 拼盒日志数据查找失败，拼盒编号格式错误 packBoxNumber: {}", packBoxNumber);
            return ResponseData.createFailResponseData("findByPackBoxNumberInfo", "数据查找失败，拼盒编号有误", ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }
        List<PackBoxStockLog> resultList = packBoxStockLogDao.findByPackBoxNumber(packBoxNumber);
        log.info("findByPackBoxNumber 拼盒日志数据查找结果 packBoxNumber: {},  size: {}", packBoxNumber, resultList.size());
        return ResponseData.createSuccessResponseData("findByPackBoxNumberInfo", resultList);
    }

}
