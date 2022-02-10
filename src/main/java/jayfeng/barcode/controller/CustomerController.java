package jayfeng.barcode.controller;

import jayfeng.barcode.bean.Customer;
import jayfeng.barcode.response.ResponseMessage;
import jayfeng.barcode.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 客户信息控制层
 * @author JayFeng
 * @date 2021/10/19
 */
@Slf4j
@RestController
@RequestMapping("/barcode/customer")
public class CustomerController extends BaseController {

    @Autowired
    private CustomerService customerService;

    /**
     * 分页查询客户信息
     * @param requestParams 请求参数
     * @return 返回
     */
    @GetMapping("/findCustomerPage")
    public ResponseMessage findCustomerPage(@RequestParam Map<String, String> requestParams) {
        log.info("findCustomerPage 分页查询客户信息 requestParams: {}", requestParams);
        return requestSuccess(customerService.findCustomerPages(requestParams, getPage(requestParams)));
    }

    /**
     * 添加客户信息
     * @param customer 待添加的客户信息
     * @return 返回
     */
    @PostMapping("/addCustomer")
    public ResponseMessage addCustomer(@RequestBody Customer customer) {
        log.info("addCustomer 添加客户信息 customer: {}", customer);
        return requestSuccess(customerService.addCustomer(customer));
    }

    /**
     * 更新客户信息
     * @param customer 待更新的客户
     * @param password 当前登录的用户的密码
     * @return 返回
     */
    @PutMapping("/updateCustomer/{password}")
    public ResponseMessage updateCustomer(@RequestBody Customer customer,
                                          @PathVariable("password") String password) {
        log.info("updateCustomer 更新客户信息 customer: {}", customer);
        return requestSuccess(customerService.updateCustomer(customer, password));
    }

    /**
     * 删除客户信息
     * @param customer 待更新的客户
     * @param password 当前登录的用户的密码
     * @return 返回
     */
    @DeleteMapping("/deleteCustomer/{password}")
    public ResponseMessage deleteCustomer(@RequestBody Customer customer,
                                          @PathVariable("password") String password) {
        log.info("deleteCustomer 删除客户信息 customer: {}, 登录密码：password: {}", customer, password);
        return requestSuccess(customerService.deleteCustomer(customer, password));
    }
}
