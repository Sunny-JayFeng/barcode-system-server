package jayfeng.barcode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jayfeng.barcode.bean.*;
import jayfeng.barcode.constant.ResponseFailTypeConstant;
import jayfeng.barcode.constant.StockConstant;
import jayfeng.barcode.constant.WarehouseConstant;
import jayfeng.barcode.dao.*;
import jayfeng.barcode.handler.QueryConditionHandler;
import jayfeng.barcode.redis.RedisService;
import jayfeng.barcode.response.ResponseData;
import jayfeng.barcode.service.*;
import jayfeng.barcode.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 库存数据业务逻辑层
 * @author JayFeng
 * @date 2021/10/21
 */
@Slf4j
@Service
public class StockServiceImpl implements StockService {

    @Autowired
    private StockDao stockDao;
    @Autowired
    private StockLogDao stockLogDao;
    @Autowired
    private ShelfDao shelfDao;
    @Autowired
    private WarehouseDao warehouseDao;
    @Autowired
    private ProductDao productDao;
    @Autowired
    private PackBoxStockLogDao packBoxStockLogDao;
    @Autowired
    private PackCaseStockLogDao packCaseStockLogDao;
    @Autowired
    private StockServiceImpl stockServiceImpl;
    @Autowired
    private StockLogService stockLogService;
    @Autowired
    private UserService userService;
    @Autowired
    private WarehouseService warehouseService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private PackBoxStockLogService packBoxStockLogService;
    @Autowired
    private PackCaseStockLogService packCaseStockLogService;
    @Autowired
    private QueryConditionHandler queryConditionHandler;
    @Autowired
    private RedissonUtil redissonUtil;

    /**
     * 分页查询库存数据
     * @param requestParams 请求参数
     * @param page 分页参数
     * @return 返回
     */
    @Override
    public ResponseData findStockPage(Map<String, String> requestParams, Page<Stock> page) {
        Map<String, String> queryParamsMap = new HashMap<>(4);
        // 精确匹配查询条件
        queryParamsMap.put("proMaterialNumber", requestParams.get("proMaterialNumber")); // 料号
        queryParamsMap.put("proModel", requestParams.get("proModel")); // 型号
        queryParamsMap.put("lotNumber", requestParams.get("lotNumber")); // 批号
        QueryWrapper<Stock> queryWrapper = new QueryWrapper<>();
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
        queryConditionHandler.handleByteNumberQueryCondition(queryWrapper, queryParamsMap);

        Page<Stock> dataPage = stockDao.selectPage(page, queryWrapper);
        log.info("findStockPage 分页查询库存数据结果 total: {}", dataPage.getTotal());
        return ResponseData.createSuccessResponseData("findStockPageInfo", dataPage);
    }

    /**
     * 库存货物扫码上架
     * @param serialNumber 扫码上架的库存的二维码信息中的序列号：解析二维码得到
     * @param shelfId 货架 id
     * @return 返回
     */
    @Override
    @Transactional
    public ResponseData stockPutOnTheShelf(String serialNumber, Integer shelfId) {
        User nowLoginUser = userService.getNowLoginUser();
        Stock stock = serialNumberFindStock(serialNumber);
        if (stock == null) {
            log.info("stockPutOnTheShelf 库存货物扫码上架失败，序列号不正确 serialNumber: {}", serialNumber);
            return ResponseData.createFailResponseData("stockPutOnTheShelfInfo", "库存货物扫码上架失败，查无此库存，请检查序列号", ResponseFailTypeConstant.DATA_NOT_EXIST.getFailType());
        }
        Shelf shelf = shelfDao.selectById(shelfId);
        // 如果货架不存在，上架失败
        if (ObjectUtils.isEmpty(shelf)) {
            log.info("stockPutOnTheShelf 库存货物扫码上架失败，货架不存在 shelfId: {}", shelfId);
            return ResponseData.createFailResponseData("stockPutOnTheShelfInfo", "上架失败，货架不存在", ResponseFailTypeConstant.DATA_NOT_EXIST.getFailType());
        }
        // 货架类型跟库存类型不匹配
        if (!shelf.getShelfType().equals(stock.getType())) {
            log.info("stockPutOnTheShelf 库存货物扫码上架失败，货架类型跟库存类型不匹配 shelfType: {}, stockType: {}", shelf.getShelfType(), stock.getType());
            return ResponseData.createFailResponseData("stockPutOnTheShelfInfo", "库存货物扫码上架失败，货架类型跟库存类型不匹配", ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }
        // 修改库存所在货架
        stock.setShelfCode(shelf.getShelfCode());
        stock.setUpdateTime(LocalDateTime.now());

        // 生成库存操作日志
        StockLog stockLog = stockLogService.stockToStockLog(stock);
        stockLog.setOperationType(StockConstant.PUT_ON_THE_SHELF.getOperationType());
        stockLog.setOperator(nowLoginUser.getUserName());
        stockLog.setCreateTime(LocalDateTime.now());
        stockLog.setUpdateTime(LocalDateTime.now());
        // 加锁逻辑 !redisService.isExistKey(stock.getId()) 防止该库存当前正在被拼盒或者拼箱。
        // 拼盒拼箱操作中，每扫一个，会往 redis 存入这个 id
        if (redissonUtil.tryLock(stock.getId())) {
            try {
                // 持久化到数据库
                stockDao.updateById(stock);
                stockLogDao.insert(stockLog);
            } finally {
                redissonUtil.unLock(stock.getId());
            }
        } else { // 加锁失败
            log.info("stockPutOnTheShelf 库存货物扫码上架失败，该库存正在被操作");
            return ResponseData.createFailResponseData("stockPutOnTheShelfInfo", "库存货物扫码上架失败，该库存正在进行其他操作", ResponseFailTypeConstant.SYSTEM_BUSY.getFailType());
        }
        // 处理返回数据
        stock.setStockLogId(stockLog.getId());
        log.info("stockPutOnTheShelf 库存货物扫码上架成功，operator: {}, shelfCode: {}, stockLog: {}", stockLog.getOperator(), shelf.getShelfCode(), stockLog);
        return ResponseData.createSuccessResponseData("stockPutOnTheShelfInfo", stock);
    }

    /**
     * 取消库存货物扫码上架的操作
     * @param stockId 库存 id
     * @param stockLogId 操作日志 id
     * @return 返回
     */
    @Override
    @Transactional
    public ResponseData cancelStockPutOnTheShelf(Integer stockId, Integer stockLogId) {
        // 加锁逻辑 !redisService.isExistKey(stock.getId()) 防止该库存当前正在被拼盒或者拼箱。
        if (redissonUtil.tryLock(stockId)) {
            try {
                User nowLoginUser = userService.getNowLoginUser();
                Stock stock = stockDao.selectById(stockId);
                stock.setShelfCode(""); // 取消上架
                stock.setUpdateTime(LocalDateTime.now());
                log.info("cancelStockPutOnTheShelf 取消库存货物扫码上架的操作 operator: {}, stock: {}", nowLoginUser.getUserName(), stock);
                StockLog stockLog = stockLogDao.selectById(stockLogId);
                stockDao.updateById(stock);
                stockLogDao.deleteById(stockLogId);
                log.info("cancelStockPutOnTheShelf 取消上架，删除上架日志 operator: {}, stockLog: {}", nowLoginUser.getUserName(), stockLog);
                log.info("cancelStockPutOnTheShelf 取消库存货物扫码上架成功 operator: {},", nowLoginUser.getUserName());
                return ResponseData.createSuccessResponseData("cancelStockPutOnTheShelfInfo", "取消该货物的上架操作成功");
            } finally {
                redissonUtil.unLock(stockId);
            }
        } else { // 加锁失败
            log.info("cancelStockPutOnTheShelf 取消库存货物扫码上架失败，该库存正在被操作");
            return ResponseData.createFailResponseData("cancelStockPutOnTheShelfInfo", "取消库存货物扫码上架失败，该库存正在进行其他操作", ResponseFailTypeConstant.SYSTEM_BUSY.getFailType());
        }
    }

    /**
     * 库存货物扫码下架
     * @param serialNumber 扫码下架的库存的二维码信息中的序列号：解析二维码得到
     * @param shelfId 货架 id
     * @return 返回
     */
    @Override
    @Transactional
    public ResponseData stockRemoveFromShelf(String serialNumber, Integer shelfId) {
        User nowLoginUser = userService.getNowLoginUser();
        Stock stock = serialNumberFindStock(serialNumber);
        if (stock == null) {
            log.info("stockRemoveFromShelf 库存货物扫码下架失败，序列号不正确 serialNumber: {}", serialNumber);
            return ResponseData.createFailResponseData("stockRemoveFromShelfInfo", "库存货物扫码下架失败，查无此库存，请检查序列号", ResponseFailTypeConstant.DATA_NOT_EXIST.getFailType());
        }
        Shelf shelf = shelfDao.selectById(shelfId);
        if (ObjectUtils.isEmpty(shelf)) {
            log.info("stockRemoveFromShelf 库存货物扫码下架失败，货架不存在 shelfId: {}", shelfId);
            return ResponseData.createFailResponseData("stockRemoveFromShelfInfo", "下架失败，货架不存在", ResponseFailTypeConstant.DATA_NOT_EXIST.getFailType());
        }
        // 货架类型跟库存类型不匹配
        if (!shelf.getShelfType().equals(stock.getType())) {
            log.info("stockRemoveFromShelf 库存货物扫码下架失败，货架类型跟库存类型不匹配 shelfType: {}, stockType: {}", shelf.getShelfType(), stock.getType());
            return ResponseData.createFailResponseData("stockRemoveFromShelfInfo", "库存货物扫码下架失败，货架类型跟库存类型不匹配", ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }
        // 库存货架编码置为空
        stock.setShelfCode("");
        stock.setUpdateTime(LocalDateTime.now());
        // 生成库存操作日志
        StockLog stockLog = stockLogService.stockToStockLog(stock);
        stockLog.setOperationType(StockConstant.REMOVE_FROM_THE_SHELF.getOperationType());
        stockLog.setOperator(nowLoginUser.getUserName());
        stockLog.setCreateTime(LocalDateTime.now());
        stockLog.setUpdateTime(stockLog.getUpdateTime());
        // 加锁逻辑 !redisService.isExistKey(stock.getId()) 防止该库存当前正在被拼盒或者拼箱。
        if (redissonUtil.tryLock(stock.getId())) {
            try {
                // 持久化到数据库
                stockDao.updateById(stock);
                stockLogDao.insert(stockLog);
            } finally {
                redissonUtil.unLock(stock.getId());
            }
        } else { // 加锁失败
            log.info("stockRemoveFromShelf 库存货物扫码下架成功失败，该库存正在被操作");
            return ResponseData.createFailResponseData("stockRemoveFromShelfInfo", "库存货物扫码下架成功失败，该库存正在进行其他操作", ResponseFailTypeConstant.SYSTEM_BUSY.getFailType());
        }
        // 处理返回数据
        stock.setStockLogId(stockLog.getId());
        stock.setShelfCode(shelf.getShelfCode());
        log.info("stockRemoveFromShelf 库存货物扫码下架成功,operator: {}, shelfCode: {}, stockLog: {}", nowLoginUser.getUserName(), shelf.getShelfCode(), stockLog);
        return ResponseData.createSuccessResponseData("stockRemoveFromShelfInfo", stock);
    }

    /**
     * 取消库存货物扫码下架的操作
     * @param shelfCode 货架编码
     * @param stockId 库存 id
     * @param stockLogId 操作日志 id
     * @return 返回
     */
    @Override
    @Transactional
    public ResponseData cancelStockRemoveFromShelf(String shelfCode, Integer stockId, Integer stockLogId) {
        // 加锁逻辑 !redisService.isExistKey(stock.getId()) 防止该库存当前正在被拼盒或者拼箱。
        if (redissonUtil.tryLock(stockId)) {
            try {
                User nowLoginUser = userService.getNowLoginUser();
                Stock stock = stockDao.selectById(stockId);
                stock.setShelfCode(shelfCode);
                stock.setUpdateTime(LocalDateTime.now());
                stockDao.updateById(stock);
                log.info("cancelStockRemoveFromShelf 取消库存货物扫码下架成功 operator: {}, stock: {}", nowLoginUser.getUserName(), stock);
                StockLog stockLog = stockLogDao.selectById(stockLogId);
                stockLogDao.deleteById(stockLogId);
                log.info("cancelStockRemoveFromShelf 取消库存货物扫码下架，删除操作日志 operator: {}, stockLog: {}", nowLoginUser.getUserName(), stockLog);
                log.info("cancelStockRemoveFromShelf 取消库存货物扫码下架成功 operator: {},", nowLoginUser.getUserName());
                return ResponseData.createSuccessResponseData("cancelStockRemoveFromShelfInfo", "取消下架成功");
            } finally {
                redissonUtil.unLock(stockId);
            }
        } else { // 加锁失败
            log.info("cancelStockRemoveFromShelf 取消库存货物扫码下架失败，该库存正在被操作");
            return ResponseData.createFailResponseData("cancelStockRemoveFromShelfInfo", "取消库存货物扫码下架失败，该库存正在进行其他操作", ResponseFailTypeConstant.SYSTEM_BUSY.getFailType());
        }
    }

    /**
     * 货物扫码入库
     * @param qrCodeValue 扫码入库的货物的二维码信息
     * @param wareCode 检测仓仓库编码
     * @return 返回
     */
    @Override
    @Transactional
    public ResponseData stockIntoWarehouse(String qrCodeValue, String wareCode) {
        if (!warehouseService.isInspectionWarehouse(wareCode)) { // 如果不是检测仓
            log.info("stockIntoWarehouse 扫码入库失败，新到的货物必须入检测仓 wareCode: {}", wareCode);
            return ResponseData.createFailResponseData("stockIntoWarehouseInfo", "扫码入库失败，新到的货物必须入检测仓", ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }
        Warehouse warehouse = warehouseDao.findWareHouseByWareCode(wareCode);
        Stock stock = parseQrCodeValueToStock(qrCodeValue); // 解析二维码
        User nowLoginUser = userService.getNowLoginUser();
        if (stock == null) return ResponseData.createFailResponseData("stockIntoWarehouseInfo", "入库失败，二维码解析错误", ResponseFailTypeConstant.DATA_ERROR.getFailType());
        stock.setStatus(StockConstant.IN_WARE_HOUSE.getStatus());
        stock.setGoodsOrderNumber(0L);
        stock.setIsPackBox((byte)0);
        stock.setPackBoxNumber("");
        stock.setIsPackCase((byte)0);
        stock.setPackCaseNumber("");
        stock.setWareCode(wareCode);
        stock.setWareName(warehouse.getWareName());
        stock.setShelfCode("");
        stock.setInTime(LocalDateTime.now());
        String serialNumber = createStockSerialNumber();
        if (serialNumber == null) {
            log.info("stockIntoWarehouse 扫码入库失败，无法生成序列号");
            return ResponseData.createFailResponseData("stockIntoWarehouseInfo", "扫码入库失败，无法生成序列号，请稍后重试", ResponseFailTypeConstant.SYSTEM_BUSY.getFailType());
        }
        stock.setSerialNumber(Long.parseLong((serialNumber)));
        stock.setCreateTime(LocalDateTime.now());
        stock.setUpdateTime(LocalDateTime.now());
        stockDao.insert(stock);
        StockLog stockLog = stockLogService.stockToStockLog(stock);
        stockLog.setOperationType(StockConstant.INTO_THE_WAREHOUSE.getOperationType());
        stockLog.setOperator(nowLoginUser.getUserName());
        stockLog.setCreateTime(LocalDateTime.now());
        stockLog.setUpdateTime(LocalDateTime.now());
        stockLogDao.insert(stockLog);
        // 处理返回数据
        stock.setStockLogId(stockLog.getId());
        log.info("stockIntoWarehouse 入库成功 operator: {}, stock: {}", nowLoginUser.getUserName(), stock);
        return ResponseData.createSuccessResponseData("stockIntoWarehouseInfo", stock);
    }

    /**
     * 已检测的库存扫码入库
     * @param serialNumber 扫码出库的货物二维码信息中的序列号
     * @param targetWarehouseId 目标仓库id
     * @return 返回
     */
    @Override
    public ResponseData inspectedStockIntoWarehouse(String serialNumber, Integer targetWarehouseId) {
        Stock stock = serialNumberFindStock(serialNumber);
        if (stock == null) {
            log.info("inspectedStockIntoWarehouse 已检测库存扫码入库失败，查无此库存 serialNumber: {}", serialNumber);
            return ResponseData.createFailResponseData("inspectedStockIntoWarehouseInfo", "扫码入库失败，库存不存在", ResponseFailTypeConstant.DATA_NOT_EXIST.getFailType());
        }
        Warehouse warehouse = warehouseDao.findById(targetWarehouseId);
        if (warehouse == null) {
            log.info("inspectedStockIntoWarehouse 已检测库存扫码入库失败，查无此仓库 targetWarehouseId: {}", targetWarehouseId);
            return ResponseData.createFailResponseData("inspectedStockIntoWarehouseInfo", "扫码入库失败，查无此仓库", ResponseFailTypeConstant.DATA_NOT_EXIST.getFailType());
        }
        stock.setWareCode(warehouse.getWareCode());
        stock.setWareName(warehouse.getWareName());
        stock.setUpdateTime(LocalDateTime.now());
        StockLog stockLog = stockLogService.stockToStockLog(stock);
        stockLog.setOperationType(StockConstant.CHANGE_WAREHOUSE.getOperationType());
        stockLog.setOperator(userService.getNowLoginUser().getUserName());
        stockLog.setCreateTime(LocalDateTime.now());
        stockLog.setUpdateTime(LocalDateTime.now());

        log.info("inspectedStockIntoWarehouse 已检测库存扫码入库成功 operator:{}, stock: {}, stockLog: {}", userService.getNowLoginUser().getUserName(), stock, stockLog);
        return ResponseData.createSuccessResponseData("inspectedStockIntoWarehouseInfo", "入库成功");
    }

    /**
     * 取消货物扫码入库的操作
     * @param stockId 库存 id
     * @param stockLogId 操作日志 id
     * @return 返回
     */
    @Override
    @Transactional
    public ResponseData cancelStockIntoWarehouse(Integer stockId, Integer stockLogId) {
        // 加锁逻辑 !redisService.isExistKey(stock.getId()) 防止该库存当前正在被拼盒或者拼箱。
        if (redissonUtil.tryLock(stockId)) {
            try {
                Stock stock = stockDao.selectById(stockId);
                StockLog stockLog = stockLogDao.selectById(stockLogId);
                stockDao.deleteById(stockId);
                stockLogDao.deleteById(stockLogId);
                log.info("cancelStockIntoWarehouse 取消货物扫码入库,删除库存数据，操作日志数据 operator: {}, stock: {}, stockLog: {}", userService.getNowLoginUser().getUserName(), stock, stockLog);
                return ResponseData.createSuccessResponseData("cancelStockIntoWarehouseInfo", "取消入库成功");
            } finally {
                redissonUtil.unLock(stockId);
            }
        } else { // 加锁失败
            log.info("cancelStockIntoWarehouse 取消货物扫码入库失败，该库存正在被操作");
            return ResponseData.createFailResponseData("cancelStockIntoWarehouseInfo", "取消货物扫码入库失败，该库存正在进行其他操作", ResponseFailTypeConstant.SYSTEM_BUSY.getFailType());
        }
    }

    /**
     * 货物扫码出库
     * @param serialNumber 扫码出库的货物二维码信息中的序列号
     * @return 返回
     */
    @Override
    @Transactional
    public ResponseData stockOutOfWarehouse(String serialNumber) {
        User nowLoginUser = userService.getNowLoginUser();
        Stock stock = serialNumberFindStock(serialNumber);
        if (stock == null) {
            log.info("stockOutOfWarehouse 货物扫码出库失败，序列号不正确 serialNumber: {}", serialNumber);
            return ResponseData.createFailResponseData("stockRemoveFromShelfInfo", "货物扫码出库失败，查无此库存，请检查序列号", ResponseFailTypeConstant.DATA_NOT_EXIST.getFailType());
        }
        if (!stock.getStatus().equals(StockConstant.IN_WARE_HOUSE.getStatus())) {
            log.info("stockOutOfWarehouse 货物扫码出库失败，当前货物状态不是在库");
            return ResponseData.createFailResponseData("stockOutOfWarehouseInfo", "货物扫码出库失败，当前货物状态不是在库", ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }
        String originShelfCode = stock.getShelfCode();
        String originWareCode = stock.getWareCode();
        String originWareName = stock.getWareName();
        // 新增操作日志
        StockLog stockLog = stockLogService.stockToStockLog(stock);
        stockLog.setOperationType(StockConstant.OUT_OF_WAREHOUSE.getOperationType());
        stockLog.setOperator(nowLoginUser.getUserName());
        stockLog.setCreateTime(LocalDateTime.now());
        stockLog.setUpdateTime(LocalDateTime.now());

        stock.setShelfCode(""); // 如果库存在货架上，要下架。但是这里不需要记录下架日志，只需要记录出库日志
        stock.setStatus(StockConstant.ALREADY_OUT_WARE_HOUSE.getStatus()); // 库存状态改为已出库
        stock.setUpdateTime(LocalDateTime.now());
        // 加锁逻辑 !redisService.isExistKey(stock.getId()) 防止该库存当前正在被拼盒或者拼箱。
        if (redissonUtil.tryLock(stock.getId())) {
            try {
                stockLogDao.insert(stockLog);
                stockDao.updateById(stock);
            } finally {
                redissonUtil.unLock(stock.getId());
            }
        } else { // 加锁失败
            log.info("stockOutOfWarehouse 货物扫码出库失败，该库存正在被操作");
            return ResponseData.createFailResponseData("stockOutOfWarehouseInfo", "货物扫码出库失败，该库存正在进行其他操作", ResponseFailTypeConstant.SYSTEM_BUSY.getFailType());
        }
        // 处理返回数据
        stock.setStockLogId(stockLog.getId());
        stock.setShelfCode(originShelfCode);
        stock.setWareCode(originWareCode);
        stock.setWareName(originWareName);
        log.info("stockOutOfWarehouse 货物扫码出库成功 operator: {}, stock: {}", nowLoginUser.getUserName(), stock);
        return ResponseData.createSuccessResponseData("stockOutOfWarehouseInfo", stock);
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
    @Override
    @Transactional
    public ResponseData cancelStockOutOfWarehouse(Map<String, String> requestParams) {
        requestParams.put("methodName", "cancelStockOutOfWarehouse");
        requestParams.put("operateName", "取消货物扫码出库");
        return stockServiceImpl.cancelStockOperate(requestParams);
    }

    /**
     * 库存货物变更货架
     * 分为：仓库内的货架变更；从这个仓库的货架变更到其他仓库的某个货架
     * @param serialNumber  待变更库存的二维码信息中的序列号
     * @param targetShelfId 目标货架 id
     * @return 返回
     */
    @Override
    @Transactional
    public ResponseData stockChangeShelf(String serialNumber, Integer targetShelfId) {
        Stock stock = serialNumberFindStock(serialNumber);
        Shelf targetShelf = shelfDao.selectById(targetShelfId);
        if (targetShelf == null) {
            log.info("stockChangeShelf 库存货物变更货架失败，目标货架不存在 targetShelfId: {}", targetShelfId);
            return ResponseData.createFailResponseData("stockChangeShelfInfo", "库存货物变更货架失败，目标货架不存在", ResponseFailTypeConstant.DATA_NOT_EXIST.getFailType());
        }
        if (stock == null) {
            log.info("stockChangeShelf 库存货物变更货架失败，货物不存在 serialNumber: {}", serialNumber);
            return ResponseData.createFailResponseData("stockChangeShelfInfo", "库存货物变更货架失败，查无此库存，请检查序列号", ResponseFailTypeConstant.DATA_NOT_EXIST.getFailType());
        }
        Warehouse targetWarehouse = warehouseDao.findWareHouseByWareCode(targetShelf.getWareCode());
        // 是否是同一类仓库
        if (!warehouseService.isSameTypeWarehouse(stock.getWareCode(), targetWarehouse.getWareCode())) {
            log.info("stockChangeShelf 库存货物变更货架失败，源仓库和目标仓库不是同一类仓库。originWarehouse: {}, targetWarehouse: {}", stock.getWareName(), targetWarehouse.getWareName());
            return ResponseData.createFailResponseData("stockChangeShelfInfo", "库存货物变更货架失败，源仓库和目标仓库不是同一类仓库", ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }
        User nowLoginUser = userService.getNowLoginUser();
        String originWareCode = stock.getWareCode();
        String originWareName = stock.getWareName();
        String originShelf = stock.getShelfCode();
        // 生成库存操作日志对象
        StockLog stockLog = stockLogService.stockToStockLog(stock);
        stockLog.setOperationType(StockConstant.CHANGE_SHELF.getOperationType()); // 操作类型：变更货架
        stockLog.setOperator(nowLoginUser.getUserName()); // 操作人
        stockLog.setCreateTime(LocalDateTime.now());
        stockLog.setUpdateTime(LocalDateTime.now());

        // 更新库存信息
        stock.setWareCode(targetWarehouse.getWareCode());
        stock.setWareName(targetWarehouse.getWareName());
        stock.setShelfCode(targetShelf.getShelfCode());
        stock.setUpdateTime(LocalDateTime.now());

        // 加锁逻辑 !redisService.isExistKey(stock.getId()) 防止该库存当前正在被拼盒或者拼箱。
        if (redissonUtil.tryLock(stock.getId())) {
            try {
                stockLogDao.insert(stockLog);
                stockDao.updateById(stock);
            } finally {
                redissonUtil.unLock(stock.getId());
            }
        } else { // 加锁失败
            log.info("stockChangeShelf 库存货物变更货架失败，该库存正在被操作");
            return ResponseData.createFailResponseData("stockChangeShelfInfo", "库存货物变更货架失败，该库存正在进行其他操作", ResponseFailTypeConstant.SYSTEM_BUSY.getFailType());
        }
        // 处理返回数据
        stock.setStockLogId(stockLog.getId());
        stock.setShelfCode(originWareCode);
        stock.setWareCode(originWareCode);
        stock.setWareName(originWareName);
        log.info("stockChangeShelf 库存货物变更货架成功 operator: {}, originWareName: {}, originShelfCode: {}, targetWareName: {}, targetShelfCode: {}", nowLoginUser.getUserName(), originWareName, originShelf, targetWarehouse.getWareName(), targetShelf.getShelfCode());
        return ResponseData.createSuccessResponseData("stockChangeShelfInfo", stock);
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
    @Override
    @Transactional
    public ResponseData cancelStockChangeShelf(Map<String, String> requestParams) {
        requestParams.put("methodName", "cancelStockChangeShelf");
        requestParams.put("operateName", "取消库存货物变更货架");
        return stockServiceImpl.cancelStockOperate(requestParams);
    }

    /**
     * 库存货物变更仓库
     * @param serialNumber 待变更库存的二维码信息中的序列号
     * @param targetWarehouseId 目标仓库的 id
     * @return 返回
     */
    @Override
    @Transactional
    public ResponseData stockChangeWarehouse(String serialNumber, Integer targetWarehouseId) {
        Stock stock = serialNumberFindStock(serialNumber);
        if (stock == null) {
            log.info("stockChangeWarehouse 库存货物变更仓库失败，序列号不正确 serialNumber: {}", serialNumber);
            return ResponseData.createFailResponseData("stockChangeWarehouseInfo", "货物扫码出库失败，查无此库存，请检查序列号", ResponseFailTypeConstant.DATA_NOT_EXIST.getFailType());
        }
        User nowLoginUser = userService.getNowLoginUser();
        Warehouse targetWarehouse = warehouseDao.selectById(targetWarehouseId);
        if (targetWarehouse == null) {
            log.info("stockChangeWarehouse 库存货物变更仓库失败，查无此仓库 targetWarehouseId: {}", targetWarehouseId);
            return ResponseData.createFailResponseData("stockChangeWarehouseInfo", "库存货物变更仓库失败，查无此仓库", ResponseFailTypeConstant.DATA_NOT_EXIST.getFailType());
        }
        // 是否是同一类仓库
        if (!warehouseService.isSameTypeWarehouse(stock.getWareCode(), targetWarehouse.getWareCode())) {
            log.info("stockChangeWarehouse 库存货物变更仓库失败，源仓库和目标仓库不是同一类仓库。originWarehouse: {}, targetWarehouse: {}", stock.getWareName(), targetWarehouse.getWareName());
            return ResponseData.createFailResponseData("stockChangeWarehouseInfo", "库存货物变更仓库失败，源仓库和目标仓库不是同一类仓库", ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }
        String shelfCode = stock.getShelfCode();
        String oldWareName = stock.getWareName();
        String oldWareCode = stock.getWareCode();
        // 生成库存操作日志
        StockLog stockLog = stockLogService.stockToStockLog(stock);
        stockLog.setOperationType(StockConstant.CHANGE_WAREHOUSE.getOperationType()); // 操作类型：变更仓库
        stockLog.setOperator(nowLoginUser.getUserName()); // 操作人
        stockLog.setCreateTime(LocalDateTime.now());
        stockLog.setUpdateTime(LocalDateTime.now());

        stock.setShelfCode(""); // 所在货架置为空
        stock.setWareCode(targetWarehouse.getWareCode()); // 目标仓库编码
        stock.setWareName(targetWarehouse.getWareName()); // 目标仓库名称
        stock.setUpdateTime(LocalDateTime.now());

        // 加锁逻辑 !redisService.isExistKey(stock.getId()) 防止该库存当前正在被拼盒或者拼箱。
        if (redissonUtil.tryLock(stock.getId())) {
            try {
                stockLogDao.insert(stockLog);
                stockDao.updateById(stock);
            } finally {
                redissonUtil.unLock(stock.getId());
            }
        } else { // 加锁失败
            log.info("stockChangeWarehouse 库存货物变更仓库失败，该库存正在被操作");
            return ResponseData.createFailResponseData("stockChangeWarehouseInfo", "库存货物变更仓库失败，该库存正在进行其他操作", ResponseFailTypeConstant.SYSTEM_BUSY.getFailType());
        }

        // 处理返回数据
        stock.setStockLogId(stockLog.getId()); // 操作日志 id
        stock.setShelfCode(shelfCode); // 原所在货架
        stock.setWareName(oldWareName); // 原所在仓库名称
        stock.setWareCode(oldWareCode); // 原所在仓库编码
        log.info("stockChangeWarehouse 库存货物变更仓库成功 operator: {}, oldWarehouse: {}, newWarehouse: {}", nowLoginUser.getUserName(), oldWareName, targetWarehouse.getWareName());
        return ResponseData.createSuccessResponseData("stockChangeWarehouseInfo", stock);
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
    @Override
    @Transactional
    public ResponseData cancelStockChangeWarehouse(Map<String, String> requestParams) {
        requestParams.put("methodName", "cancelStockChangeWarehouse");
        requestParams.put("operateName", "取消库存货物变更仓库");
        return stockServiceImpl.cancelStockOperate(requestParams);
    }

    /**
     * 扫码获取库存信息，拼盒
     * 如果扫的不是袋，则返回错误，因为只有袋能拼盒
     * 这里只返回展示数据，要点击确认拼盒之后才会触发拼盒功能
     * @param serialNumber 解析二维码得到的序列号，用于获取库存信息
     * @return 返回库存信息
     */
    @Override
    public ResponseData scanStockToPackBox(String serialNumber) {
        Stock stock = serialNumberFindStock(serialNumber);
        ResponseData checkResult = checkPackStock(stock, "scanStockToPackBox", "扫码拼盒", serialNumber);
        if (checkResult != null) return checkResult;
        if (stock.getIsPackBox().equals(StockConstant.IS_PACK_BOX.getPackStatus())) {
            log.info("scanStockToPackBox 库存拼盒失败，当前库存已经拼盒，必须先拆盒");
            return ResponseData.createFailResponseData("scanStockToPackBoxInfo", "库存拼盒失败，当前库存已经拼盒，必须先拆盒", ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }
        if (stock.getType().equals(StockConstant.CASE.getType()) || stock.getIsPackCase().equals(StockConstant.IS_PACK_CASE.getPackStatus())) {
            log.info("scanStockToPackBox 库存拼盒失败，当前库存为箱，不能拼盒");
            return ResponseData.createFailResponseData("scanStockToPackBoxInfo", "库存拼盒失败，当前库存为箱，不能拼盒", ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }
        // 不释放锁
        if (redissonUtil.tryLockWithoutLeaseTime(stock.getId())) {
            log.info("scanStockToPackBox 扫码拼盒 operator: {}, stock: {}", userService.getNowLoginUser().getUserName(), stock);
            return ResponseData.createSuccessResponseData("scanStockToPackBoxInfo", stock);
        } else { // 加锁失败
            log.info("scanStockToPackBox 扫码拼盒加锁失败，该库存正在被操作 stock: {}", stock);
            return ResponseData.createFailResponseData("scanStockToPackInfo", "扫码拼盒失败，该库存正在被操作，请稍后重试或使用其他库存", ResponseFailTypeConstant.SYSTEM_BUSY.getFailType());
        }
    }

    /**
     * 取消某个库存的拼盒操作
     * @param stockId 库存 id
     * @return 返回
     */
    @Override
    public ResponseData cancelTheStockPackBox(Integer stockId) {
        Stock stock = stockDao.selectById(stockId);
        if (stock == null) {
            log.info("cancelTheStockPackBox 查无此库存 stockId: {}", stockId);
            return ResponseData.createFailResponseData("cancelTheStockPackBoxInfo", "取消失败，查无此库存", ResponseFailTypeConstant.DATA_NOT_EXIST.getFailType());
        }
        redissonUtil.unLock(stock.getId());
        log.info("cancelTheStockPackBox 取消库存的拼盒操作成功");
        return ResponseData.createSuccessResponseData("cancelTheStockPackBoxInfo", "取消该库存的拼盒操作成功");
    }

    /**
     * 点击确认拼盒
     * @param stockIds 库存 id 列表
     * @return 返回拼盒是否成功
     */
    @Override
    @Transactional
    public ResponseData confirmStockPackBox(String stockIds) {
        List<Integer> stockIdList = parseStockIds(stockIds);
        // 生成拼盒编码
        String serialNumber = createStockSerialNumber(); // 拼盒编码跟序列号一致
        if (serialNumber == null) {
            log.info("confirmStockPackBox 确认拼盒失败，序列号创建不成功");
            return ResponseData.createFailResponseData("confirmStockPackBoxInfo", "确认拼盒失败，创建新序列号和拼盒编码不成功，请稍后重试", ResponseFailTypeConstant.SYSTEM_BUSY.getFailType());
        }
        List<Stock> stockList = stockDao.selectBatchIds(stockIdList);
        List<PackBoxStockLog> packBoxStockLogList = new ArrayList<>(stockIdList.size()); // 拼盒日志
        // 校验数据 料号、型号、批号必须一致;库存类型必须是袋/盒;库存本身必须没有拼盒/拼箱;数量总和必须等于盒标准数量;库存所在仓库、货架必须一致;库存必须是在库状态
        // 已经拼盒的，不能拼，提示必须先拆盒
        Stock baseStock = stockList.get(0); // 检验数据是否一致
        if (baseStock.getType().equals(StockConstant.CASE.getType())) {
            log.info("confirmStockPackBox 确认拼盒失败，箱不能用于拼盒 baseStock: {}", baseStock);
            return ResponseData.createFailResponseData("confirmStockPackBoxInfo", "确认拼盒失败，箱不能用于拼盒", ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }
        // 查出盒标准数量
        int boxStandardQuantity = productDao.findBoxStandardQuantity(baseStock.getProMaterialNumber());
        int count = baseStock.getAmount();
        packBoxStockLogList.add(packBoxStockLogService.stockToPackBoxStockLog(baseStock, serialNumber));
        // 统计数量，校验数据是否一致
        for (int i = 1; i < stockList.size(); i ++) {
            Stock stock = stockList.get(i);
            if (canPack(baseStock, stock) && !stock.getType().equals(StockConstant.CASE.getType())) {
                count += stock.getAmount();
                packBoxStockLogList.add(packBoxStockLogService.stockToPackBoxStockLog(stock, serialNumber));
            } else {
                log.info("confirmStockPackBox 确认拼盒失败，数据有误 stock: {}, baseStock: {}", stock, baseStock);
                return ResponseData.createFailResponseData("confirmStockPackBoxInfo", "拼盒失败，数据有误。料号、型号、批号必须一致;库存类型必须是袋/盒;库存本身必须没有拼盒/拼箱;数量总和必须等于盒标准数量;库存所在仓库、货架必须一致;库存必须是在库状态", ResponseFailTypeConstant.DATA_ERROR.getFailType());
            }
        }
        if (count != boxStandardQuantity) {
            log.info("confirmStockPackBox 确认拼盒失败，数量不对 count: {}, boxStandardQuantity: {}", count, boxStandardQuantity);
            return ResponseData.createFailResponseData("confirmStockPackBoxInfo", "确认拼盒失败，数量与盒标准数量不匹配，请检查", ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }
        // 生成新的 stock 记录
        Stock newStock = packBoxGetNewStock(baseStock, count, serialNumber);
        // 持久化数据
        log.info("confirmStockPackBox 持久化新库存数据 newStock: {}", newStock);
        stockDao.insert(newStock);
        // packBoxStockLog 记录拼盒日志
        for (PackBoxStockLog packBoxStockLog : packBoxStockLogList) {
            log.info("confirmStockPackBox 持久化拼盒日志 packBoxStockLog: {}", packBoxStockLog);
            packBoxStockLogDao.insert(packBoxStockLog);
        }
        // stock 删除旧库存记录
        log.info("confirmStockPackBox 删除旧库存 stockList: {}", stockList);
        stockDao.deleteBatchIds(stockIdList);
        // 释放所有锁住的 stockIdKey
        for (Integer stockId : stockIdList) {
            redissonUtil.unLock(stockId);
        }
        log.info("confirmStockPackBox 确认拼盒操作成功 operator: {}", userService.getNowLoginUser().getUserName());
        return ResponseData.createSuccessResponseData("confirmStockPackBoxInfo", "拼盒操作成功");
    }

    /**
     * 扫码获取库存信息，拼箱
     * 如果扫的是盒，则可以，如果扫的是袋，则返回错误
     * 因为拼箱只能由盒拼成箱
     * 这里只返回展示数据，要点击确认拼盒之后才会触发拼箱功能
     * @param serialNumber 解析二维码得到的序列号，用于获取库存信息
     * @return 返回库存信息
     */
    @Override
    public ResponseData scanStockToPackCase(String serialNumber) {
        Stock stock = serialNumberFindStock(serialNumber);
        ResponseData checkResult = checkPackStock(stock, "scanStockToPackCase", "扫码拼箱", serialNumber);
        if (checkResult != null) return checkResult;
        if (stock.getIsPackCase().equals(StockConstant.IS_PACK_CASE.getPackStatus())) {
            log.info("scanStockToPackCase 扫码拼箱失败，当前库存已拼箱，需要先拆箱");
            return ResponseData.createFailResponseData("scanStockToPackCaseInfo", "扫码拼箱失败，当前库存已经拼箱，请先拆箱", ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }
        if (!stock.getType().equals(StockConstant.BOX.getType())) {
            log.info("scanStockToPackCase 扫码拼箱失败，只有盒才能用来拼箱，type: {}", stock.getType());
            return ResponseData.createFailResponseData("scanStockToPackCaseInfo", "扫码拼箱失败，只有盒才能用来拼箱", ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }
        // 不释放锁
        if (redissonUtil.tryLockWithoutLeaseTime(stock.getId())) {
            log.info("scanStockToPackCase 扫码拼箱 operator: {},  stock: {}", userService.getNowLoginUser().getUserName(), stock);
            return ResponseData.createSuccessResponseData("scanStockToPackCaseInfo", stock);
        } else {
            log.info("scanStockToPackCase 扫码拼箱失败，该库存正在被操作");
            return ResponseData.createFailResponseData("scanStockToPackCaseInfo", "扫码拼箱失败，该库存正在被使用，请稍后重试或使用其他库存", ResponseFailTypeConstant.SYSTEM_BUSY.getFailType());
        }
    }

    /**
     * 取消某个库存的拼箱操作
     * @param stockId 库存 id
     * @return 返回
     */
    @Override
    public ResponseData cancelTheStockPackCase(Integer stockId) {
        Stock stock = stockDao.selectById(stockId);
        if (stock == null) {
            log.info("cancelTheStockPackCase 库存取消拼箱失败，查无此库存 stockId: {}", stockId);
            return ResponseData.createFailResponseData("cancelTheStockPackCaseInfo", "库存取消拼箱失败，查无此库存", ResponseFailTypeConstant.DATA_NOT_EXIST.getFailType());
        }
        redissonUtil.unLock(stockId);
        log.info("cancelTheStockPackCase 库存取消拼箱成功");
        return ResponseData.createSuccessResponseData("cancelTheStockPackCaseInfo", "取消该库存的拼箱操作成功");
    }

    /**
     * 点击确认拼箱
     * @param stockIds 库存 id 列表
     * @return 返回拼箱是否成功
     */
    @Override
    @Transactional
    public ResponseData confirmStockPackCase(String stockIds) {
        List<Integer> stockIdList = parseStockIds(stockIds);
        String serialNumber = createStockSerialNumber(); // 拼箱编号跟序列号一样
        if (serialNumber == null) {
            log.info("confirmStockPackCase 确认拼箱失败，序列号创建不成功");
            return ResponseData.createFailResponseData("confirmStockPackCaseInfo", "确认拼箱失败，拼箱编号创建不成功，请稍后重试", ResponseFailTypeConstant.SYSTEM_BUSY.getFailType());
        }
        List<Stock> stockList = stockDao.selectBatchIds(stockIdList);
        List<PackCaseStockLog> packCaseStockLogList = new ArrayList<>(stockList.size());
        // 校验数据 料号、型号、批号必须一致;库存类型必须是盒;库存本身必须没有拼箱;数量总和必须等于箱标准数量;库存所在仓库、货架必须一致;库存必须是在库状态
        // 已经拼箱的，不能拼，提示必须先拆箱
        Stock baseStock = stockList.get(0);
        if (!baseStock.getType().equals(StockConstant.BAG.getType())) {
            log.info("confirmStockPackCase 确认拼箱失败，只有盒才能用于拼箱 baseStock: {}", baseStock);
            return ResponseData.createFailResponseData("confirmStockPackCaseInfo", "确认拼箱失败，只有盒能用于拼箱", ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }
        // 根据料号查出箱标准数量
        int caseStandardQuantity = productDao.findCaseStandardQuantity(baseStock.getProMaterialNumber());
        int count = baseStock.getAmount();
        packCaseStockLogList.add(packCaseStockLogService.stockToPackCaseStockLog(baseStock, serialNumber));
        for (int i = 1; i < stockList.size(); i ++) {
            Stock stock = stockList.get(i);
            if (canPack(baseStock, stock) && stock.getType().equals(StockConstant.BAG.getType())) {
                count += stock.getAmount();
                packCaseStockLogList.add(packCaseStockLogService.stockToPackCaseStockLog(stock, serialNumber));
            } else {
                log.info("confirmStockPackCase 确认拼箱失败，数据有误 stock: {}, baseStock: {}", stock, baseStock);
                return ResponseData.createFailResponseData("confirmStockPackCaseInfo", "确认拼箱失败，数据有误，请检查。料号、型号、批号必须一致;库存类型必须是盒;库存本身必须没有拼箱;数量总和必须等于箱标准数量;库存所在仓库、货架必须一致;库存必须是在库状态。", ResponseFailTypeConstant.DATA_ERROR.getFailType());
            }
        }
        if (count != caseStandardQuantity) {
            log.info("confirmStockPackCase 确认拼箱失败，数量不等于箱标准数量，count: {}, caseStandardQuantity: {}", count, caseStandardQuantity);
            return ResponseData.createFailResponseData("confirmStockPackCaseInfo", "确认拼箱失败，数量不等于箱标准数量：" + caseStandardQuantity, ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }
        // 新增拼后的新库存
        Stock newStock = packCaseGetNewStock(baseStock, count, serialNumber);
        log.info("confirmStockPackCase 持久化数据 newStock: {}", newStock);
        stockDao.insert(newStock);
        for (PackCaseStockLog packCaseStockLog : packCaseStockLogList ) {
            log.info("confirmStockPackCase 持久化拼箱日志 packCaseStockLog: {}", packCaseStockLog);
            packCaseStockLogDao.insert(packCaseStockLog);
        }
        // 删除旧库存记录
        log.info("confirmStockPackCase 删除旧库存 stockList: {}", stockList);
        stockDao.deleteBatchIds(stockIdList);
        // 释放所有锁住的 stockIdKey
        for (Integer stockId : stockIdList) {
            redissonUtil.unLock(stockIds);
        }
        log.info("confirmStockPackCase 确认拼箱成功 operator: {}", userService.getNowLoginUser().getUserName());
        return ResponseData.createSuccessResponseData("confirmStockPackCaseInfo", "拼箱操作成功");
    }

    /**
     * 根据出货单号查询库存数据
     * @param goodsOrderNumber 出货单号
     * @return 返回
     */
    @Override
    public ResponseData findByGoodsOrderNumber(String goodsOrderNumber) {
        if (ObjectUtils.isEmpty(goodsOrderNumber)) {
            log.info("findByGoodsOrderNumber 出货单数据明细查询失败，出货单号为空");
            return ResponseData.createFailResponseData("findByGoodsOrderNumberInfo", "明细查询失败，出货单号为空", ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }
        List<Stock> stockList = stockDao.findByGoodsOrderNumber(goodsOrderNumber);
        log.info("findByGoodsOrderNumber 出货单数据明细查询结果 size: {}", stockList.size());
        return ResponseData.createSuccessResponseData("findByGoodsOrderNumberInfo", stockList);
    }

    /**
     * 移货架的时候，货架上所有库存的货架信息都必须修改
     * @param shelfCode 货架编码
     * @param newWareCode 新仓库编码
     */
    @Override
    @Transactional
    public void updateStockWareCode(String shelfCode, String newWareCode) {
        log.info("updateStockWareCode 修改货架上库存货物的仓库编码 shelfCode: {}, newWareCode: {}", shelfCode, newWareCode);
        stockDao.updateStockWareCode(shelfCode, newWareCode, LocalDateTime.now());
    }

    /**
     * 根据序列号查询库存
     * @param serialNumber 序列号
     * @return 返回库存
     */
    @Override
    public Stock serialNumberFindStock(String serialNumber) {
        try {
            return stockDao.findBySerialNumber(Long.parseLong(serialNumber));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 二维码解析成库存货物对象
     * @param qrCodeValue 二维码
     * @return 返回
     */
    @Override
    public Stock parseQrCodeValueToStock(String qrCodeValue) {
        try {
            return parseQrCodeValueToStock(qrCodeValue.split("-"));
        } catch (Exception e ){
            log.info("parseQrCodeValueToStock 二维码解析成库存对象失败，二维码数据有误 qrCodeValue: {}", qrCodeValue);
            return null;
        }
    }

    /**
     * 解析二维码获得库存对象
     * @param stockFieldValue 已根据 “-” 拆分的二维码数据
     * @return 返回库存对象
     */
    public Stock parseQrCodeValueToStock(String[] stockFieldValue) throws Exception {
        Stock stock = new Stock();
        stock.setProMaterialNumber(stockFieldValue[0]); // 料号
        stock.setProModel(stockFieldValue[1]); // 型号
        stock.setLotNumber(stockFieldValue[2]); // 批号
        stock.setAmount(Integer.parseInt(stockFieldValue[3])); // 数量
        stock.setType(new Byte(stockFieldValue[4])); // 类型
        stock.setSerialNumber(Long.parseLong(stockFieldValue[5])); // 序列号
        return stock;
    }

    /**
     * 检查是否有参数为空
     * @param keyList 参数名列表
     * @param requestParams 请求参数
     * @return 返回
     */
    private boolean isHaveEmptyParam(List<String> keyList, Map<String, String> requestParams) {
        for (String key : keyList) {
            if (ObjectUtils.isEmpty(requestParams.get(key))) return true;
        }
        return false;
    }

    /**
     *
     * 取消货物扫码出库的操作
     * 取消库存货物变更货架的操作
     * 取消库存货物变更仓库的操作
     * @param requestParams
     * String originShelfCode, 原来的货架编码
     * String originWareName, 原来的仓库名称
     * String originWareCode, 原来的仓库编码
     * Integer stockId, 库存 id
     * Integer stockLogId 操作日志 id
     */
    private ResponseData cancelStockOperate(Map<String, String> requestParams) {
        String methodName = requestParams.get("methodName");
        String operateName = requestParams.get("operateName");
        // 加锁逻辑 !redisService.isExistKey(stock.getId()) 防止该库存当前正在被拼盒或者拼箱。
        if (redissonUtil.tryLock(requestParams.get("stockId"))) {
            try {
                List<String> keyList = new ArrayList<>(5);
                keyList.add("originShelfCode");
                keyList.add("originWareName");
                keyList.add("originWareCode");
                keyList.add("stockId");
                keyList.add("stockLogId");
                if (isHaveEmptyParam(keyList, requestParams)) {
                    log.info(methodName + " " + operateName + "失败，参数错误");
                    return ResponseData.createFailResponseData(methodName + "Info", operateName + "失败，参数错误", ResponseFailTypeConstant.DATA_ERROR.getFailType());
                }
                User nowLoginUser = userService.getNowLoginUser();
                Stock stock = stockDao.selectById(requestParams.get("stockId"));
                stock.setShelfCode(requestParams.get("shelfCode"));
                stock.setWareCode(requestParams.get("wareCode"));
                stock.setWareName(requestParams.get("wareName"));
                stock.setUpdateTime(LocalDateTime.now());
                StockLog stockLog = stockLogDao.selectById(requestParams.get("stockLogId"));
                stockDao.updateById(stock);
                stockLogDao.deleteById(stockLog.getId());
                log.info(methodName + " " + operateName + "成功，operator: {}, 更新库存信息 stock: {}, 删除出库日志 stockLog: {}", nowLoginUser.getUserName(), stock, stockLog);
                return ResponseData.createSuccessResponseData(methodName + "Info", operateName + "成功");
            } finally {
                redissonUtil.unLock(requestParams.get("stockId"));
            }
        } else { // 加锁失败
            log.info(methodName + " " + operateName + "失败，该库存正在被操作");
            return ResponseData.createFailResponseData(methodName + "Info", operateName + "失败，该库存正在进行其他操作", ResponseFailTypeConstant.SYSTEM_BUSY.getFailType());
        }

    }

    /**
     * 解析库存 id 列表
     * @param stockIds 库存 id 列表
     * @return 返回 list
     */
    @Override
    public List<Integer> parseStockIds(String stockIds) {
        List<Integer> result = new ArrayList<>();
        if (ObjectUtils.isEmpty(stockIds)) return result;
        String[] ids = stockIds.split(",");
        for (String id : ids) {
            try {
                result.add(Integer.parseInt(id.trim()));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return result;
    }

    /**
     * 生成一个库存序列号
     * @return 返回序列号
     */
    private String createStockSerialNumber() {
        StringBuilder result = new StringBuilder();
        LocalDateTime now = LocalDateTime.now();
        result.append(now.getYear());
        result.append(now.getMonthValue());
        result.append(now.getDayOfMonth());
        result.append(now.getHour());
        result.append(now.getMinute());
        result.append(now.getSecond());
        String serialNumber = redisService.getSerialNumber();
        if (serialNumber == null) return null;
        result.append(serialNumber);
        return result.toString();
    }

    /**
     * 判断是否能拼
     * 料号、型号、批号必须一致;
     * 库存本身必须没有拼盒/拼箱;
     * 数量总和必须等于盒标准数量;
     * 库存所在仓库、货架必须一致;库存必须是在库状态
     * @param baseStock 基库存
     * @param stock 库存
     * @return 返回是否能拼
     */
    private boolean canPack(Stock baseStock, Stock stock) {
        // 所在货架必须一致
        return stock.getProMaterialNumber().equals(baseStock.getProMaterialNumber()) && // 料号一致
                stock.getProModel().equals(baseStock.getProModel()) &&  // 型号一致
                stock.getLotNumber().equals(baseStock.getLotNumber()) && // 批号一致
                stock.getStatus().equals(StockConstant.IN_WARE_HOUSE.getStatus()) && // 库存必须是在库状态
                stock.getIsPackBox().equals(StockConstant.UN_PACK_BOX.getPackStatus()) && // 没有拼盒
                stock.getIsPackCase().equals(StockConstant.UN_PACK_CASE.getPackStatus()) && // 没有拼箱
                stock.getWareName().equals(baseStock.getWareName()) && stock.getWareCode().equals(baseStock.getWareCode()) && // 所在仓库必须一致
                stock.getShelfCode().equals(baseStock.getShelfCode());
    }

    /**
     * 拼盒操作生成新库存
     * @param baseStock 基库存，用于获取料号、型号、批号、仓库、货架
     * @param count 数量
     * @param serialNumber 序列号
     * @return 返回新库存
     */
    private Stock packBoxGetNewStock(Stock baseStock, Integer count, String serialNumber) {
        return packOperateGetNewStock(baseStock, count, serialNumber, StockConstant.BOX.getType(),
                StockConstant.IS_PACK_BOX.getPackStatus(), serialNumber,
                StockConstant.UN_PACK_CASE.getPackStatus(), "");
    }

    /**
     * 拼箱操作生成新库存
     * @param baseStock 基库存，用于获取料号、型号、批号、仓库、货架
     * @param count 数量
     * @param serialNumber 序列号
     * @return 返回新库存
     */
    private Stock packCaseGetNewStock(Stock baseStock, Integer count, String serialNumber) {
        return packOperateGetNewStock(baseStock, count, serialNumber, StockConstant.CASE.getType(),
                StockConstant.UN_PACK_BOX.getPackStatus(), "",
                StockConstant.IS_PACK_CASE.getPackStatus(), serialNumber);
    }

    /**
     * 拼盒拼箱操作生成新库存
     * @param baseStock 基库存，用于获取料号、型号、批号、仓库、货架
     * @param count 数量
     * @param serialNumber 序列号
     * @param stockType 库存类型
     * @param isPackBox 是否拼盒
     * @param packBoxNumber 拼盒编码
     * @param isPackCase 是否拼箱
     * @param packCaseNumber 拼箱编码
     * @return 返回新库存
     */
    private Stock packOperateGetNewStock(Stock baseStock, Integer count, String serialNumber, Byte stockType, Byte isPackBox, String packBoxNumber, Byte isPackCase, String packCaseNumber) {
        // 可以利用拷贝工具类，但是这里没有用。工具类是通过反射去实现的
        // 这里属性不多，反射效率比较差，所以手动赋值就好了。
        Stock stock = new Stock();
        stock.setProMaterialNumber(baseStock.getProMaterialNumber());
        stock.setProModel(baseStock.getProModel());
        stock.setLotNumber(baseStock.getLotNumber());
        stock.setAmount(count);
        stock.setType(stockType);
        stock.setStatus(StockConstant.IN_WARE_HOUSE.getStatus());
        stock.setGoodsOrderNumber(0L);
        stock.setIsPackBox(isPackBox);
        stock.setPackBoxNumber(packBoxNumber);
        stock.setIsPackCase(isPackCase);
        stock.setPackCaseNumber(packCaseNumber);
        stock.setWareName(baseStock.getWareName());
        stock.setWareCode(baseStock.getWareCode());
        stock.setShelfCode(baseStock.getShelfCode());
        stock.setInTime(LocalDateTime.now());
        stock.setSerialNumber(Long.parseLong(serialNumber));
        stock.setCreateTime(LocalDateTime.now());
        stock.setUpdateTime(LocalDateTime.now());
        return stock;
    }

    /**
     * 检查库存是否存在，状态是否正确
     * @param stock 库存
     * @param methodName 方法名
     * @param operateName 操作名
     * @param serialNumber 序列号
     * @return 返回
     */
    private ResponseData checkPackStock(Stock stock, String methodName, String operateName, String serialNumber) {
        if (stock == null) {
            log.info(methodName + " " + operateName + "失败，序列号错误 serialNumber: {}", serialNumber);
            return ResponseData.createFailResponseData(methodName + "Info", "查无此库存，请检查序列号", ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }
        if (!stock.getStatus().equals(StockConstant.IN_WARE_HOUSE.getType())) {
            log.info(methodName + " " + operateName + "失败，库存不在仓库中");
            return ResponseData.createFailResponseData(methodName + "Info", operateName + "失败，该库存状态不是在库", ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }
        return null;
    }
    
}
