package jayfeng.barcode.service;

import jayfeng.barcode.bean.bo.CustomerLabelBo;
import jayfeng.barcode.response.ResponseData;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 客户定制标签业务逻辑层
 * @author JayFeng
 * @date 2021/11/8
 */
@Service
public interface CustomerLabelService {

    /**
     * 打印客户定制标签
     * @param customerLabelBo 标签信息
     * @return 返回
     */
    ResponseData printCustomerLabel(CustomerLabelBo customerLabelBo);

}
