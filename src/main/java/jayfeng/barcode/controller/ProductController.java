package jayfeng.barcode.controller;

import jayfeng.barcode.bean.Product;
import jayfeng.barcode.response.ResponseMessage;
import jayfeng.barcode.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 产品信息控制层
 * @author JayFeng
 * @date 2021/10/13
 */
@Slf4j
@RestController
@RequestMapping("/barcode/product")
public class ProductController extends BaseController{

    @Autowired
    private ProductService productService;

    /**
     * 分页查询产品信息
     * @param requestParams 请求参数
     * @return 返回数据体
     */
    @GetMapping("/findProductsPage")
    public ResponseMessage findProductsPage(@RequestParam Map<String, String> requestParams) {
        log.info("findProductsPage 分页查询产品信息 requestParams: {}", requestParams);
        return requestSuccess(productService.findProductsPage(requestParams, getPage(requestParams)));
    }

    /**
     * 更新产品信息
     * 根据 id 进行修改，只能修改每盒标准数量、每箱标准数量
     * @param requestParams 请求参数
     * @return 返回数据体
     */
    @PutMapping("/updateProduct")
    public ResponseMessage updateProduct(@RequestParam Map<String, String> requestParams) {
        log.info("updateProduct 更新产品信息 requestParams: {}", requestParams);
        return requestSuccess(productService.updateProduct(requestParams));
    }

    /**
     * 新增产品信息
     * @param product 产品信息
     * @return 返回数据体
     */
    @PostMapping("/addProduct")
    public ResponseMessage addProduct(@RequestBody Product product) {
        log.info("addProduct 新增产品信息 product: {}", product);
        return requestSuccess(productService.addProduct(product));
    }

    /**
     * 删除产品信息
     * @param product 产品信息
     * @param password 密码
     * @return 返回数据体
     */
    @DeleteMapping("/deleteProduct/{password}")
    public ResponseMessage deleteProduct(@RequestBody Product product,
                                         @PathVariable("password") String password) {
        log.info("deleteProduct 删除产品信息 product: {}", product);
        return requestSuccess(productService.deleteProduct(product, password));
    }

}
