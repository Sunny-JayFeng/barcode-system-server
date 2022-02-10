package jayfeng.barcode.service.impl;

import com.google.zxing.WriterException;
import io.netty.util.internal.ResourcesUtil;
import jayfeng.barcode.bean.Test;
import jayfeng.barcode.dao.TestDao;
import jayfeng.barcode.redis.RedisService;
import jayfeng.barcode.util.QrCodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author JayFeng
 * @date 2021/10/27
 */
@Service
public class TestService {

    @Autowired
    private TestDao testDao;
    @Autowired
    private RedisService redisService;

    public void test() throws IOException {
        System.out.println("开始执行");

        System.out.println("执行完毕");
    }

    public static void main(String[] args) throws IOException, WriterException {
//        int labelWidth = 480; // 图片宽度
//        int labelHeight = 270; // 图片高度
//        int wordsX = 30; // 文字 X 坐标
//        int wordsY = 39; // 文字 Y 坐标
//        int wordsInterval = 35; // 两行文字之间的间隔
//        BufferedImage label = new BufferedImage(labelWidth, labelHeight, BufferedImage.TYPE_INT_RGB);
//        Font font = new Font("黑体", Font.PLAIN, 20);
//        Graphics2D graphics2D = label.createGraphics();
//        graphics2D.fillRect(0, 0, labelWidth, labelHeight);
//        graphics2D.setColor(Color.BLACK); // 画笔颜色
//        graphics2D.setBackground(Color.WHITE); // 背景颜色
//        graphics2D.setFont(font); // 字体
//        //消除文字锯齿
//        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//        //消除画图锯齿
//        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        // 标签名称
//
//        // 料号
//        graphics2D.drawString("料号：ABC123", wordsX, wordsY);
//        wordsY += wordsInterval;
//
//        // 型号
//        graphics2D.drawString("型号：H717", wordsX, wordsY);
//        wordsY += wordsInterval;
//
//        // 批号
//        graphics2D.drawString("批号：HB1234", wordsX, wordsY);
//        wordsY += wordsInterval;
//
//        // 数量
//        graphics2D.drawString("数量：6000", wordsX, wordsY);
//        wordsY += wordsInterval;
//
//        // 序列号
//        graphics2D.drawString("序列号：202111092227307", wordsX, wordsY);
//        wordsY += wordsInterval;
//
//        // 公司名称
//        graphics2D.drawString("公司名称：冰轮科技有限公司", wordsX, wordsY);
//        wordsY += wordsInterval;
//
//        // 标签日期
//        graphics2D.drawString("标签日期：2021-11-09", wordsX, wordsY);
//
//        // 画入二维码
//        QrCodeUtil qrCodeUtil = new QrCodeUtil();
//        String qrCodeValue = "ABC123-" + "H717-" + "HB1234-" + "6000-" + "202111092227307";
//        graphics2D.drawImage(qrCodeUtil.drawQrCode(qrCodeValue, 110, 110),341, 35, null);
//        graphics2D.drawImage(ImageIO.read(new File(this.getClass().getClassLoader().getResource("static/whale.png").getPath())), 360, 150, null);
//        ImageIO.write(label, "png", new File("D://test.png"));
    }

}
