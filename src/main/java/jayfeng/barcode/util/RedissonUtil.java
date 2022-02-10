package jayfeng.barcode.util;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * redisson 工具类：分布式锁的加锁和释放锁
 * @author JayFeng
 * @date 2021/10/24
 */
@Slf4j
@Component
public class RedissonUtil {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 尝试加锁
     * @param lockName 锁名字
     * @param waitTime 尝试加锁等待时间
     * @param leaseTime 锁过期时间
     * @return 返回加锁是否成功
     */
    public boolean tryLock(String lockName, Long waitTime, Long leaseTime) {
        if (ObjectUtils.isEmpty(lockName)) return false;
        RLock lock = redissonClient.getLock(lockName);
        try {
            boolean result = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            log.info("tryLock 加锁结果 result: {}", result);
            return result;
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.info("tryLock 加锁失败，出现异常 message: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 尝试加锁
     * @param lockName 锁名字
     * @param leaseTime 锁过期时间
     * @return 返回加锁是否成功
     */
    public boolean tryLock(String lockName, Long leaseTime) {
        // 默认尝试加锁等待时间 3s
        Long DEFAULT_WAIT_TIME = 3L;
        return tryLock(lockName, DEFAULT_WAIT_TIME, leaseTime);
    }

    /**
     * 尝试加锁
     * @param lockName 锁名字
     * @return 返回加锁是否成功
     */
    public boolean tryLock(String lockName) {
        // 默认锁过期时间 15s
        Long DEFAULT_LEASE_TIME = 15L;
        return tryLock(lockName, DEFAULT_LEASE_TIME);
    }

    /**
     * 尝试加锁
     * @param lockName 锁名字
     * @return 返回加锁是否成功
     */
    public boolean tryLock(Object lockName) {
        return tryLock(lockName.toString());
    }

    /**
     * 不设置过期时间加锁
     * @param lockName 锁名称
     * @return 返回
     */
    public boolean tryLockWithoutLeaseTime(Object lockName) {
        return tryLockWithoutLeaseTime(lockName.toString());
    }

    /**
     * 不设置过期时间加锁
     * @param lockName 锁名称
     * @return 返回
     */
    public boolean tryLockWithoutLeaseTime(String lockName) {
        if (ObjectUtils.isEmpty(lockName)) return false;
        RLock lock = redissonClient.getLock(lockName);
        boolean result = lock.tryLockAsync(3L, TimeUnit.SECONDS).isSuccess();
        log.info("tryLockWithoutLeaseTime 加锁结果 result: {}", result);
        return result;
    }

    /**
     * 释放锁
     * @param lockName 锁名字
     */
    public void unLock(String lockName) {
        if (ObjectUtils.isEmpty(lockName)) return ;
        RLock lock = redissonClient.getLock(lockName);
        lock.unlock();
    }

    /**
     * 释放锁
     * @param lockName 锁名字
     */
    public void unLock(Object lockName) {
        unLock(lockName.toString());
    }

    /**
     * 锁是否已加锁
     * @param lockName 锁名字
     * @return 返回
     */
    public boolean lockIsLocked(String lockName) {
        return redissonClient.getLock(lockName).isLocked();
    }

    /**
     * 锁是否已加锁
     * @param lockName 锁名字
     * @return 返回
     */
    public boolean lockIsLocked(Object lockName) {
        return lockIsLocked(lockName.toString());
    }

}
