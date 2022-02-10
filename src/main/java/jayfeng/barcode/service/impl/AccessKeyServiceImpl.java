package jayfeng.barcode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jayfeng.barcode.bean.AccessKey;
import jayfeng.barcode.bean.User;
import jayfeng.barcode.constant.ResponseFailMessageConstant;
import jayfeng.barcode.constant.ResponseFailTypeConstant;
import jayfeng.barcode.dao.AccessKeyDao;
import jayfeng.barcode.response.ResponseData;
import jayfeng.barcode.service.AccessKeyService;
import jayfeng.barcode.service.UserService;
import jayfeng.barcode.util.EncryptUtil;
import jayfeng.barcode.util.UserPermissionCheck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * 阿里短信服务密钥业务逻辑层
 * @author JayFeng
 * @date 2021/10/14
 */
@Slf4j
@Service
public class AccessKeyServiceImpl implements AccessKeyService {

    @Autowired
    private AccessKeyDao accessKeyDao;
    @Autowired
    private UserService userService;
    @Autowired
    private UserPermissionCheck userPermissionCheck;
    @Autowired
    private EncryptUtil encryptUtil;

    private static Random random = new Random();

    /**
     * 获取一个短信服务密钥
     * @return 返回
     */
    @Override
    public AccessKey getAccessKey() {
        List<AccessKey> accessKeyList = accessKeyDao.findAllAccessKey();
        if (accessKeyList.isEmpty()) return null;
        return accessKeyList.get(random.nextInt(accessKeyList.size()));
    }

    /**
     * 分页查询短信服务密钥信息
     * @param page 分页
     * @return 返回
     */
    @Override
    public ResponseData getAccessKeyPage(Page<AccessKey> page) {
        Page<AccessKey> dataPage = accessKeyDao.selectPage(page, new QueryWrapper<>());
        log.info("getAccessKeyPage 分页查询短信服务密钥信息结果 total: {}", dataPage.getTotal());
        return ResponseData.createSuccessResponseData("getAccessKeyPageInfo", dataPage);
    }

    /**
     * 添加一个短信服务密钥
     * @param accessKey 待添加密钥
     * @param password 登录密码
     * @return 返回
     */
    @Override
    public ResponseData addAccessKey(AccessKey accessKey, String password) {
        User nowLoginUser = userService.getNowLoginUser();
        if (userPermissionCheck.haveAddPermission(nowLoginUser)) { // 如果有添加密钥的权限
            // 密码验证
            if (encryptUtil.matches(password, nowLoginUser.getPassword())) { // 验证通过
                // 数据不为空校验
                if (ObjectUtils.isEmpty(accessKey.getRegionId()) ||
                        ObjectUtils.isEmpty(accessKey.getAccessKeyId()) ||
                        ObjectUtils.isEmpty(accessKey.getSecret())) {
                    log.info("addAccessKey 添加短信服务密钥失败, 数据错误 accessKey: {}", accessKey);
                    return ResponseData.createFailResponseData("addAccessKeyInfo",
                            ResponseFailMessageConstant.ADD_DATA_ERROR.getFailMessage(),
                            ResponseFailTypeConstant.DATA_ERROR.getFailType());
                }
                // 如果密钥已存在，返回已存在
                if (accessKeyDao.findIdByAccessKeyId(accessKey.getAccessKeyId()) != null) {
                    log.info("addAccessKey 添加短信服务密钥失败, 该密钥已存在");
                    return ResponseData.createFailResponseData("addAccessKeyInfo",
                            "密钥已存在",
                            ResponseFailTypeConstant.DATA_ALREADY_EXIST.getFailType());
                }
                // 添加密钥
                accessKey.setCreateTime(LocalDateTime.now());
                accessKey.setUpdateTime(accessKey.getCreateTime());
                accessKeyDao.insert(accessKey);
                log.info("addAccessKey 添加短信服务密钥成功 operator: {}, accessKey: {}", nowLoginUser.getUserName(), accessKey);
                return ResponseData.createSuccessResponseData("addAccessKeyInfo", "短信服务密钥添加成功");
            }
            // 验证通过
            log.info("addAccessKey 添加失败，密码校验不通过");
            return ResponseData.createFailResponseData("addAccessKeyInfo",
                    ResponseFailMessageConstant.ADD_PASSWORD_ERROR.getFailMessage(),
                    ResponseFailTypeConstant.PASSWORD_ERROR.getFailType());
        } else { // 如果没有权限，返回失败
            log.info("addAccessKey 没有添加密钥的权限 nowLoginUser: {}", nowLoginUser);
            return ResponseData.createFailResponseData("addAccessKeyInfo",
                    ResponseFailMessageConstant.NO_ADD_PERMISSION.getFailMessage(),
                    ResponseFailTypeConstant.NO_OPERATION_PERMISSION.getFailType());
        }
    }

    /**
     * 删除一个短信服务密钥
     * @param accessKey 待添加密钥
     * @param password 登录密码
     * @return 返回
     */
    @Override
    @Transactional
    public ResponseData deleteAccessKey(AccessKey accessKey, String password) {
        User nowLoginUser = userService.getNowLoginUser();
        if (userPermissionCheck.haveDeletePermission(nowLoginUser)) { // 如果有删除权限
            // 密码校验
            if (encryptUtil.matches(password, nowLoginUser.getPassword())) { // 密码校验通过
                accessKeyDao.deleteById(accessKey.getId());
                log.info("deleteAccessKey 删除短信服务密钥成功 operator: {}, accessKey: {}", nowLoginUser.getUserName(), accessKey);
                return ResponseData.createSuccessResponseData("deleteAccessKeyInfo", "短信服务密钥删除成功");
            }
            // 密码校验不通过
            log.info("deleteAccessKey 删除短信服务密钥失败，密码错误");
            return ResponseData.createFailResponseData("deleteAccessKeyInfo",
                    ResponseFailMessageConstant.DELETE_PASSWORD_ERROR.getFailMessage(),
                    ResponseFailTypeConstant.PASSWORD_ERROR.getFailType());
        } else { // 如果没有删除权限，返回失败
            log.info("deleteAccessKey 删除短信服务密钥失败，没有操作权限 nowLoginUser: {}", nowLoginUser);
            return ResponseData.createFailResponseData("deleteAccessKeyInfo",
                    ResponseFailMessageConstant.NO_DELETE_PERMISSION.getFailMessage(),
                    ResponseFailTypeConstant.NO_OPERATION_PERMISSION.getFailType());
        }
    }

    /**
     * 修改一个短信服务密钥
     * @param accessKey 待添加密钥
     * @param password 登录密码
     * @return 返回
     */
    @Override
    @Transactional
    public ResponseData updateAccessKey(AccessKey accessKey, String password) {
        User nowLoginUser = userService.getNowLoginUser();
        // 权限校验
        if (userPermissionCheck.haveUpdatePermission(nowLoginUser)) { // 如果有更新的权限
            if (encryptUtil.matches(password, nowLoginUser.getPassword())) { // 密码校验通过
                AccessKey oldAccessKey = accessKeyDao.selectById(accessKey.getId());
                accessKey.setUpdateTime(LocalDateTime.now());
                accessKeyDao.updateById(accessKey);
                log.info("updateAccessKey 短信服务密钥信息更新成功 operator: {}, oldAccessKey: {}, newAccessKey: {}", nowLoginUser.getUserName(), oldAccessKey, accessKey);
                return ResponseData.createSuccessResponseData("updateAccessKeyInfo", "短信服务密钥信息更新成功");
            }
            // 密码校验不通过
            log.info("updateAccessKey 短信服务密钥信息更新失败, 密码错误");
            return ResponseData.createFailResponseData("updateAccessKeyInfo",
                    ResponseFailMessageConstant.UPDATE_PASSWORD_ERROR.getFailMessage(),
                    ResponseFailTypeConstant.PASSWORD_ERROR.getFailType());
        } else { // 如果没有更新的权限，返回失败
            log.info("updateAccessKey 短信服务密钥信息更新失败，没有权限");
            return ResponseData.createFailResponseData("updateAccessKeyInfo",
                    ResponseFailMessageConstant.NO_UPDATE_PERMISSION.getFailMessage(),
                    ResponseFailTypeConstant.NO_OPERATION_PERMISSION.getFailType());
        }
    }
}
