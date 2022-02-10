package jayfeng.barcode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jayfeng.barcode.bean.Customer;
import jayfeng.barcode.bean.User;
import jayfeng.barcode.constant.ResponseFailMessageConstant;
import jayfeng.barcode.constant.ResponseFailTypeConstant;
import jayfeng.barcode.dao.CustomerDao;
import jayfeng.barcode.handler.QueryConditionHandler;
import jayfeng.barcode.response.ResponseData;
import jayfeng.barcode.service.CustomerService;
import jayfeng.barcode.service.UserService;
import jayfeng.barcode.util.EncryptUtil;
import jayfeng.barcode.util.UserPermissionCheck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 客户信息业务逻辑层
 * @author JayFeng
 * @date 2021/10/19
 */
@Slf4j
@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerDao customerDao;
    @Autowired
    private QueryConditionHandler queryConditionHandler;
    @Autowired
    private UserService userService;
    @Autowired
    private UserPermissionCheck userPermissionCheck;
    @Autowired
    private EncryptUtil encryptUtil;

    /**
     * 分页查询客户信息
     * @param requestParams 请求参数
     * @param page 分页参数
     * @return 返回
     */
    @Override
    public ResponseData findCustomerPages(Map<String, String> requestParams, Page<Customer> page) {
        log.info("findCustomerPages 分页查询客户信息 requestParams: {}, page: {}", requestParams, page);
        Map<String, String> queryTypeMap = new HashMap<>(8);
        queryTypeMap.put("customerName", QueryConditionHandler.EQUAL);
        queryTypeMap.put("address", QueryConditionHandler.LIKE);
        queryTypeMap.put("phone", QueryConditionHandler.EQUAL);
        QueryWrapper<Customer> queryWrapper = new QueryWrapper<>();
        queryConditionHandler.handleQueryCondition(queryWrapper, queryTypeMap, requestParams);
        Page<Customer> dataPage = customerDao.selectPage(page, queryWrapper);
        log.info("findCustomerPages 分页查询客户信息结果 total: {}", dataPage.getTotal());
        return ResponseData.createSuccessResponseData("findCustomerPagesInfo", dataPage);
    }

    /**
     * 添加客户信息
     * @param customer 待添加的客户信息
     * @return 返回
     */
    @Override
    public ResponseData addCustomer(Customer customer) {
        customer.setCreateTime(LocalDateTime.now());
        customer.setUpdateTime(customer.getCreateTime());
        customerDao.insert(customer);
        log.info("addCustomer 添加客户信息成功 customer: {}", customer);
        return ResponseData.createSuccessResponseData("addCustomerInfo", "客户信息添加成功");
    }

    /**
     * 更新客户信息
     * @param customer 待更新的客户
     * @param password 当前登录的用户的密码
     * @return 返回
     */
    @Override
    public ResponseData updateCustomer(Customer customer, String password) {
        User nowLoginUser = userService.getNowLoginUser();
        if (userPermissionCheck.haveUpdatePermission(nowLoginUser)) {
            if (encryptUtil.matches(password, nowLoginUser.getPassword())) {
                Customer oldCustomer = customerDao.selectById(customer.getId());
                customer.setUpdateTime(LocalDateTime.now());
                customerDao.updateById(customer);
                log.info("updateCustomer 更新客户信息成功 operator: {}, oldCustomer:{}, newCustomer: {}", nowLoginUser.getUserName(), oldCustomer, customer);
                return ResponseData.createSuccessResponseData("updateCustomerInfo", "客户信息更新成功");
            } else {
                log.info("updateCustomer 更新客户信息失败，密码错误");
                return ResponseData.createFailResponseData("updateCustomerInfo",
                        ResponseFailMessageConstant.UPDATE_PASSWORD_ERROR.getFailMessage(),
                        ResponseFailTypeConstant.PASSWORD_ERROR.getFailType());
            }
        } else {
            log.info("updateCustomer 更新客户信息失败，没有权限");
            return ResponseData.createFailResponseData("updateCustomerInfo",
                    ResponseFailMessageConstant.NO_UPDATE_PERMISSION.getFailMessage(),
                    ResponseFailTypeConstant.NO_OPERATION_PERMISSION.getFailType());
        }
    }

    /**
     * 删除客户信息
     * @param customer 待更新的客户
     * @param password 当前登录的用户的密码
     * @return 返回
     */
    @Override
    public ResponseData deleteCustomer(Customer customer, String password) {
        User nowLoginUser = userService.getNowLoginUser();
        if (userPermissionCheck.haveDeletePermission(nowLoginUser)) {
            if (encryptUtil.matches(password, nowLoginUser.getPassword())) {
                customerDao.deleteById(customer.getId());
                log.info("deleteCustomer 删除客户信息成功 operator: {}, customer: {}", nowLoginUser.getUserName(), customer);
                return ResponseData.createSuccessResponseData("deleteCustomerInfo", "客户信息删除成功");
            } else {
                log.info("deleteCustomer 删除客户信息失败, 密码错误");
                return ResponseData.createFailResponseData("deleteCustomerInfo",
                        ResponseFailMessageConstant.DELETE_PASSWORD_ERROR.getFailMessage(),
                        ResponseFailTypeConstant.PASSWORD_ERROR.getFailType());
            }
        } else {
            log.info("deleteCustomer 删除客户信息失败，没有权限");
            return ResponseData.createFailResponseData("deleteCustomerInfo",
                    ResponseFailMessageConstant.NO_DELETE_PERMISSION.getFailMessage(),
                    ResponseFailTypeConstant.NO_OPERATION_PERMISSION.getFailType());
        }
    }

}
