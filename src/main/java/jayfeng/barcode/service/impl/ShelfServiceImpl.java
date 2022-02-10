package jayfeng.barcode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.zxing.WriterException;
import jayfeng.barcode.bean.Shelf;
import jayfeng.barcode.bean.User;
import jayfeng.barcode.bean.Warehouse;
import jayfeng.barcode.constant.ResponseFailMessageConstant;
import jayfeng.barcode.constant.ResponseFailTypeConstant;
import jayfeng.barcode.dao.ShelfDao;
import jayfeng.barcode.dao.WarehouseDao;
import jayfeng.barcode.handler.QueryConditionHandler;
import jayfeng.barcode.response.ResponseData;
import jayfeng.barcode.service.ShelfService;
import jayfeng.barcode.service.StockService;
import jayfeng.barcode.service.UserService;
import jayfeng.barcode.service.WarehouseService;
import jayfeng.barcode.util.EncryptUtil;
import jayfeng.barcode.util.QrCodeUtil;
import jayfeng.barcode.util.RedissonUtil;
import jayfeng.barcode.util.UserPermissionCheck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 货架信息业务逻辑层
 * @author JayFeng
 * @date 2021/10/20
 */
@Slf4j
@Service
public class ShelfServiceImpl implements ShelfService {

    @Autowired
    private ShelfDao shelfDao;
    @Autowired
    private WarehouseDao warehouseDao;
    @Autowired
    private UserService userService;
    @Autowired
    private ShelfService shelfService;
    @Autowired
    private WarehouseService warehouseService;
    @Autowired
    private StockService stockService;
    @Autowired
    private EncryptUtil encryptUtil;
    @Autowired
    private UserPermissionCheck userPermissionCheck;
    @Autowired
    private QueryConditionHandler queryConditionHandler;
    @Autowired
    private RedissonUtil redissonUtil;
    @Autowired
    private QrCodeUtil qrCodeUtil;

    /**
     * 分页查询货架信息
     * @param requestParams 请求参数
     * @param page 分页参数
     * @return 返回
     */
    @Override
    public ResponseData findShelfPage(Map<String, String> requestParams, Page<Shelf> page) {
        Map<String, String> queryTypeMap = new HashMap<>(4);
        queryTypeMap.put("shelfCode", QueryConditionHandler.EQUAL);
        queryTypeMap.put("wareName", QueryConditionHandler.EQUAL);
        QueryWrapper<Shelf> queryWrapper = new QueryWrapper<>();
        queryConditionHandler.handleQueryCondition(queryWrapper, queryTypeMap, requestParams);
        try {
            Map<String, String> queryDataMap = new HashMap<>(4);
            queryDataMap.put("shelfType", requestParams.get("shelfType"));
            queryConditionHandler.handleByteNumberQueryCondition(queryWrapper, queryDataMap);
        } catch (NumberFormatException e) {
            log.info("findShelfPage 分页查询货架信息失败，数字参数存在错误 requestParams: {}", requestParams);
            return ResponseData.createFailResponseData("findShelfPageInfo",
                    "查询失败，数字参数错误",
                    ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }

        Page<Shelf> dataPage = shelfDao.selectPage(page, queryWrapper);
        log.info("findShelfPage 分页查询货架信息结果 total: {}", dataPage.getTotal());
        return ResponseData.createSuccessResponseData("findShelfPageInfo", dataPage);
    }

    /**
     * 根据仓库编码，查询最新的货架编号
     * @param wareCode 仓库编码
     * @return 返回
     */
    @Override
    public ResponseData findShelfNumberByWareCode(String wareCode) {
        Integer shelfNumber = shelfDao.findShelfNumberByWareCode(wareCode);
        log.info("findShelfNumberByWareCode 根据仓库编码查询最新的货架编号 wareCode: {}, shelfNumber: {}", wareCode, shelfNumber);
        return ResponseData.createSuccessResponseData("findShelfNumberByWareCodeInfo", shelfNumber);
    }

    /**
     * 添加货架信息
     * @param shelf 待添加的货架
     * @param password 当前已登录的用户的密码
     * @return 返回
     */
    @Override
    @Transactional
    public ResponseData addShelf(Shelf shelf, String password) {
        User nowLoginUser = userService.getNowLoginUser();
        Warehouse warehouse = warehouseDao.findWareHouseByWareCode(shelf.getWareCode());
        if (ObjectUtils.isEmpty(warehouse)) {
            log.info("addShelf 添加货架信息失败，仓库不存在 warehouse: {}", shelf.getWareCode());
            return ResponseData.createFailResponseData("addShelfInfo", "添加失败，仓库不存在", ResponseFailTypeConstant.DATA_NOT_EXIST.getFailType());
        }
        // 加锁逻辑
        if (redissonUtil.tryLock(shelf.getShelfCode())) {
            // 当前登录的用户是否为该仓库的管理员
            if (warehouse.getManager().equals(nowLoginUser.getUserName())) {
                // 货架是否已存在
                if (shelfDao.findIdByShelfCode(shelf.getShelfCode()) != null) {
                    log.info("addShelf 添加货架信息失败，该货架已经存在");
                    return ResponseData.createFailResponseData("addShelfInfo",
                            "添加失败，货架已存在",
                            ResponseFailTypeConstant.DATA_ALREADY_EXIST.getFailType());
                } else {
                    // 密码是否匹配
                    if (encryptUtil.matches(password, nowLoginUser.getPassword())) {
                        shelf.setShelfQrCode(shelf.getShelfType() + "_" + shelf.getShelfCode() + "_" + shelf.getWareCode()); // 货架二维码 货架编码-仓库编码
                        shelf.setCreateTime(LocalDateTime.now());
                        shelf.setUpdateTime(shelf.getCreateTime());
                        shelfDao.insert(shelf);
                        log.info("addShelf 添加货架信息成功 operator: {}, shelf: {}", nowLoginUser.getUserName(), shelf);
                        return ResponseData.createSuccessResponseData("addShelfInfo", "货架信息添加成功");
                    } else {
                        log.info("addShelf 添加货架信息失败，密码不匹配");
                        return ResponseData.createFailResponseData("addShelfInfo",
                                ResponseFailMessageConstant.ADD_PASSWORD_ERROR.getFailMessage(),
                                ResponseFailTypeConstant.PASSWORD_ERROR.getFailType());
                    }
                }
            } else {
                log.info("addShelf 添加货架信息失败，当前登录的账号不是该仓库的管理员");
                return ResponseData.createFailResponseData("addShelfInfo",
                        "添加失败，您不是该仓库的管理员",
                        ResponseFailTypeConstant.NO_OPERATION_PERMISSION.getFailType());
            }
        }
        log.info("addShelf 添加货架信息失败，获取锁失败");
        return ResponseData.createFailResponseData("addShelfInfo", "操作该货架的人数过多，请稍后重试", ResponseFailTypeConstant.SYSTEM_BUSY.getFailType());
    }

    /**
     * @param shelf 待更新的货架
     * @param password 当前已登录的用户的密码
     * @return 返回
     */
    @Override
    public ResponseData updateShelf(Shelf shelf, String password) {
        User nowLoginUser = userService.getNowLoginUser();
        if (userPermissionCheck.haveUpdatePermission(nowLoginUser)) {
            if (encryptUtil.matches(password, nowLoginUser.getPassword())) {
                shelfService.updateShelfMessage(shelf, nowLoginUser.getUserName());
                return ResponseData.createSuccessResponseData("updateShelfInfo", "货架信息更新成功");
            } else {
                log.info("updateShelf 更新货架信息失败，密码错误");
                return ResponseData.createFailResponseData("updateShelfInfo",
                        ResponseFailMessageConstant.UPDATE_PASSWORD_ERROR.getFailMessage(),
                        ResponseFailTypeConstant.PASSWORD_ERROR.getFailType());
            }
        } else {
            log.info("updateShelf 更新货架信息失败，没有权限");
            return ResponseData.createFailResponseData("updateShelfInfo",
                    ResponseFailMessageConstant.NO_UPDATE_PERMISSION.getFailMessage(),
                    ResponseFailTypeConstant.NO_OPERATION_PERMISSION.getFailType());
        }
    }

    /**
     * 更新货架信息
     * 修改货架所在的仓库(比较少)，如果修改所在仓库，则需要修改货架标签二维码，需要重贴标签
     * @param shelf 待更新的货架
     */
    @Override
    public void updateShelfMessage(Shelf shelf, String userName) {
        Shelf oldShelf = shelfDao.selectById(shelf.getId());
        shelf.setShelfQrCode(shelf.getShelfType() + "_" + shelf.getShelfCode() + "_" + shelf.getWareCode());
        shelf.setUpdateTime(LocalDateTime.now());
        shelfDao.updateById(shelf);
        log.info("updateShelf 更新货架信息成功 operator: {}, oldShelf: {}, newShelf: {}", userName, oldShelf, shelf);
    }

    /**
     * @param shelf 待删除的货架
     * @param password 当前已登录的用户的密码
     * @return 返回
     */
    @Override
    public ResponseData deleteShelf(Shelf shelf, String password) {
        User nowLoginUser = userService.getNowLoginUser();
        Warehouse warehouse = warehouseDao.findWareHouseByWareCode(shelf.getWareCode());
        if (ObjectUtils.isEmpty(warehouse)) {
            log.info("deleteShelf 删除货架信息成功，仓库不存在 warehouse: {}", shelf.getWareCode());
            return ResponseData.createFailResponseData("deleteShelfInfo", "删除失败，仓库不存在", ResponseFailTypeConstant.DATA_NOT_EXIST.getFailType());
        }
        // 加锁逻辑
        if (redissonUtil.tryLock(shelf.getShelfCode())) {
            // 是否有操作权限
            if (userPermissionCheck.haveUpdatePermission(nowLoginUser)) {
                // 密码是否匹配
                if (encryptUtil.matches(password, nowLoginUser.getPassword())) {
                    shelfDao.deleteById(shelf.getId());
                    log.info("deleteShelf 删除货架信息成功 operator: {}, shelf: {}", nowLoginUser.getUserName(), shelf);
                    return ResponseData.createSuccessResponseData("deleteShelfInfo", "货架信息删除成功");
                } else {
                    log.info("deleteShelf 删除货架信息失败，密码错误");
                    return ResponseData.createFailResponseData("deleteShelfInfo",
                            ResponseFailMessageConstant.DELETE_PASSWORD_ERROR.getFailMessage(),
                            ResponseFailTypeConstant.PASSWORD_ERROR.getFailType());
                }
            } else {
                log.info("deleteShelf 删除货架信息失败，没有权限");
                return ResponseData.createFailResponseData("deleteShelfInfo",
                        ResponseFailMessageConstant.NO_DELETE_PERMISSION.getFailMessage(),
                        ResponseFailTypeConstant.NO_OPERATION_PERMISSION.getFailType());
            }
        }
        log.info("deleteShelf 删除货架信息失败，获取锁失败");
        return ResponseData.createFailResponseData("deleteShelfInfo", "操作该货架的人数过多，请稍后重试", ResponseFailTypeConstant.SYSTEM_BUSY.getFailType());
    }

    /**
     * 移货架  从一个仓库移动到另外一个仓库
     * @param shelf 待移动的仓库
     * @param targetWareCode 目标仓库编码
     * @return 返回
     */
    @Override
    @Transactional
    public ResponseData moveShelf(Shelf shelf, String targetWareCode) {
        String wareCode = shelf.getWareCode();
        Warehouse warehouse = warehouseDao.findWareHouseByWareCode(wareCode);
        Warehouse targetWarehouse = warehouseDao.findWareHouseByWareCode(targetWareCode);
        // 判断是否为同类型的仓库
        if (warehouseService.isSameTypeWarehouse(wareCode, targetWareCode)) {
            boolean lockShelf = redissonUtil.tryLock(shelf.getShelfCode());
            boolean lockWarehouse = redissonUtil.tryLock(wareCode);
            boolean lockTargetWarehouse = redissonUtil.tryLock(targetWareCode);
            // 货架、源仓库、目标仓库都必须加锁成功才能执行移货架逻辑
            if (lockShelf) {
                if (lockWarehouse) {
                    if (lockTargetWarehouse) {
                        // 3 个都加锁成功，处理移货架逻辑
                        try {
                            // 开始数据逻辑修改
                            // 修改货架上库存货物的仓库编码
                            stockService.updateStockWareCode(shelf.getShelfCode(), targetWareCode);
                            // 修改货架所在的仓库编码，货架二维码
                            shelf.setWareCode(targetWareCode);
                            shelf.setShelfQrCode(shelf.getShelfType() + "_" + shelf.getShelfCode() + "_" + targetWareCode);
                            shelf.setUpdateTime(LocalDateTime.now());

                            // 持久化数据
                            shelfDao.updateById(shelf);
                            warehouseDao.updateById(warehouse);
                            warehouseDao.updateById(targetWarehouse);

                            log.info("moveShelf 货架移动成功 operator: {}，oldWarehouse: {}, newWarehouse: {}", userService.getNowLoginUser().getUserName(), wareCode, targetWareCode);
                            return ResponseData.createSuccessResponseData("moveShelfInfo", "货架移动成功");

                        } finally { // 保证释放锁
                            redissonUtil.unLock(shelf.getShelfCode());
                            redissonUtil.unLock(wareCode);
                            redissonUtil.unLock(targetWareCode);
                        }
                    } else { // 只有前两个加锁成功
                        log.info("moveShelf 移货架失败，目标仓库加锁失败");
                        redissonUtil.unLock(shelf.getShelfCode());
                        redissonUtil.unLock(wareCode);
                    }
                } else { // 只有第一个加锁成功
                    log.info("moveShelf 移货架失败，源仓库加锁失败");
                    redissonUtil.unLock(shelf.getShelfCode());
                }
            }
            log.info("moveShelf 移货架失败，获取锁失败");
            return ResponseData.createFailResponseData("moveShelfInfo", "操作该货架的人数过多，请稍后重试", ResponseFailTypeConstant.SYSTEM_BUSY.getFailType());
        } else { // 如果不是同类型的仓库
            log.info("moveShelf 目标仓库与源仓库不一致, wareCode: {}, targetWareCode: {}", wareCode, targetWareCode);
            return ResponseData.createFailResponseData("moveShelfInfo", "货架移动失败，目标仓库与源仓库不一致", ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }
    }

    /**
     * 打印货架标签
     * @param shelf 货架
     * @return 返回
     */
    @Override
    public ResponseData printShelfLabel(Shelf shelf) {
        int labelWidth = 480; // 图片宽度
        int labelHeight = 270; // 图片高度
        int leftTopX = 30, leftTopY = 50; // 左上角坐标
        int rightTopX = 450, rightTopY = 50; // 右上角坐标
        int leftBottomX = 30, leftBottomY = 249; // 左下角坐标
        int rightBottomX = 450, rightBottomY = 249; // 右下角坐标
        int wordsX = 40; // 文字 X 坐标
        int wordsY = 85; // 文字 Y 坐标
        int lineX = 30; // 第一条线左端点 X 坐标
        int lineY = 100; // 第一条线左端点 Y 坐标
        int lineLength = 270; // 线的长度
        int wordsInterval = 50; // 两行文字之间的间隔
        int lineInterval = 50; // 两条线之间的间隔
        BufferedImage label = new BufferedImage(labelWidth, labelHeight, BufferedImage.TYPE_INT_RGB);
        Font font = new Font("黑体", Font.PLAIN, 26);
        Graphics2D graphics2D = label.createGraphics();
        graphics2D.fillRect(0, 0, labelWidth, labelHeight);
        graphics2D.setColor(Color.BLACK); // 画笔颜色
        graphics2D.setBackground(Color.WHITE); // 背景颜色
        graphics2D.setFont(font); // 字体
        //消除文字锯齿
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        //消除画图锯齿
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 标签名称
        graphics2D.drawString("货架标签", 30, 40);
        // 画边框
        graphics2D.setStroke(new BasicStroke(2)); // 画线的宽度
        graphics2D.drawLine(leftTopX, leftTopY, leftBottomX, leftBottomY);
        graphics2D.drawLine(leftTopX, leftTopY, rightTopX, rightTopY);
        graphics2D.drawLine(rightTopX, rightTopY, rightBottomX, rightBottomY);
        graphics2D.drawLine(leftBottomX, leftBottomY, rightBottomX, rightBottomY);

        // 货架编码
        graphics2D.drawString("货架编码：" + shelf.getShelfCode(), wordsX, wordsY);
        graphics2D.drawLine(lineX, lineY, lineX + lineLength, lineY);
        wordsY += wordsInterval;
        lineY += lineInterval;
        // 货架类型
        byte type = shelf.getShelfType();
        if (type == 0) graphics2D.drawString("货架类型：袋货架", wordsX, wordsY);
        else if (type == 1) graphics2D.drawString("货架类型：盒货架", wordsX, wordsY);
        else if (type == 2) graphics2D.drawString("货架类型：箱货架", wordsX, wordsY);
        else {
            log.info("printShelfLabel 货架标签生成失败 货架类型错误 type: {}", type);
            return ResponseData.createFailResponseData("printShelfLabelInfo", "货架标签打印失败，货架类型错误", ResponseFailTypeConstant.DATA_ERROR.getFailType());
        }
        graphics2D.drawLine(lineX, lineY, lineX + lineLength, lineY);
        wordsY += wordsInterval;
        lineY += lineInterval;
        // 仓库编码
        graphics2D.drawString("仓库编码：" + shelf.getWareCode(), wordsX, wordsY);
        graphics2D.drawLine(lineX, lineY, rightBottomX, lineY);
        wordsY += wordsInterval;
        // 仓库名称
        graphics2D.drawString("仓库名称：" + shelf.getWareName(), wordsX, wordsY);
        // 标签打印日期
        LocalDateTime now = LocalDateTime.now();
        graphics2D.drawString(now.getYear() + "-" + now.getMonthValue() + "-" + now.getDayOfMonth(), lineX + lineLength + 11, wordsY);

        // 文字和二维码分隔线
        graphics2D.drawLine(lineX + lineLength, leftTopY, lineX + lineLength, leftBottomY);
        // 画入二维码
        try {
            graphics2D.drawImage(qrCodeUtil.drawQrCode(shelf.getShelfQrCode()),313, 63, null);
            ImageIO.write(label, "png", new File("D://test.png"));
        } catch (WriterException | IOException e) {
            log.info("printShelfLabel 货架标签生成失败，画标签出异常 shelf: {}", shelf);
            return ResponseData.createFailResponseData("printShelfLabelInfo", "打印货架标签任务提交失败，未知错误，稍后重试", ResponseFailTypeConstant.UNKNOWN_ERROR.getFailType());
        }
        log.info("printShelfLabel 货架标签生成成功，任务提交成功");
        return ResponseData.createSuccessResponseData("printShelfLabelInfo", "打印货架标签任务提交成功");
    }


}
