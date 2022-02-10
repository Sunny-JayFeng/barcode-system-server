package jayfeng.barcode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.zxing.WriterException;
import jayfeng.barcode.bean.User;
import jayfeng.barcode.bean.Warehouse;
import jayfeng.barcode.constant.ResponseFailMessageConstant;
import jayfeng.barcode.constant.ResponseFailTypeConstant;
import jayfeng.barcode.constant.WarehouseConstant;
import jayfeng.barcode.dao.WarehouseDao;
import jayfeng.barcode.handler.QueryConditionHandler;
import jayfeng.barcode.response.ResponseData;
import jayfeng.barcode.service.UserService;
import jayfeng.barcode.service.WarehouseService;
import jayfeng.barcode.util.EncryptUtil;
import jayfeng.barcode.util.QrCodeUtil;
import jayfeng.barcode.util.UserPermissionCheck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仓库信息业务逻辑层
 * @author JayFeng
 * @date 2021/10/21
 */
@Slf4j
@Service
public class WarehouseServiceImpl implements WarehouseService {

    @Autowired
    private WarehouseDao warehouseDao;
    @Autowired
    private UserService userService;
    @Autowired
    private WarehouseService warehouseService;
    @Autowired
    private UserPermissionCheck userPermissionCheck;
    @Autowired
    private QueryConditionHandler queryConditionHandler;
    @Autowired
    private EncryptUtil encryptUtil;

    /**
     * 分页查询仓库信息
     * @param requestParams 查询参数
     * @param page 分页参数
     * @return 返回
     */
    @Override
    public ResponseData findWarehousePage(Map<String, String> requestParams, Page<Warehouse> page) {
        Map<String, String> queryParamsMap = new HashMap<>(4);
        queryParamsMap.put("wareCode", requestParams.get("wareCode"));
        queryParamsMap.put("wareName", requestParams.get("wareName"));
        queryParamsMap.put("manager", requestParams.get("manager"));
        QueryWrapper<Warehouse> queryWrapper = new QueryWrapper<>();
        queryConditionHandler.handleEqualQueryCondition(queryWrapper, queryParamsMap);
        Page<Warehouse> dataPage = warehouseDao.selectPage(page, queryWrapper);
        log.info("findWarehousePage 分页查询仓库信息结果 total: {}", dataPage.getTotal());
        return ResponseData.createSuccessResponseData("findWarehousePageInfo", dataPage);
    }

    /**
     * 查询所有仓库信息
     * @return 返回
     */
    @Override
    public ResponseData findWarehouse() {
        QueryWrapper<Warehouse> queryWrapper = new QueryWrapper<>();
        List<Warehouse> warehouseList = warehouseDao.selectList(queryWrapper);
        log.info("findWarehouse 查询所有仓库信息结果 warehouseListSize: {}", warehouseList.size());
        return ResponseData.createSuccessResponseData("findWarehouseInfo", warehouseList);
    }

    /**
     * 添加仓库信息
     * @param wareHouse 待添加的仓库信息
     * @param password 当前已登录的用户的密码
     * @return 返回
     */
    @Override
    public ResponseData addWarehouse(Warehouse wareHouse, String password) {
        User nowLoginUser = userService.getNowLoginUser();
        if (userPermissionCheck.haveAddPermission(nowLoginUser)) {
            if (encryptUtil.matches(password, nowLoginUser.getPassword())) {
                if (warehouseDao.findWareHouseByWareCode(wareHouse.getWareCode()) != null) {
                    log.info("addWarehouse 添加失败，仓库已存在");
                    return ResponseData.createFailResponseData("addWarehouseInfo",
                            "添加失败，仓库已存在",
                            ResponseFailTypeConstant.DATA_ALREADY_EXIST.getFailType());
                }
                wareHouse.setWarehouseQrCode(wareHouse.getWareCode() + "-" + wareHouse.getManager()); // 仓库标签二维码 仓库管理员-仓库编码
                wareHouse.setCreateTime(LocalDateTime.now());
                wareHouse.setUpdateTime(wareHouse.getCreateTime());
                warehouseDao.insert(wareHouse);
                log.info("addWarehouse 添加仓库信息成功 operator: {}, warehouse: {}", nowLoginUser.getUserName(), wareHouse);
                return ResponseData.createSuccessResponseData("addWarehouseInfo", "仓库信息添加成功");
            } else {
                log.info("addWarehouse 添加仓库信息失败，密码错误");
                return ResponseData.createFailResponseData("addWarehouseInfo",
                        ResponseFailMessageConstant.ADD_PASSWORD_ERROR.getFailMessage(),
                        ResponseFailTypeConstant.PASSWORD_ERROR.getFailType());
            }
        } else {
            log.info("addWarehouse 添加仓库信息失败，没有权限");
            return ResponseData.createFailResponseData("addWarehouseInfo",
                    ResponseFailMessageConstant.NO_ADD_PERMISSION.getFailMessage(),
                    ResponseFailTypeConstant.NO_OPERATION_PERMISSION.getFailType());
        }
    }

    /**
     * 修改仓库信息
     * @param wareHouse 待添加的仓库信息
     * @param password 当前已登录的用户的密码
     * @return 返回
     */
    @Override
    public ResponseData updateWarehouse(Warehouse wareHouse, String password) {
        User nowLoginUser = userService.getNowLoginUser();
        if (userPermissionCheck.haveUpdatePermission(nowLoginUser)) {
            if (encryptUtil.matches(password, nowLoginUser.getPassword())) {
                warehouseService.updateWarehouseMessage(wareHouse, nowLoginUser.getUserName());
                return ResponseData.createSuccessResponseData("updateWarehouseInfo", "仓库信息修改成功");
            } else {
                log.info("updateWarehouse 修改仓库信息失败，密码错误");
                return ResponseData.createFailResponseData("updateWarehouseInfo",
                        ResponseFailMessageConstant.UPDATE_PASSWORD_ERROR.getFailMessage(),
                        ResponseFailTypeConstant.PASSWORD_ERROR.getFailType());
            }
        } else {
            log.info("updateWarehouse 修改仓库信息失败，没有权限");
            return ResponseData.createFailResponseData("updateWarehouseInfo",
                    ResponseFailMessageConstant.NO_UPDATE_PERMISSION.getFailMessage(),
                    ResponseFailTypeConstant.NO_OPERATION_PERMISSION.getFailType());
        }
    }

    /**
     * 更新仓库信息 -- 主要是：增删货架个数、
     * 修改仓库管理员(比较少) 如果修改仓库管理员，那么仓库二维码需要修改，重贴标签
     * @param warehouse 待更新仓库
     * @param userName 操作人
     */
    public void updateWarehouseMessage(Warehouse warehouse, String userName) {
        Warehouse oldWarehouse = warehouseDao.selectById(warehouse.getId());
        warehouse.setWarehouseQrCode(warehouse.getManager() + "-" + warehouse.getWareCode());
        warehouse.setUpdateTime(LocalDateTime.now());
        warehouseDao.updateById(warehouse);
        log.info("deleteWarehouse 更新仓库信息成功 operator: {}, oldWarehouse: {}, newWarehouse: {}", userName, oldWarehouse, warehouse);
    }

    /**
     * 以非事务方式更新仓库信息
     * @param warehouse 仓库信息
     */
    @Override
    public void notSupportedUpdateWarehouse(Warehouse warehouse) {
        warehouseDao.updateById(warehouse);
    }

    /**
     * 删除仓库信息
     * @param warehouse 待添加的仓库信息
     * @param password 当前已登录的用户的密码
     * @return 返回
     */
    @Override
    public ResponseData deleteWarehouse(Warehouse warehouse, String password) {
        User nowLoginUser = userService.getNowLoginUser();
        if (userPermissionCheck.haveDeletePermission(nowLoginUser)) {
            if (encryptUtil.matches(password, nowLoginUser.getPassword())) {
                warehouseDao.deleteById(warehouse.getId());
                log.info("deleteWarehouse 删除仓库信息成功 operator: {}, warehouse: {}", nowLoginUser.getUserName(), warehouse);
                return ResponseData.createSuccessResponseData("deleteWarehouseInfo", "仓库信息删除成功");
            } else {
                log.info("deleteWarehouse 删除仓库信息失败，密码错误");
                return ResponseData.createFailResponseData("deleteWarehouseInfo",
                        ResponseFailMessageConstant.DELETE_PASSWORD_ERROR.getFailMessage(),
                        ResponseFailTypeConstant.PASSWORD_ERROR.getFailType());
            }
        } else {
            log.info("deleteWarehouse 删除仓库信息失败，没有权限");
            return ResponseData.createFailResponseData("deleteWarehouseInfo",
                    ResponseFailMessageConstant.NO_DELETE_PERMISSION.getFailMessage(),
                    ResponseFailTypeConstant.NO_OPERATION_PERMISSION.getFailType());
        }
    }

    /**
     * 验证是否为良品仓
     * @param wareCode 仓库编码
     * @return 返回
     */
    @Override
    public boolean isQualifiedProductsWarehouse(String wareCode) {
        return !ObjectUtils.isEmpty(wareCode) && wareCode.startsWith(WarehouseConstant.QP_STARTS_WIDTH.getStartsWidth());
    }

    /**
     * 验证是否为不良品仓
     * @param wareCode 仓库编码
     * @return 返回
     */
    @Override
    public boolean isUnQualifiedProductsWarehouse(String wareCode) {
        return !ObjectUtils.isEmpty(wareCode) && wareCode.startsWith(WarehouseConstant.UQP_STARTS_WIDTH.getStartsWidth());
    }

    /**
     * 验证是否为检测仓
     * @param wareCode 仓库编码
     * @return 返回
     */
    @Override
    public boolean isInspectionWarehouse(String wareCode) {
        return !ObjectUtils.isEmpty(wareCode) && wareCode.startsWith(WarehouseConstant.INS_STARTS_WIDTH.getStartsWidth());
    }

    /**
     * 验证是否为出货仓
     * @param wareCode 仓库编码
     * @return 返回
     */
    @Override
    public boolean isShipmentWarehouse(String wareCode) {
        return !ObjectUtils.isEmpty(wareCode) && wareCode.startsWith(WarehouseConstant.SHIP_WIDTH.getStartsWidth());
    }

    /**
     * 两个仓库是否为同类型的仓库
     * @param wareCode1 仓库编码1
     * @param wareCode2 仓库编码2
     * @return 返回
     */
    @Override
    public boolean isSameTypeWarehouse(String wareCode1, String wareCode2) {
        if (ObjectUtils.isEmpty(wareCode1) || ObjectUtils.isEmpty(wareCode2)) return false;
        return wareCode1.charAt(0) == wareCode2.charAt(0) &&
                wareCode1.charAt(1) == wareCode2.charAt(1) &&
                wareCode1.charAt(2) == wareCode2.charAt(2);
    }

    /**
     * 打印仓库标签
     * @param warehouse 仓库信息
     * @return 返回
     */
    @Override
    public ResponseData printWarehouseLabel(Warehouse warehouse) {
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
        int lineLength = 280; // 线的长度
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
        graphics2D.drawString("仓库标签", 30, 40);
        // 画边框
        graphics2D.setStroke(new BasicStroke(2)); // 画线的宽度
        graphics2D.drawLine(leftTopX, leftTopY, leftBottomX, leftBottomY);
        graphics2D.drawLine(leftTopX, leftTopY, rightTopX, rightTopY);
        graphics2D.drawLine(rightTopX, rightTopY, rightBottomX, rightBottomY);
        graphics2D.drawLine(leftBottomX, leftBottomY, rightBottomX, rightBottomY);

        // 货架编码
        graphics2D.drawString("仓库编码：" + warehouse.getWareCode(), wordsX, wordsY);
        graphics2D.drawLine(lineX, lineY, lineX + lineLength, lineY);
        wordsY += wordsInterval;
        lineY += lineInterval;
        // 货架类型
        graphics2D.drawString("仓库名称：" + warehouse.getWareName(), wordsX, wordsY);
        graphics2D.drawLine(lineX, lineY, lineX + lineLength, lineY);
        wordsY += wordsInterval;
        lineY += lineInterval;
        // 仓库编码
        graphics2D.drawString("管理人员：" + warehouse.getManager(), wordsX, wordsY);
        graphics2D.drawLine(lineX, lineY, rightBottomX, lineY);
        wordsY += wordsInterval;
        // 标签打印日期
        LocalDateTime now = LocalDateTime.now();
        graphics2D.drawString("标签日期：" + now.getYear() + "-" + now.getMonthValue() + "-" + now.getDayOfMonth(), wordsX, wordsY);
        // 文字和二维码分隔线
        graphics2D.drawLine(lineX + lineLength, leftTopY, lineX + lineLength, leftBottomY);
        // 画入二维码
        QrCodeUtil qrCodeUtil = new QrCodeUtil();
        try {
            graphics2D.drawImage(qrCodeUtil.drawQrCode(warehouse.getWarehouseQrCode()),318, 63, null);
            ImageIO.write(label, "png", new File("D://test.png"));
        } catch (WriterException | IOException e) {
            log.info("printWarehouseLabel 仓库标签生成失败，画标签出现异常 warehouse: {}", warehouse);
            return ResponseData.createFailResponseData("printWarehouseLabelInfo", "打印货架标签任务提交失败，未知错误，请稍后重试", ResponseFailTypeConstant.UNKNOWN_ERROR.getFailType());
        }
        log.info("printWarehouseLabel 打印仓库标签任务提交成功");
        return ResponseData.createSuccessResponseData("printWarehouseLabelInfo", "打印仓库标签任务提交成功");
    }

}
