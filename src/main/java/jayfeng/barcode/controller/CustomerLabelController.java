package jayfeng.barcode.controller;

import jayfeng.barcode.bean.bo.CustomerLabelBo;
import jayfeng.barcode.response.ResponseMessage;
import jayfeng.barcode.service.CustomerLabelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 客户定制标签控制层
 * @author JayFeng
 * @date 2021/11/8
 */
@Slf4j
@RestController
@RequestMapping("/barcode/customerLabel")
public class CustomerLabelController extends BaseController {

    @Autowired
    private CustomerLabelService customerLabelService;

    /**
     * 打印客户定制标签
     * @param customerLabelBo 标签信息
     * @return 返回
     */
    @PostMapping("/printCustomerLabel")
    public ResponseMessage printCustomerLabel(@RequestBody CustomerLabelBo customerLabelBo) {
        log.info("printCustomerLabel 打印客户定制标签 customerLabelBo: {}", customerLabelBo);
        return requestSuccess(customerLabelService.printCustomerLabel(customerLabelBo));
    }

}
