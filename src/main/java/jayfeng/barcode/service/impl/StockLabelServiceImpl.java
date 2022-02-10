package jayfeng.barcode.service.impl;

import com.google.zxing.WriterException;
import jayfeng.barcode.bean.bo.StockLabelBo;
import jayfeng.barcode.constant.ResponseFailTypeConstant;
import jayfeng.barcode.response.ResponseData;
import jayfeng.barcode.service.StockLabelService;
import jayfeng.barcode.util.PrintLabelUtil;
import jayfeng.barcode.util.QrCodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 库存标签业务逻辑层
 * @author JayFeng
 * @date 2021/11/8
 */
@Slf4j
@Service
public class StockLabelServiceImpl implements StockLabelService {

    @Autowired
    private PrintLabelUtil printLabelUtil;

    /**
     * 打印库存标签：袋、盒、箱
     * @param stockLabelBo 库存标签信息
     * @return 返回
     */
    @Override
    public ResponseData printStockLabel(StockLabelBo stockLabelBo) {
        int labelWidth = 480; // 图片宽度
        int labelHeight = 270; // 图片高度
        int leftTopX = 30, leftTopY = 50; // 左上角坐标
        int rightTopX = 450, rightTopY = 50; // 右上角坐标
        int leftBottomX = 30, leftBottomY = 249; // 左下角坐标
        int rightBottomX = 450, rightBottomY = 249; // 右下角坐标
        int wordsX = 40; // 文字 X 坐标
        int wordsY = 80; // 文字 Y 坐标
        int lineX = 30; // 第一条线左端点 X 坐标
        int lineY = 92; // 第一条线左端点 Y 坐标
        int lineLength = 280; // 线的长度
        int wordsInterval = 39; // 两行文字之间的间隔
        int lineInterval = 39; // 两条线之间的间隔
        BufferedImage label = new BufferedImage(labelWidth, labelHeight, BufferedImage.TYPE_INT_RGB);
        Font font = new Font("黑体", Font.PLAIN, 22);
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
        if (stockLabelBo.getType() == 0) graphics2D.drawString("真空袋标签", 30, 40);
        else if (stockLabelBo.getType() == 1) graphics2D.drawString("内盒标签", 30, 40);
        else if (stockLabelBo.getType() == 2) graphics2D.drawString("外箱标签", 30, 40);
        // 画边框
        graphics2D.setStroke(new BasicStroke(2)); // 画线的宽度
        graphics2D.drawLine(leftTopX, leftTopY, leftBottomX, leftBottomY);
        graphics2D.drawLine(leftTopX, leftTopY, rightTopX, rightTopY);
        graphics2D.drawLine(rightTopX, rightTopY, rightBottomX, rightBottomY);
        graphics2D.drawLine(leftBottomX, leftBottomY, rightBottomX, rightBottomY);

        // 料号
        graphics2D.drawString("料号：" + stockLabelBo.getProMaterialNumber(), wordsX, wordsY);
        graphics2D.drawLine(lineX, lineY, lineX + lineLength, lineY);
        wordsY += wordsInterval;
        lineY += lineInterval;
        // 型号
        graphics2D.drawString("型号：" + stockLabelBo.getProModel(), wordsX, wordsY);
        graphics2D.drawLine(lineX, lineY, lineX + lineLength, lineY);
        wordsY += wordsInterval;
        lineY += lineInterval;
        // 批号
        graphics2D.drawString("批号：" + stockLabelBo.getLotNumber(), wordsX, wordsY);
        graphics2D.drawLine(lineX, lineY, rightBottomX, lineY);
        wordsY += wordsInterval;
        lineY += lineInterval;
        // 数量
        graphics2D.drawString("数量：" + stockLabelBo.getAmount(), wordsX, wordsY);
        graphics2D.drawLine(lineX, lineY, lineX + lineLength, lineY);
        graphics2D.drawString("标签日期：", lineX + lineLength + 22, wordsY + 8);
        wordsY += wordsInterval;

        // 序列号
        LocalDateTime now = LocalDateTime.now();
        graphics2D.drawString("序列号：" + stockLabelBo.getSerialNumber(), wordsX, wordsY);
        graphics2D.drawString(now.getYear() + "-" + now.getMonthValue() + "-" + now.getDayOfMonth(), lineX + lineLength + 16, wordsY - 2);

        // 文字和二维码分隔线
        graphics2D.drawLine(lineX + lineLength, leftTopY, lineX + lineLength, leftBottomY);
        // 画入二维码
        QrCodeUtil qrCodeUtil = new QrCodeUtil();
        try {
            // 二维码码值：料号-型号-批号-数量-序列号
            String qrCodeValue = stockLabelBo.getProMaterialNumber() + "-" + stockLabelBo.getProModel() + "-" +
                                 stockLabelBo.getLotNumber() + "-" + stockLabelBo.getAmount() + "-" +
                                 stockLabelBo.getSerialNumber();
            graphics2D.drawImage(qrCodeUtil.drawQrCode(qrCodeValue, 110, 110),326, 55, null);
            ImageIO.write(label, "png", new File("D://test.png"));
        } catch (WriterException | IOException e) {
            log.info("printStockLabel 库存标签打印失败，生成标签出异常 stockLabelBo: {}", stockLabelBo);
            return ResponseData.createFailResponseData("printStockLabelInfo", "打印库存标签任务提交失败，未知错误，请稍后重试", ResponseFailTypeConstant.UNKNOWN_ERROR.getFailType());
        }
        log.info("printStockLabel 打印库存标签任务提交成功");
        return ResponseData.createSuccessResponseData("printStockLabelInfo", "打印库存标签任务提交成功");
    }

}
