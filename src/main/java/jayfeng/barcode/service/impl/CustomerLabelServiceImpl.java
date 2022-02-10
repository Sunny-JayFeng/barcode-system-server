package jayfeng.barcode.service.impl;

import com.google.zxing.WriterException;
import jayfeng.barcode.bean.bo.CustomerLabelBo;
import jayfeng.barcode.constant.ResponseFailTypeConstant;
import jayfeng.barcode.response.ResponseData;
import jayfeng.barcode.service.CustomerLabelService;
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
import java.io.InputStream;
import java.time.LocalDateTime;

/**
 * 客户定制标签业务逻辑层
 * @author JayFeng
 * @date 2021/11/8
 */
@Slf4j
@Service
public class CustomerLabelServiceImpl implements CustomerLabelService {

    @Autowired
    private PrintLabelUtil printLabelUtil;

    /**
     * 打印客户定制标签
     * @param customerLabelBo 标签信息
     * @return 返回
     */
    @Override
    public ResponseData printCustomerLabel(CustomerLabelBo customerLabelBo) {
        String customerName = customerLabelBo.getCustomerName();
        switch (customerName) {
            case "wangShu": return wangShuCustomerLabel(customerLabelBo);
            case "bingLun": return bingLunCustomerLabel(customerLabelBo);
            case "guiPo": return guiPoCustomerLabel(customerLabelBo);
            case "dolphin": return dolphinCustomerLabel(customerLabelBo);
            case "whale": return whaleCustomerLabel(customerLabelBo);
        }
        log.info("printCustomerLabel 客户定制标签打印失败，不存在该客户的定制标签 customerLabelBo: {}", customerLabelBo);
        return ResponseData.createFailResponseData("printCustomerLabelInfo", "客户定制标签打印失败，不存在该客户的定制标签", ResponseFailTypeConstant.DATA_NOT_EXIST.getFailType());
    }

    /**
     * 客户定制标签：望舒
     * @param customerLabelBo 标签信息
     * @return 返回
     */
    private ResponseData wangShuCustomerLabel(CustomerLabelBo customerLabelBo) {
        int labelWidth = 480; // 图片宽度
        int labelHeight = 270; // 图片高度
        int wordsX = 40; // 文字 X 坐标
        int wordsY = 45; // 文字 Y 坐标
        int wordsInterval = 39; // 两行文字之间的间隔
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

        // 料号
        graphics2D.drawString("料号：" + customerLabelBo.getProMaterialNumber(), wordsX, wordsY);
        wordsY += wordsInterval;
        // 型号
        graphics2D.drawString("型号：" + customerLabelBo.getProModel(), wordsX, wordsY);
        wordsY += wordsInterval;
        // 批号
        graphics2D.drawString("批号：" + customerLabelBo.getLotNumber(), wordsX, wordsY);
        wordsY += wordsInterval;
        // 数量
        graphics2D.drawString("数量：" + customerLabelBo.getAmount(), wordsX, wordsY);
        wordsY += wordsInterval;

        // 序列号
        graphics2D.drawString("序列号：" + customerLabelBo.getSerialNumber(), wordsX, wordsY);
        wordsY += wordsInterval;

        // 标签日期
        LocalDateTime now = LocalDateTime.now();
        graphics2D.drawString("标签日期：" + now.getYear() + "-" + now.getMonthValue() + "-" + now.getDayOfMonth(), wordsX, wordsY);

        // 画入二维码
        QrCodeUtil qrCodeUtil = new QrCodeUtil();
        String qrCodeValue = customerLabelBo.getCustomerName() + "-" +
                             customerLabelBo.getProMaterialNumber() + "-" +
                             customerLabelBo.getProModel() + "-" +
                             customerLabelBo.getLotNumber() + "-" +
                             customerLabelBo.getAmount();
        try {
            graphics2D.drawImage(qrCodeUtil.drawQrCode(qrCodeValue, 110, 110),326, 55, null);
            ImageIO.write(label, "png", new File("D://test.png"));
        } catch (WriterException | IOException e) {
            log.info("wangShuCustomerLabel 望舒定制标签生成失败，出现异常: {}", e.getMessage());
            return ResponseData.createFailResponseData("wangShuCustomerLabelInfo", "望舒定制标签打印失败，未知错误，请稍后再试", ResponseFailTypeConstant.UNKNOWN_ERROR.getFailType());
        }
        log.info("wangShuCustomerLabel 望舒定制标签打印任务提交成功");
        return ResponseData.createSuccessResponseData("wangShuCustomerLabelInfo", "望舒定制标签打印任务提交成功");
    }

    /**
     * 客户定制标签：冰轮
     * @param customerLabelBo 标签信息
     * @return 返回
     */
    private ResponseData bingLunCustomerLabel(CustomerLabelBo customerLabelBo) {
        int labelWidth = 480; // 图片宽度
        int labelHeight = 270; // 图片高度
        int wordsX = 30; // 文字 X 坐标
        int wordsY = 45; // 文字 Y 坐标
        int wordsInterval = 30; // 两行文字之间的间隔
        BufferedImage label = new BufferedImage(labelWidth, labelHeight, BufferedImage.TYPE_INT_RGB);
        Font font = new Font("黑体", Font.PLAIN, 18);
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

        // 料号 型号
        graphics2D.drawString("料号：" + customerLabelBo.getProMaterialNumber(), wordsX, wordsY);
        graphics2D.drawString("型号：" + customerLabelBo.getProModel(), wordsX + 180, wordsY);
        wordsY += wordsInterval;
        // 批号 数量
        graphics2D.drawString("批号：" + customerLabelBo.getLotNumber(), wordsX, wordsY);
        graphics2D.drawString("数量：" + customerLabelBo.getAmount(), wordsX + 180, wordsY);
        wordsY += wordsInterval;

        // 序列号
        graphics2D.drawString("序列号：" + customerLabelBo.getSerialNumber(), wordsX, wordsY);
        wordsY += wordsInterval + 20;

        // 公司名称
        graphics2D.drawString("公司名称：" + customerLabelBo.getCompanyName(), wordsX, wordsY);
        wordsY += wordsInterval;
        // 公司地址
        String address = customerLabelBo.getAddress();
        if (address == null) address = "";
        if (address.length() > 16) {
            graphics2D.drawString("公司地址：" + address.substring(0, 16), wordsX, wordsY);
            wordsY += 23;
            graphics2D.drawString(address.substring(16), wordsX + 90, wordsY);
        } else {
            graphics2D.drawString("公司地址：" + address, wordsX, wordsY);
        }
        wordsY += wordsInterval;

        // 标签日期
        LocalDateTime now = LocalDateTime.now();
        graphics2D.drawString("标签日期：" + now.getYear() + "-" + now.getMonthValue() + "-" + now.getDayOfMonth(), wordsX, wordsY);

        // 画入二维码
        QrCodeUtil qrCodeUtil = new QrCodeUtil();
        String qrCodeValue = customerLabelBo.getProMaterialNumber() + "-" +
                             customerLabelBo.getProModel() + "-" +
                             customerLabelBo.getLotNumber() + "-" +
                             customerLabelBo.getAmount();
        try {
            graphics2D.drawImage(qrCodeUtil.drawQrCode(qrCodeValue, 110, 110),341, 35, null);
            ImageIO.write(label, "png", new File("D://test.png"));
        } catch (WriterException | IOException e) {
            log.info("bingLunCustomerLabel 冰轮定制标签生成失败，出现异常: {}", e.getMessage());
            return ResponseData.createFailResponseData("bingLunCustomerLabelInfo", "冰轮定制标签打印失败，未知错误，请稍后重试", ResponseFailTypeConstant.UNKNOWN_ERROR.getFailType());
        }
        log.info("bingLunCustomerLabelInfo 打印冰轮定制标签任务提交成功");
        return ResponseData.createSuccessResponseData("bingLunCustomerLabelInfo", "冰轮定制标签打印任务提交成功");
    }

    /**
     * 客户定制标签：桂魄
     * @param customerLabelBo 标签信息
     * @return 返回
     */
    private ResponseData guiPoCustomerLabel(CustomerLabelBo customerLabelBo) {
        int labelWidth = 480; // 图片宽度
        int labelHeight = 270; // 图片高度
        int wordsX = 30; // 文字 X 坐标
        int wordsY = 39; // 文字 Y 坐标
        int wordsInterval = 35; // 两行文字之间的间隔
        BufferedImage label = new BufferedImage(labelWidth, labelHeight, BufferedImage.TYPE_INT_RGB);
        Font font = new Font("黑体", Font.PLAIN, 20);
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

        // 料号
        graphics2D.drawString("料号：" + customerLabelBo.getProMaterialNumber(), wordsX, wordsY);
        wordsY += wordsInterval;

        // 型号
        graphics2D.drawString("型号：" + customerLabelBo.getProModel(), wordsX, wordsY);
        wordsY += wordsInterval;

        // 批号
        graphics2D.drawString("批号：" + customerLabelBo.getLotNumber(), wordsX, wordsY);
        wordsY += wordsInterval;

        // 数量
        graphics2D.drawString("数量：" + customerLabelBo.getLotNumber(), wordsX, wordsY);
        wordsY += wordsInterval;

        // 序列号
        graphics2D.drawString("序列号：" + customerLabelBo.getSerialNumber(), wordsX, wordsY);
        graphics2D.drawString("标签有效年份：", wordsX + 300, wordsY);
        wordsY += wordsInterval;

        // 公司名称
        LocalDateTime now = LocalDateTime.now();
        graphics2D.drawString("公司名称：" + customerLabelBo.getCompanyName(), wordsX, wordsY);
        graphics2D.drawString(now.getYear() + "", wordsX + 342, wordsY - 10);
        wordsY += wordsInterval;

        // 标签日期
        graphics2D.drawString("标签日期：" + now.getYear() + "-" + now.getMonthValue() + "-" + now.getDayOfMonth(), wordsX, wordsY);

        // 画入二维码
        QrCodeUtil qrCodeUtil = new QrCodeUtil();
        String qrCodeValue = customerLabelBo.getProMaterialNumber() + "-" +
                             customerLabelBo.getProModel() + "-" +
                             customerLabelBo.getLotNumber() + "-" +
                             customerLabelBo.getAmount();
        try {
            graphics2D.drawImage(qrCodeUtil.drawQrCode(qrCodeValue, 110, 110),341, 35, null);
            ImageIO.write(label, "png", new File("D://test.png"));
        } catch (WriterException | IOException e) {
            log.info("guiPoCustomerLabel 桂魄定制标签生成失败，出现异常： {}", e.getMessage());
            return ResponseData.createFailResponseData("guiPoCustomerLabelInfo", "桂魄定制标签任务提交失败，未知错误，请稍后重试", ResponseFailTypeConstant.UNKNOWN_ERROR.getFailType());
        }
        log.info("guiPoCustomerLabel 打印桂魄定制标签任务提交成功");
        return ResponseData.createSuccessResponseData("guiPoCustomerLabelInfo", "打印桂魄定制标签任务提交成功");
    }

    /**
     * 客户定制标签：小海豚
     * @param customerLabelBo 标签信息
     * @return 返回
     */
    private ResponseData dolphinCustomerLabel(CustomerLabelBo customerLabelBo) {
        int labelWidth = 480; // 图片宽度
        int labelHeight = 270; // 图片高度
        int wordsX = 30; // 文字 X 坐标
        int wordsY = 39; // 文字 Y 坐标
        int wordsInterval = 35; // 两行文字之间的间隔
        BufferedImage label = new BufferedImage(labelWidth, labelHeight, BufferedImage.TYPE_INT_RGB);
        Font font = new Font("黑体", Font.PLAIN, 20);
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

        // 料号
        graphics2D.drawString("料号：" + customerLabelBo.getProMaterialNumber(), wordsX, wordsY);
        wordsY += wordsInterval;

        // 型号
        graphics2D.drawString("型号：" + customerLabelBo.getProModel(), wordsX, wordsY);
        wordsY += wordsInterval;

        // 批号
        graphics2D.drawString("批号：" + customerLabelBo.getLotNumber(), wordsX, wordsY);
        wordsY += wordsInterval;

        // 数量
        graphics2D.drawString("数量：" + customerLabelBo.getLotNumber(), wordsX, wordsY);
        wordsY += wordsInterval;

        // 序列号
        graphics2D.drawString("序列号：" + customerLabelBo.getSerialNumber(), wordsX, wordsY);
        wordsY += wordsInterval;

        // 公司名称
        LocalDateTime now = LocalDateTime.now();
        graphics2D.drawString("公司名称：", wordsX, wordsY);
        graphics2D.drawString(now.getYear() + "", wordsX + 342, wordsY - 10);
        wordsY += wordsInterval;

        // 标签有效年份
        graphics2D.drawString("标签有效年份：" + now.getYear(), wordsX, wordsY);

        // 画入二维码
        QrCodeUtil qrCodeUtil = new QrCodeUtil();
        String qrCodeValue = customerLabelBo.getProMaterialNumber() + "-" +
                customerLabelBo.getProModel() + "-" +
                customerLabelBo.getLotNumber() + "-" +
                customerLabelBo.getAmount();
        try {
            graphics2D.drawImage(qrCodeUtil.drawQrCode(qrCodeValue, 110, 110),341, 35, null);
            ImageIO.write(label, "png", new File("D://test.png"));
        } catch (WriterException | IOException e) {
            log.info("dolphinCustomerLabel 小海豚定制标签生成失败，出现异常： {}", e.getMessage());
            return ResponseData.createFailResponseData("dolphinCustomerLabelInfo", "小海豚定制标签任务提交失败，未知错误，请稍后重试", ResponseFailTypeConstant.UNKNOWN_ERROR.getFailType());
        }
        log.info("dolphinCustomerLabel 打印小海豚定制标签任务提交成功");
        return ResponseData.createSuccessResponseData("dolphinCustomerLabelInfo", "打印小海豚定制标签任务提交成功");
    }

    /**
     * 客户定制标签：鲸鱼
     * @param customerLabelBo 标签信息
     * @return 返回
     */
    private ResponseData whaleCustomerLabel(CustomerLabelBo customerLabelBo) {
        int labelWidth = 480; // 图片宽度
        int labelHeight = 270; // 图片高度
        int wordsX = 30; // 文字 X 坐标
        int wordsY = 39; // 文字 Y 坐标
        int wordsInterval = 35; // 两行文字之间的间隔
        BufferedImage label = new BufferedImage(labelWidth, labelHeight, BufferedImage.TYPE_INT_RGB);
        Font font = new Font("黑体", Font.PLAIN, 20);
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

        // 料号
        graphics2D.drawString("料号：" + customerLabelBo.getProMaterialNumber(), wordsX, wordsY);
        wordsY += wordsInterval;

        // 型号
        graphics2D.drawString("型号：" + customerLabelBo.getProModel(), wordsX, wordsY);
        wordsY += wordsInterval;

        // 批号
        graphics2D.drawString("批号：" + customerLabelBo.getLotNumber(), wordsX, wordsY);
        wordsY += wordsInterval;

        // 数量
        graphics2D.drawString("数量：" + customerLabelBo.getAmount(), wordsX, wordsY);
        wordsY += wordsInterval;

        // 序列号
        graphics2D.drawString("序列号：" + customerLabelBo.getSerialNumber(), wordsX, wordsY);
        wordsY += wordsInterval;

        // 公司名称
        graphics2D.drawString("公司名称：" + customerLabelBo.getCompanyName(), wordsX, wordsY);
        wordsY += wordsInterval;

        // 标签日期
        LocalDateTime now = LocalDateTime.now();
        graphics2D.drawString("标签日期：" + now.getYear() + "-" + now.getMonthValue() + "-" + now.getDayOfMonth(), wordsX, wordsY);


        // 画入二维码
        QrCodeUtil qrCodeUtil = new QrCodeUtil();
        String qrCodeValue = customerLabelBo.getProMaterialNumber() + "-" +
                             customerLabelBo.getProModel() + "-" +
                             customerLabelBo.getLotNumber() + "-" +
                             customerLabelBo.getAmount();
        try {

            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("static/whale.png");
            if (inputStream == null) {
                log.info("whaleCustomerLabel 鲸鱼定制标签任务提交失败, 鲸鱼logo图片不存在");
                return ResponseData.createFailResponseData("whaleCustomerLabelInfo", "打印鲸鱼定制标签失败，鲸鱼logo图片不存在", ResponseFailTypeConstant.DATA_NOT_EXIST.getFailType());
            }
            // 画二维码
            graphics2D.drawImage(qrCodeUtil.drawQrCode(qrCodeValue, 110, 110),341, 35, null);
            // 画鲸鱼logo
            graphics2D.drawImage(ImageIO.read(inputStream), 360, 150, null);
            ImageIO.write(label, "png", new File("D://test2.png"));
        } catch (WriterException | IOException e) {
            log.info("whaleCustomerLabel 鲸鱼定制标签任务提交失败，出现异常: {}", e.getMessage());
            return ResponseData.createFailResponseData("whaleCustomerLabelInfo", "打印鲸鱼定制标签失败，未知错误，请稍后重试", ResponseFailTypeConstant.UNKNOWN_ERROR.getFailType());
        }
        log.info("whaleCustomerLabel 鲸鱼定制标签生成成功");
        return ResponseData.createSuccessResponseData("whaleCustomerLabelInfo", "打印鲸鱼定制标签任务提交成功");
    }

//    @PostConstruct
//    public void test() {
//        CustomerLabelBo customerLabelBo = new CustomerLabelBo();
//        customerLabelBo.setCustomerName("鲸鱼");
//        customerLabelBo.setCompanyName("鲸鱼科技有效公司");
//        customerLabelBo.setProMaterialNumber("ABC123");
//        customerLabelBo.setProModel("H717");
//        customerLabelBo.setLotNumber("HAB123");
//        customerLabelBo.setAmount("6000");
//        customerLabelBo.setSerialNumber("202111092227307");
//        System.out.println("开始执行");
//        ResponseData res = whaleCustomerLabel(customerLabelBo);
//        System.out.println("执行结束");
//        System.out.println(res);
//    }


}
