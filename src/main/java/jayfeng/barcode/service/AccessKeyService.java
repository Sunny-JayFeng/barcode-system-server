package jayfeng.barcode.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jayfeng.barcode.bean.AccessKey;
import jayfeng.barcode.response.ResponseData;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * 阿里短信服务密钥业务逻辑层
 * @author JayFeng
 * @date 2021/10/14
 */
@Service
public interface AccessKeyService {

    /**
     * 获取一个短信服务密钥
     * @return 返回
     */
    AccessKey getAccessKey();

    /**
     * 分页查询短信服务密钥信息
     * @param page 分页
     * @return 返回
     */
    ResponseData getAccessKeyPage(Page<AccessKey> page);

    /**
     * 添加一个短信服务密钥
     * @param accessKey 待添加密钥
     * @param password 登录密码
     * @return 返回
     */
    ResponseData addAccessKey(AccessKey accessKey, String password);

    /**
     * 删除一个短信服务密钥
     * @param accessKey 待添加密钥
     * @param password 登录密码
     * @return 返回
     */
    ResponseData deleteAccessKey(AccessKey accessKey, String password);

    /**
     * 修改一个短信服务密钥
     * @param accessKey 待添加密钥
     * @param password 登录密码
     * @return 返回
     */
    ResponseData updateAccessKey(AccessKey accessKey, String password);

}
