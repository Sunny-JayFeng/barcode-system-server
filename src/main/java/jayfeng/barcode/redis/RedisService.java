package jayfeng.barcode.redis;

import jayfeng.barcode.bean.AccessKey;
import jayfeng.barcode.constant.RedisConstant;
import jayfeng.barcode.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * redis 操作
 * @author JayFeng
 * @date 2021/10/12
 */
@Service
@Slf4j
public class RedisService {

    @Autowired
    private RedisOperate redisOperate;
    @Autowired
    private RedissonUtil redissonUtil;

    /**
     * 获取短信验证码
     * @param phone key-手机号
     * @return 返回验证码的值
     */
    public String getIdentifyCode(String phone) {
        log.info("getIdentifyCode 从redis缓存中获取短信验证码, phone: {}", phone);
        return redisOperate.get(phone);
    }

    /**
     * 向 redis 缓存中添加一个验证码，有效时间为 10 分钟
     * @param phone key-手机号
     * @param identifyCode 验证码
     */
    public void addIdentifyCode(String phone, String identifyCode) {
        log.info("addIdentifyCode 向redis缓存中添加一个验证码, phone: {}, identifyCode: {}", phone, identifyCode);
        redisOperate.set(phone, identifyCode, RedisConstant.IDENTIFY_TIMEOUT.getTimeout(), TimeUnit.SECONDS);
    }

    /**
     * 从 redis 缓存中移除一个验证码
     * @param phone key-手机号
     */
    public Boolean removeIdentifyCode(String phone) {
        log.info("removeIdentifyCode, 从redis缓存中移除一个验证码, phone: {}", phone);
        return redisOperate.remove(phone);
    }

    /**
     * 将当前登录的用户信息存到 redis
     * @param redisKey map key
     * @param sessionId 用户 key
     * @param nowLoginUserJson 用户信息JSON
     */
    public void setUserJSON(String redisKey, String sessionId, String nowLoginUserJson) {
        redisOperate.setForHash(redisKey, sessionId, nowLoginUserJson);
        log.info("setUserJSON 用户信息存到 redis 成功， nowLoginUserJson: {}", nowLoginUserJson);
    }

    /**
     * 获取某个 map 中的某个键值对的值
     * @param redisKey map key
     * @param sessionId 键
     * @return 值
     */
    public Object getUserJSON(String redisKey, String sessionId) {
        Object userObj = redisOperate.getValueForHash(redisKey, sessionId);
        log.info("getUserJSON 从 redis 缓存中获取用户 userObj: {}", userObj);
        return userObj;
    }

    /**
     * 存入 uuid
     * @param redisKey 哪一个 map
     * @param sessionId 存入的 key
     */
    public void addUUID(String redisKey, String sessionId, String objectStr) {
        log.info("addUUID 向redis缓存中添加一个uuid, redisKey: {}, UUID: {}", redisKey, sessionId);
        redisOperate.setForHash(redisKey, sessionId, objectStr);
    }

    /**
     * 退出登录
     * 删除 uuid
     * @param redisKey 哪一个 map
     * @param sessionId 删除的 key
     */
    public void deleteUUID(String redisKey, String sessionId) {
        log.info("deleteUUID 从redis缓存中删除一个uuid, redisKey: {}, UUID: {}", redisKey, sessionId);
        redisOperate.removeForHash(redisKey, sessionId);
    }

    /**
     * 是否存在某个 key
     * @param key key
     * @return 返回是否存在
     */
    public boolean isExistKey(Object key) {
        return redisOperate.isExistsKey(key.toString());
    }

    /**
     * 拼盒拼箱过程中，存入库存 id
     * @param stockId 库存id
     */
    public void addStockIdKey(Integer stockId) {
        addStockIdKey(stockId, 6L);
    }

    /**
     * 支持传自定义过期时间
     * @param stockId 库存id
     * @param time 过期时间
     */
    public void addStockIdKey(Integer stockId, Long time) {
        redisOperate.set(stockId.toString(), stockId.toString(), time, TimeUnit.MINUTES);
    }

    /**
     * 拼盒拼箱过程中，获取库存 id 的过期时间
     * @param stockId 库存 id
     * @return 返回过期时间
     */
    public Long getStockIdKeyTimeout(Integer stockId) {
        return redisOperate.getTimeout(stockId.toString(), TimeUnit.MINUTES);
    }

    /**
     * 删除键
     * @param stockId 库存 id
     * @return 返回删除是否成功
     */
    public boolean removeStockIdKey(Integer stockId) {
        return redisOperate.remove(stockId.toString());
    }

    /**
     * 获取一个流水号
     * @return 返回流水号
     */
    public String getSerialNumber() {
        if (redissonUtil.tryLock("serialNumber")) {
            try {
                int value = Integer.parseInt(redisOperate.get("serialNumber"));
                redisOperate.set("serialNumber", value + 1 + "");
                return Integer.toString(value);
            } finally {
                redissonUtil.unLock("serialNumber");
            }
        } else {
            return null;
        }
    }


    public boolean commitPrintLabelTask() {
        return true;
    }

    public void getPrintLabelTask() {

    }

}
