package jayfeng.barcode.service;

import jayfeng.barcode.bean.Stock;
import jayfeng.barcode.bean.bo.StockLabelBo;
import jayfeng.barcode.response.ResponseData;
import org.springframework.stereotype.Service;

/**
 * 库存标签业务逻辑层
 * @author JayFeng
 * @date 2021/11/8
 */
@Service
public interface StockLabelService {

    /**
     * 打印库存标签：袋、盒、箱
     * @param stockLabelBo 库存标签信息
     * @return 返回
     */
    ResponseData printStockLabel(StockLabelBo stockLabelBo);

}
