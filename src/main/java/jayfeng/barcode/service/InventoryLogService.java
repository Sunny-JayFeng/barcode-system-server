package jayfeng.barcode.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jayfeng.barcode.bean.InventoryLog;
import jayfeng.barcode.response.ResponseData;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 盘点日志数据业务逻辑层
 * @author JayFeng
 * @date 2021/11/2
 */
@Service
public interface InventoryLogService {

    Byte NO_DIFFERENCE = 0;
    Byte DIFFERENCE = 1;

    /**
     * 分页查询盘点日志数据
     * @param requestParams 请求参数
     * @param page 页参数
     * @return 返回
     */
    ResponseData findInventoryLogPage(Map<String, String> requestParams, Page<InventoryLog> page);

    /**
     * 扫码盘点
     * @param qrCodeValue 二维码信息
     * @return 返回
     */
    ResponseData scanQrCodeInventory(String qrCodeValue);

}
