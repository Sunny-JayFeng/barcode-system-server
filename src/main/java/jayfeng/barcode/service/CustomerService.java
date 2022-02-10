package jayfeng.barcode.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jayfeng.barcode.bean.Customer;
import jayfeng.barcode.response.ResponseData;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 客户信息业务逻辑层
 * @author JayFeng
 * @date 2021/10/19
 */
@Service
public interface CustomerService {

    /**
     * 分页查询客户信息
     * @param requestParams 请求参数
     * @param page 分页参数
     * @return 返回
     */
    ResponseData findCustomerPages(Map<String, String> requestParams, Page<Customer> page);

    /**
     * 添加客户信息
     * @param customer 待添加的客户信息
     * @return 返回
     */
    ResponseData addCustomer(Customer customer);

    /**
     * 更新客户信息
     * @param customer 待更新的客户
     * @param password 当前登录的用户的密码
     * @return 返回
     */
    ResponseData updateCustomer(Customer customer, String password);

    /**
     * 删除客户信息
     * @param customer 待更新的客户
     * @param password 当前登录的用户的密码
     * @return 返回
     */
    ResponseData deleteCustomer(Customer customer, String password);

}
