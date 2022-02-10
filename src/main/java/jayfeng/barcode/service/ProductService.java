package jayfeng.barcode.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jayfeng.barcode.bean.Product;
import jayfeng.barcode.response.ResponseData;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 产品信息业务逻辑层
 * @author JayFeng
 * @date 2021/10/13
 */
@Service
public interface ProductService {

    /**
     * 分页查询产品信息
     * @param requestParams 请求参数
     * @return 返回数据体
     */
    ResponseData findProductsPage(Map<String, String> requestParams, Page<Product> page);

    /**
     * 更新产品信息
     * 根据 id 进行修改，只能修改每盒标准数量、每箱标准数量
     * @param requestParams 请求参数
     * @return 返回数据体
     */
    ResponseData updateProduct(Map<String, String> requestParams);

    /**
     * 新增产品信息
     * @param product 产品信息
     * @return 返回数据体
     */
    ResponseData addProduct(Product product);

    /**
     * 删除产品信息
     * @param product 产品信息
     * @param password 密码
     * @return 返回数据体
     */
    ResponseData deleteProduct(Product product, String password);

}
