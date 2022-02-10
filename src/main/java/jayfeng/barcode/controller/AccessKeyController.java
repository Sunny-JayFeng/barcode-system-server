package jayfeng.barcode.controller;

import jayfeng.barcode.bean.AccessKey;
import jayfeng.barcode.response.ResponseMessage;
import jayfeng.barcode.service.AccessKeyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 阿里短信服务密钥
 * @author JayFeng
 * @date 2021/10/14
 */
@Slf4j
@RestController
@RequestMapping("/barcode/accessKey")
public class AccessKeyController extends BaseController {

    @Autowired
    private AccessKeyService accessKeyService;

    /**
     * 分页查询短信服务密钥信息
     * @param requestParams 分页参数
     * @return 返回
     */
    @GetMapping("/getAccessKeyPage")
    public ResponseMessage getAccessKeyPage(@RequestParam Map<String, String> requestParams) {
        log.info("getAccessKeyPage 分页查询短信服务密钥信息: requestParams: {}", requestParams);
        return requestSuccess(accessKeyService.getAccessKeyPage(getPage(requestParams)));
    }

    /**
     * 添加一个短信服务密钥
     * @param accessKey 待添加密钥
     * @param password 登录密码
     * @return 返回
     */
    @PostMapping("/addAccessKey/{password}")
    public ResponseMessage addAccessKey(@RequestBody AccessKey accessKey,
                                        @PathVariable("password") String password) {
        log.info("addAccessKey 添加一个短信服务密钥 accessKey: {}", accessKey);
        return requestSuccess(accessKeyService.addAccessKey(accessKey, password));
    }

    /**
     * 删除一个短信服务密钥
     * @param accessKey 待添加密钥
     * @param password 登录密码
     * @return 返回
     */
    @DeleteMapping("/deleteAccessKey/{password}")
    public ResponseMessage deleteAccessKey(@RequestBody AccessKey accessKey,
                                           @PathVariable("password") String password) {
        log.info("deleteAccessKey 删除一个短信服务密钥 accessKey: {}", accessKey);
        return requestSuccess(accessKeyService.deleteAccessKey(accessKey, password));
    }

    /**
     * 修改一个短信服务密钥
     * @param accessKey 待添加密钥
     * @param password 登录密码
     * @return 返回
     */
    @PutMapping("/updateAccessKey/{password}")
    public ResponseMessage updateAccessKey(@RequestBody AccessKey accessKey,
                                           @PathVariable("password") String password) {
        log.info("updateAccessKey 修改一个短信服务密钥accessKey: {}", accessKey);
        return requestSuccess(accessKeyService.updateAccessKey(accessKey, password));
    }

}