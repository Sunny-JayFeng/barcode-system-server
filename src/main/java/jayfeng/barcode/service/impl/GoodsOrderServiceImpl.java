package jayfeng.barcode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jayfeng.barcode.bean.*;
import jayfeng.barcode.constant.ResponseFailTypeConstant;
import jayfeng.barcode.constant.StockConstant;
import jayfeng.barcode.constant.WarehouseConstant;
import jayfeng.barcode.dao.GoodsOrderDao;
import jayfeng.barcode.dao.ProductDao;
import jayfeng.barcode.dao.StockDao;
import jayfeng.barcode.dao.StockLogDao;
import jayfeng.barcode.handler.QueryConditionHandler;
import jayfeng.barcode.redis.RedisService;
import jayfeng.barcode.response.ResponseData;
import jayfeng.barcode.service.GoodsOrderService;
import jayfeng.barcode.service.StockLogService;
import jayfeng.barcode.service.StockService;
import jayfeng.barcode.service.UserService;
import jayfeng.barcode.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 出货单业务逻辑层
 * @author JayFeng
 * @date 2021/11/5
 */
@Slf4j
@Service
public class GoodsOrderServiceImpl implements GoodsOrderService {

    @Autowired
    private GoodsOrderDao goodsOrderDao;
    @Autowired
    private ProductDao productDao;
    @Autowired
    private StockDao stockDao;
    @Autowired
    private StockLogDao stockLogDao;
    @Autowired
    private QueryConditionHandler queryConditionHandler;
    @Autowired
    private UserService userService;
    @Autowired
    private StockService stockService;
    @Autowired
    private StockLogService stockLogService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private RedissonUtil redissonUtil;

    /**
     * 分页查询出货单信息
     * @param requestParams 请求参数
     * @param page 分页参数
     * @return 返回
     */
    @Override
    public ResponseData findGoodsOrderPage(Map<String, String> requestParams, Page<GoodsOrder> page) {
        Map<String, String> queryParams = new HashMap<>(8);
        queryParams.put("createUserName", requestParams.get("createUserName"));
        queryParams.put("customerName", requestParams.get("customerName"));
        queryParams.put("proMaterialNumber", requestParams.get("proMaterialNumber"));
        queryParams.put("proModel", requestParams.get("proModel"));
        queryParams.put("lotNumber", requestParams.get("lotNumber"));
        String goodsOrderNumber = requestParams.get("goodsOrderNumber");
        QueryWrapper<GoodsOrder> queryWrapper = new QueryWrapper<>();
        queryConditionHandler.handleEqualQueryCondition(queryWrapper, queryParams);
        if (!ObjectUtils.isEmpty(goodsOrderNumber)) {
            try {
                queryWrapper.eq("goodsOrderNumber", Long.parseLong(goodsOrderNumber));
            } catch (NumberFormatException e) {
                log.info("findGoodsOrderPage 查询失败，出货单编号错误 goodsOrderNumber: {}", goodsOrderNumber);
                return ResponseData.createFailResponseData("findGoodsOrderPageInfo", "查询失败，出货单编号错误", ResponseFailTypeConstant.DATA_ERROR.getFailType());
            }
        }
        Page<GoodsOrder> dataPage = goodsOrderDao.selectPage(page, queryWrapper);
        log.info("findGoodsOrderPage 分页查询出货单信息结果total: {}", dataPage.getTotal());
        return ResponseData.createSuccessResponseData("findGoodsOrderPageInfo", dataPage);
    }

    /**
     * 获取库存用于创建出货单
     * @param requestParams
     * String proMaterialNumber 料号
     * String proModel 型号
     * String lotNumber 批号-可选
     * @return 返回
     */
    @Override
    public ResponseData findStockToCreateOrder(Map<String, String> requestParams) {
        String proMaterialNumber = requestParams.get("proMaterialNumber");
        String proModel = requestParams.get("proModel");
        String lotNumber = requestParams.get("lotNumber"); // 批号可有可无
        if (ObjectUtils.isEmpty(proMaterialNumber) || ObjectUtils.isEmpty(proModel)) {
            log.info("findStockToCreateOrder 获取库存用于创建出货单失败，参数为空 requestParams: {}", requestParams);
            return ResponseData.createFailResponseData("findStockToCreateOrderInfo", "获取库存用于创建出货单失败，参数错误", ResponseFailTypeConstant.PARAMS_ERROR.getFailType());
        }
        // 获取标准数量
        Product product = productDao.findStandardQuantity(proMaterialNumber);
        int boxStandardQuantity = product.getBoxStandardQuantity();
        int caseStandardQuantity = product.getCaseStandardQuantity();
        // 构建查询条件
        QueryWrapper<Stock> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("proMaterialNumber", proMaterialNumber);
        queryWrapper.eq("proModel", proModel);
        if (!ObjectUtils.isEmpty(lotNumber)) { // 批号可有可无
            queryWrapper.eq("lotNumber", lotNumber);
        }
        queryWrapper.eq("status", StockConstant.IN_WARE_HOUSE.getStatus());
        queryWrapper.likeRight("wareCode", WarehouseConstant.QP_STARTS_WIDTH.getStartsWidth());
        queryWrapper.orderByDesc("inTime");
        // 获取数据
        List<Stock> stockList = stockDao.selectList(queryWrapper);
        // 库存分组
        List<Stock> boxStockList = new ArrayList<>(64);
        List<Stock> caseStockList = new ArrayList<>(64);
        List<Stock> scatterStockList = new ArrayList<>(64);
        for (Stock stock : stockList) {
            try {
                if (!redissonUtil.tryLock(stock.getId())) continue;
            } finally {
                redissonUtil.unLock(stock.getId());
            }
            if (stock.getAmount().equals(boxStandardQuantity)) boxStockList.add(stock);
            else if (stock.getAmount().equals(caseStandardQuantity)) caseStockList.add(stock);
            else scatterStockList.add(stock);
        }
        // 构建返回数据
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("boxStandardQuantity", boxStandardQuantity);
        resultMap.put("caseStandardQuantity", caseStandardQuantity);
        resultMap.put("boxStockList", boxStockList);
        resultMap.put("caseStockList", caseStockList);
        resultMap.put("scatterStockList", scatterStockList);
        log.info("findStockToCreateOrder 获取库存用户创建出货单成功stockListSize: {}", stockList.size());
        return ResponseData.createSuccessResponseData("findStockToCreateOrderInfo", resultMap);
    }

    /**
     * 库存加锁，用于创建出货单
     * @param stockId 库存 id
     * @return 返回
     */
    @Override
    public ResponseData lockStockToCreateOrder(Integer stockId) {
        if (redissonUtil.tryLockWithoutLeaseTime(stockId.toString())) {
            log.info("lockStockToCreateOrder 库存加锁成功 operator: {}, stockId: {}", userService.getNowLoginUser().getUserName(), stockId);
            return ResponseData.createSuccessResponseData("lockStockToCreateOrderInfo", "success");
        }
        log.info("lockStockToCreateOrder 库存加锁失败，该库存正在被操作 stockId: {}", stockId);
        return ResponseData.createFailResponseData("lockStockToCreateOrderInfo", "库存加锁失败，该库存正在被操作", ResponseFailTypeConstant.SYSTEM_BUSY.getFailType());
    }

    /**
     * 库存释放锁，取消创建出货单
     * @param stockId 库存 id
     * @return 返回
     */
    @Override
    public ResponseData unLockStockOfCreateOrder(Integer stockId) {
        if (redissonUtil.lockIsLocked(stockId)) {
            redissonUtil.unLock(stockId);
        }
        redisService.removeStockIdKey(stockId);
        log.info("unLockStockOfCreateOrder 取消勾选，释放锁 operator: {}", userService.getNowLoginUser().getUserName());
        return ResponseData.createSuccessResponseData("unLockStockOfCreateOrder", "锁释放成功");
    }

    /**
     * 没有点击确认生成出货单就退出页面 -- 释放所有锁
     * @param stockIds 库存 id 列表
     * @return 返回
     */
    @Override
    public ResponseData unLockGoodsOrderStock(String stockIds) {
        return unLockGoodsOrderStock(stockService.parseStockIds(stockIds));
    }

    /**
     * 生成出货单 -- 库存状态转为待出货，而不是生成出货单直接就出货了
     * @param requestParams 请求参数
     * String customerName 客户名称
     * String proMaterialNumber 料号
     * String proModel 型号
     * String stockIds 锁定的库存 id 列表
     * Integer amount 出货数量
     * @return 返回
     */
    @Override
    @Transactional
    public ResponseData createGoodsOrder(Map<String, String> requestParams) {
        String customerName = requestParams.get("customerName");
        String proMaterialNumber = requestParams.get("proMaterialNumber");
        String proModel = requestParams.get("proModel");
        String stockIds = requestParams.get("stockIds");
        if (ObjectUtils.isEmpty(customerName) || ObjectUtils.isEmpty(proMaterialNumber) || ObjectUtils.isEmpty(proModel) || ObjectUtils.isEmpty(stockIds)) {
            log.info("createGoodsOrder 出货单生成失败，参数为空 requestParams: {}", requestParams);
            return ResponseData.createFailResponseData("createGoodsOrderInfo", "出货单生成失败，参数错误", ResponseFailTypeConstant.PARAMS_ERROR.getFailType());
        }
        String amountStr = requestParams.get("amount");
        Integer amount = null;
        try {
            amount = Integer.parseInt(amountStr);
            if (amount == 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            log.info("createGoodsOrder 出货单生成失败，数量有误 amount: {}", amountStr);
            return ResponseData.createFailResponseData("createGoodsOrderInfo", "出货单生成失败，数量错误", ResponseFailTypeConstant.PARAMS_ERROR.getFailType());
        }
        List<Integer> stockIdList = stockService.parseStockIds(stockIds);
        if (stockIdList == null) {
            log.info("createGoodsOrder 出货单生成失败，数据有误, 库存id列表解析失败 stockIds: {}", stockIds);
            return ResponseData.createFailResponseData("createGoodsOrderInfo", "出货单生成失败，数据有误", ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }
        List<Stock> stockList = stockDao.selectBatchIds(stockIdList);
        // 如果有料号或型号不匹配的
        if (stockList.stream().anyMatch(t -> !t.getProMaterialNumber().equals(proMaterialNumber) && !t.getProModel().equals(proModel))) {
            log.info("createGoodsOrder 出货单生成失败，数据有误, 存在料号或型号不匹配的");
            return ResponseData.createFailResponseData("createGoodsOrderInfo", "出货单生成失败，数据有误", ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }
        // 如果数量不匹配
        Integer stocksAmount = stockList.stream().mapToInt(Stock::getAmount).sum();
        if (!stocksAmount.equals(amount)) {
            log.info("createGoodsOrder 出货单生成失败，数量有误 amount: {}, stocksAmount: {}", amount, stocksAmount);
            return ResponseData.createFailResponseData("createGoodsOrderInfo", "出货单生成失败，数量有误", ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }
        User nowLoginUser = userService.getNowLoginUser();
        // 构建出货单
        GoodsOrder goodsOrder = new GoodsOrder();
        goodsOrder.setCreateUserName(nowLoginUser.getUserName());
        goodsOrder.setCustomerName(customerName);
        goodsOrder.setProMaterialNumber(proMaterialNumber);
        goodsOrder.setProModel(proModel);
        goodsOrder.setAmount(amount);
        goodsOrder.setGoodsOrderNumber(createGoodsOrderNumber());
        goodsOrder.setStatus(GoodsOrderService.WAIT_OUT);
        goodsOrder.setCreateTime(LocalDateTime.now());
        goodsOrder.setUpdateTime(LocalDateTime.now());

        // 变更库存状态，不用更新库存出货单编号这个字段，因为需要等确认出货之后才更新这个出货单编号
        for (Stock stock : stockList) {
            stock.setStatus(StockConstant.WAIT_OUT_WARE_HOUSE.getStatus());
            stock.setUpdateTime(LocalDateTime.now());
            stockDao.updateById(stock);
        }
        goodsOrderDao.insert(goodsOrder);
        log.info("createGoodsOrder 出货单生成成功 goodsOrder: {}", goodsOrder);
        return ResponseData.createSuccessResponseData("createGoodsOrderInfo", "出货单生成成功");
    }

    /**
     * 确认出货
     * @param goodsOrderId 出货单 id
     * @param stockIds 库存 id 列表
     * @return 返回
     */
    @Override
    @Transactional
    public ResponseData confirmOutStock(Integer goodsOrderId, String stockIds) {
        GoodsOrder goodsOrder = goodsOrderDao.selectById(goodsOrderId);
        if (goodsOrder == null) {
            log.info("confirmOutStock 确认出货失败，出货单不存在 goodsOrderId: {}", goodsOrderId);
            return ResponseData.createFailResponseData("confirmOutStockInfo", "确认出货失败，出货单不存在", ResponseFailTypeConstant.DATA_NOT_EXIST.getFailType());
        }
        List<Integer> stockIdList = stockService.parseStockIds(stockIds);
        if (stockIdList == null) {
            log.info("confirmOutStock 确认出货失败，库存 id 列表有误 stockIds: {}", stockIds);
            return ResponseData.createFailResponseData("confirmOutStockInfo", "确认出货失败，数据有误", ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }
        // 更新出货单状态
        goodsOrder.setStatus(GoodsOrderService.ALREADY_OUT);
        goodsOrder.setUpdateTime(LocalDateTime.now());
        goodsOrderDao.updateById(goodsOrder);
        // 更新库存状态，释放锁
        List<Stock> stockList = stockDao.selectBatchIds(stockIdList);
        for (Stock stock : stockList) {
            stock.setGoodsOrderNumber(goodsOrder.getGoodsOrderNumber());
            stock.setStatus(StockConstant.ALREADY_OUT_WARE_HOUSE.getStatus());
            stock.setUpdateTime(LocalDateTime.now());
            redissonUtil.unLock(stock.getId()); // 释放锁
            stockDao.updateById(stock);
            // 生成库存操作日志
            StockLog stockLog = stockLogService.stockToStockLog(stock);
            stockLog.setOperationType(StockConstant.OUT_OF_WAREHOUSE.getOperationType());
            stockLog.setOperator(userService.getNowLoginUser().getUserName());
            stockLog.setGoodsOrderNumber(goodsOrder.getGoodsOrderNumber());
            stockLog.setUpdateTime(LocalDateTime.now());
            stockLogDao.insert(stockLog);
        }
        log.info("confirmOutStock 确认出货成功 operator: {}, stockIds: {}, goodsOrder: {}", userService.getNowLoginUser().getUserName(), stockIds, goodsOrder);
        return ResponseData.createSuccessResponseData("confirmOutStockInfo", "确认出货成功");
    }

    /**
     * 释放所有锁的逻辑
     * 出货单已经生成，点击确认出货，释放所有锁
     * 没有点击确认生成出货单就退出页面 -- 释放所有锁
     * @param stockIdList 库存 id 列表
     * @return 返回
     */
    public ResponseData unLockGoodsOrderStock(List<Integer> stockIdList) {
        if (stockIdList == null) {
            log.info("unLockGoodsOrderStock 释放所有出货库存锁失败，库存id列表有误");
            return ResponseData.createFailResponseData("unLockGoodsOrderStockInfo", "释放所有出货库存锁失败，数据有误", ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }
        for (Integer stockId : stockIdList) {
            redissonUtil.unLock(stockId);
        }
        log.info("unLockGoodsOrderStock 释放所有出货库存锁成功 operator: {}, stockIdList: {}", userService.getNowLoginUser().getUserName(), stockIdList);
        return ResponseData.createSuccessResponseData("unLockGoodsOrderStockInfo", "锁释放成功");
    }

    /**
     * 创建出货单编号
     * @return 返回出货单编号
     */
    public Long createGoodsOrderNumber() {
        StringBuilder result = new StringBuilder();
        LocalDateTime date = LocalDateTime.now();
        result.append(date.getYear());
        result.append(date.getMonthValue());
        result.append(date.getDayOfMonth());
        result.append(date.getHour());
        result.append(date.getMinute());
        result.append(date.getSecond());
        result.append(System.currentTimeMillis());
        return Long.parseLong(result.toString());
    }

}
