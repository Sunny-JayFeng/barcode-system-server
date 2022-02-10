package jayfeng.barcode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jayfeng.barcode.bean.Product;
import jayfeng.barcode.bean.User;
import jayfeng.barcode.constant.ResponseFailMessageConstant;
import jayfeng.barcode.constant.ResponseFailTypeConstant;
import jayfeng.barcode.dao.ProductDao;
import jayfeng.barcode.response.ResponseData;
import jayfeng.barcode.service.ProductService;
import jayfeng.barcode.service.UserService;
import jayfeng.barcode.util.EncryptUtil;
import jayfeng.barcode.handler.QueryConditionHandler;
import jayfeng.barcode.util.UserPermissionCheck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 产品信息业务逻辑层
 * @author JayFeng
 * @date 2021/10/13
 */
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductDao productDao;
    @Autowired
    private UserService userService;
    @Autowired
    private UserPermissionCheck userPermissionCheck;
    @Autowired
    private QueryConditionHandler queryConditionHandler;
    @Autowired
    private EncryptUtil encryptUtil;

    /**
     * 分页查询产品信息
     * @param requestParams 请求参数
     * @return 返回数据体
     */
    @Override
    public ResponseData findProductsPage(Map<String, String> requestParams, Page<Product> page) {
        log.info("findProductsPage 分页查询产品信息: requestParams: {}, page: {}", requestParams, page);
        Map<String, String> queryTypeMap = new HashMap<>(8); // 查询条件查询类型
        queryTypeMap.put("proMaterialNumber", QueryConditionHandler.LIKE); // 模糊查询
        queryTypeMap.put("proModel", QueryConditionHandler.EQUAL); // 精准查询
        queryTypeMap.put("lotNumber", QueryConditionHandler.EQUAL); // 精准查询
        queryTypeMap.put("serialNumber", QueryConditionHandler.EQUAL); // 精准查询
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryConditionHandler.handleQueryCondition(queryWrapper, queryTypeMap, requestParams); // 处理查询条件
        try {
            queryConditionHandler.handleStandardQuantityQueryCondition(queryWrapper, requestParams); // 处理数量查询条件
        } catch (NumberFormatException e) {
            log.info("findProductsPage 分页查询产品信息失败，数量参数存在错误");
            ResponseData.createFailResponseData("findProductsPageInfo",
                    "数量参数错误",
                    ResponseFailTypeConstant.QUANTITY_PARAMS_ERROR.getFailType());
        }
        Page<Product> dataPage = productDao.selectPage(page, queryWrapper);
        log.info("findProductsPage 分页查询产品信息结果 total: {}", dataPage.getTotal());
        return ResponseData.createSuccessResponseData("findProductsPageInfo", dataPage);
    }

    /**
     * 更新产品信息
     * 根据 id 进行修改，只能修改每盒标准数量、每箱标准数量
     * @param requestParams 请求参数
     * @return 返回数据体
     */
    @Override
    @Transactional
    public ResponseData updateProduct(Map<String, String> requestParams) {
        log.info("updateProduct 更新产品信息 requestParams: {}", requestParams);
        try { // 理论上这里是不会出异常的，除非通过postMan恶意请求
            Integer productId = Integer.parseInt(requestParams.get("productId"));
            Product product = productDao.selectById(productId);
            if (product == null) {
                log.info("updateProduct 更新产品信息失败，查无此产品 productId: {}", productId);
                return ResponseData.createFailResponseData("updateProductInfo",
                        "查无此产品",
                        ResponseFailTypeConstant.DATA_NOT_EXIST.getFailType());
            } else {
                Integer boxStandardQuantity = Integer.parseInt(requestParams.get("boxStandardQuantity"));
                Integer caseStandardQuantity = Integer.parseInt(requestParams.get("caseStandardQuantity"));
                log.info("updateProduct 更新产品信息, 旧产品信息 product: {}", product);
                product.setBoxStandardQuantity(boxStandardQuantity);
                product.setCaseStandardQuantity(caseStandardQuantity);
                product.setUpdateTime(LocalDateTime.now());
                productDao.updateById(product);
                log.info("updateProduct 产品标准数量更新成功 product: {}", product);
                return ResponseData.createSuccessResponseData("updateProduct", "产品标准数量更新成功");
            }

        } catch (NumberFormatException e) {
            log.info("updateProduct 更新产品信息失败, 参数存在错误");
            return ResponseData.createFailResponseData("updateProductInfo",
                    ResponseFailMessageConstant.UPDATE_DATA_ERROR.getFailMessage(),
                    ResponseFailTypeConstant.QUANTITY_PARAMS_ERROR.getFailType());
        }
    }

    /**
     * 新增产品信息
     * @param product 产品信息
     * @return 返回数据体
     */
    @Override
    @Transactional
    public ResponseData addProduct(Product product) {
        log.info("addProduct 添加产品信息 product: {}", product);
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(product.getCreateTime());
        productDao.insert(product);
        return ResponseData.createSuccessResponseData("addProductInfo", null);
    }

    /**
     * 删除产品信息
     * @param product 产品信息
     * @param password 密码
     * @return 返回数据体
     */
    @Override
    @Transactional
    public ResponseData deleteProduct(Product product, String password) {
        User nowLoginUser = userService.getNowLoginUser();
        if (userPermissionCheck.haveDeletePermission(nowLoginUser)) { // 有删除的权限
            if (encryptUtil.matches(password, nowLoginUser.getPassword())) { // 校验密码是否匹配
                productDao.deleteById(product.getId());
                log.info("deleteProduct 产品信息删除成功 operator: {}, product: {}", nowLoginUser.getUserName(), product);
                return ResponseData.createSuccessResponseData("deleteProductInfo", "删除产品信息成功");
            } else { // 不匹配
                log.info("deleteProduct 产品信息删除失败, 密码不匹配");
                return ResponseData.createFailResponseData("deleteProductInfo",
                        ResponseFailMessageConstant.DELETE_PASSWORD_ERROR.getFailMessage(),
                        ResponseFailTypeConstant.PASSWORD_ERROR.getFailType());
            }
        } else { // 没有删除权限
            log.info("deleteProduct 没有删除产品信息的权限 nowLoginUser: {}", nowLoginUser);
            return ResponseData.createFailResponseData("deleteProductInfo",
                    ResponseFailMessageConstant.NO_DELETE_PERMISSION.getFailMessage(),
                    ResponseFailTypeConstant.NO_OPERATION_PERMISSION.getFailType());
        }

    }

}
