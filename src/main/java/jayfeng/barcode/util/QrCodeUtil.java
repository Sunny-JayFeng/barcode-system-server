package jayfeng.barcode.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码工具类
 * @author JayFeng
 * @date 2021/10/21
 */
@Component
public class QrCodeUtil {

    private MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

    private BarcodeFormat barcodeFormat = BarcodeFormat.QR_CODE;

    private Map<EncodeHintType, Object> map = new HashMap<>();

    private static Integer DEFAULT_WIDTH = 128;

    private static Integer DEFAULT_HEIGHT = 128;

    private Integer black = Color.BLACK.getRGB();

    private Integer white = Color.WHITE.getRGB();

    /**
     * 初始化
     */
    @PostConstruct
    private void init() {
        map.put(EncodeHintType.CHARACTER_SET, "UTF-8"); // 设置中文字符集
        map.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // 纠错等级
        map.put(EncodeHintType.MARGIN, 2); // 二维码四周留白。像素
    }

    /**
     * 画一个二维码
     * @param content 二维码码值
     * @param width 宽度
     * @param height 高度
     * @return 返回 BufferedImage
     * @throws WriterException
     */
    public BufferedImage drawQrCode(String content, int width, int height) throws WriterException {
        // 生成二维码对象
        BitMatrix bitMatrix = multiFormatWriter.encode(content, barcodeFormat, width, height, map);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x ++) {
            for (int y = 0; y < height; y ++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? black : white);
            }
        }
        return image;
    }

    /**
     * 画一个二维码
     * @param content 二维码码值
     * @return 返回 BufferedImage
     * @throws WriterException
     */
    public BufferedImage drawQrCode(String content) throws WriterException {
        return drawQrCode(content, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 生成一个二维码图片
     * @param filePath 图片路径
     * @param content 二维码码值
     * @param width 宽度
     * @param height 高度
     * @return 返回生成是否成功
     * @throws WriterException
     * @throws IOException
     */
    public Boolean generateQrCodeFile(String filePath, String content, int width, int height) throws WriterException, IOException {
        BufferedImage image = drawQrCode(content, width, height);
        File file = new File(filePath);
        return ImageIO.write(image, "jpg", file);
    }

    /**
     * 生成一个二维码图片
     * @param filePath 图片路径
     * @param content 二维码码值
     * @return 返回生成是否成功
     * @throws WriterException
     * @throws IOException
     */
    public Boolean generateQrCodeFile(String filePath, String content) throws WriterException, IOException {
        return generateQrCodeFile(filePath, content, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public static void main(String[] args) throws IOException, WriterException {
        QrCodeUtil qrCodeUtil = new QrCodeUtil();
        qrCodeUtil.init();
        qrCodeUtil.generateQrCodeFile("E://test.jpg", "test", 100, 100);
    }

}
