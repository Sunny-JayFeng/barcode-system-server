package jayfeng.barcode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jayfeng.barcode.bean.PackCaseStockLog;
import jayfeng.barcode.bean.Stock;
import jayfeng.barcode.constant.ResponseFailTypeConstant;
import jayfeng.barcode.dao.PackCaseStockLogDao;
import jayfeng.barcode.handler.QueryConditionHandler;
import jayfeng.barcode.response.ResponseData;
import jayfeng.barcode.service.PackCaseStockLogService;
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
 * 盒库存拼箱日志数据业务逻辑层
 * @author JayFeng
 * @date 2021/10/21
 */
@Slf4j
@Service
public class PackCaseStockLogServiceImpl implements PackCaseStockLogService {

    @Autowired
    private PackCaseStockLogDao packCaseStockLogDao;
    @Autowired
    private QueryConditionHandler queryConditionHandler;

    /**
     * 分页查询盒库存拼箱日志数据
     * @param requestParams 请求参数
     * @param page 分页参数
     * @return 返回
     */
    @Override
    public ResponseData findPackCaseStockLogPage(Map<String, String> requestParams, Page<PackCaseStockLog> page) {
        Map<String, String> queryParamsMap = new HashMap<>(8);
        // 精确匹配查询条件
        queryParamsMap.put("proMaterialNumber", requestParams.get("proMaterialNumber")); // 料号
        queryParamsMap.put("proModel", requestParams.get("proModel")); // 型号
        queryParamsMap.put("lotNumber", requestParams.get("lotNumber")); // 批号
        QueryWrapper<PackCaseStockLog> queryWrapper = new QueryWrapper<>();
        queryConditionHandler.handleEqualQueryCondition(queryWrapper, queryParamsMap);

        // 模糊查询条件
        queryParamsMap.clear();
        queryParamsMap.put("shipmentNumber", requestParams.get("shipmentNumber")); // 出货单号
        queryParamsMap.put("packCaseNumber", requestParams.get("packCaseNumber")); // 拼盒编号

        // byte 状态查询条件
        queryParamsMap.clear();
        queryParamsMap.put("type", requestParams.get("type")); // 库存类型
        queryParamsMap.put("status", requestParams.get("status")); // 库存状态
        queryConditionHandler.handleByteNumberQueryCondition(queryWrapper, queryParamsMap);

        Page<PackCaseStockLog> dataPage = packCaseStockLogDao.selectPage(page, queryWrapper);
        log.info("findPackCaseStockLogPage 分页查询袋库存拼盒日志数据结果 total: {}", dataPage.getTotal());
        return ResponseData.createSuccessResponseData("findPackCaseStockLogPageInfo", dataPage);
    }

    /**
     * 通过 stockId 查询盒库存拼箱日志数据
     * @param stockIdList stockId 集合
     * @return 返回数据
     */
    @Override
    public List<PackCaseStockLog> findByStockIdList(Set<Integer> stockIdList) {
        log.info("findByStockIdList 通过 stockId 查询盒库存拼箱日志数据 stockIdList: {}", stockIdList);
        List<PackCaseStockLog> result = packCaseStockLogDao.selectBatchIds(stockIdList);
        log.info("findByStockIdList 查询盒库存拼箱日志数据结果 size: {}", result.size());
        return result;
    }

    /**
     * 库存对象转库存拼箱日志对象
     * @param stock 库存对象
     * @param packCaseNumber 拼箱编码
     * @return 返回库存拼箱日志对象
     */
    public PackCaseStockLog stockToPackCaseStockLog(Stock stock, String packCaseNumber) {
        PackCaseStockLog packCaseStockLog = new PackCaseStockLog();
        packCaseStockLog.setStockId(stock.getId());
        packCaseStockLog.setProMaterialNumber(stock.getProMaterialNumber());
        packCaseStockLog.setProModel(stock.getProModel());
        packCaseStockLog.setLotNumber(stock.getLotNumber());
        packCaseStockLog.setAmount(stock.getAmount());
        packCaseStockLog.setType(stock.getType());
        packCaseStockLog.setPackCaseNumber(packCaseNumber);
        packCaseStockLog.setWareCode(stock.getWareCode());
        packCaseStockLog.setWareName(stock.getWareName());
        packCaseStockLog.setShelfCode(stock.getShelfCode());
        packCaseStockLog.setCreateTime(LocalDateTime.now());
        packCaseStockLog.setUpdateTime(LocalDateTime.now());
        log.info("stockToPackCaseStockLog 库存对象转库存拼箱日志对象 packCaseStockLog: {}", packCaseStockLog);
        return packCaseStockLog;
    }

    /**
     * 根据拼箱编号查询拼箱日志数据
     * @param packCaseNumber 拼箱编号
     * @return 返回
     */
    @Override
    public ResponseData findByPackCaseNumber(String packCaseNumber) {
        if (ObjectUtils.isEmpty(packCaseNumber)) {
            log.info("findByPackCaseNumber 拼箱日志数据查找失败，拼箱编号为空");
            return ResponseData.createFailResponseData("findByPackCaseNumberInfo", "数据查找失败，拼箱编号有误", ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }
        try {
            Long.parseLong(packCaseNumber);
        } catch (NumberFormatException e) {
            log.info("findByPackCaseNumber 拼箱日志数据查找失败，拼箱编号格式错误 packCaseNumber: {}", packCaseNumber);
            return ResponseData.createFailResponseData("findByPackCaseNumberInfo", "数据查找失败，拼箱编号有误", ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }
        List<PackCaseStockLog> resultList = packCaseStockLogDao.findByPackCaseNumber(packCaseNumber);
        log.info("packCaseNumber 拼箱日志数据查找结果 packCaseNumber: {}, size: {}", packCaseNumber, resultList.size());
        return ResponseData.createSuccessResponseData("packCaseNumberInfo", resultList);
    }

}
