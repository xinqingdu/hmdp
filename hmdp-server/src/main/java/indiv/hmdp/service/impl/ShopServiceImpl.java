package indiv.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import indiv.hmdp.common.redis.RedisLock;
import indiv.hmdp.common.redis.RedisStringClient;
import indiv.hmdp.constants.RedisConstants;
import indiv.hmdp.common.redis.RedisData;
import indiv.hmdp.entity.po.ShopPO;
import indiv.hmdp.mapper.ShopMapper;
import indiv.hmdp.service.api.IShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, ShopPO> implements IShopService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedisLock redisLock;

    @Autowired
    RedisStringClient redisStringClient;

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(5);


    @Override
    public ShopPO queryById(Long id) {
        // 解决缓存穿透
        ShopPO shop = redisStringClient
                .queryWithPassThrough(RedisConstants.CACHE_SHOP_KEY, id, ShopPO.class, this::getById, RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 互斥锁解决缓存击穿
        // Shop shop = redisStringClient
        //         .queryWithMutex(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 逻辑过期解决缓存击穿
        // Shop shop = redisStringClient
        //         .queryWithLogicalExpire(CACHE_SHOP_KEY, id, Shop.class, this::getById, 20L, TimeUnit.SECONDS);

//        String shopInfo = stringRedisTemplate.opsForValue().get(RedisConstants.CACHE_SHOP_KEY + id);
//        if (StrUtil.isNotBlank(shopInfo)) {
//            return JSONUtil.toBean(shopInfo, ShopPO.class);
//        }
//        ShopPO shop = getById(id);
//        stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
        return shop;
    }

    public ShopPO queryByIdWithLogicExpire(Long id) {
        // 1. 查询缓存
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        String shopRedisData = stringRedisTemplate.opsForValue().get(key);
        // 2. 缓存不存在，直接返回
        if (StrUtil.isBlank(shopRedisData)) {
            return null;
        }
        RedisData redisData = JSONUtil.toBean(shopRedisData, RedisData.class);
        ShopPO shop = JSONUtil.toBean((JSONObject) redisData.getData(), ShopPO.class);
        // 3. 缓存未过期，直接返回数据
        if (redisData.getExpireTime().isAfter(LocalDateTime.now())) {
            return shop;
        }
        // 4. 缓存过期了，加锁异步刷新缓存
        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        boolean isLock = redisLock.lock(lockKey);
        // 4.1.判断是否获取锁成功
        if (isLock) {
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    // 重建缓存
                    this.saveShopRedis(id, RedisConstants.CACHE_SHOP_TTL);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    redisLock.unlock(lockKey);
                }
            });
        }
        // 4.2 获取锁失败，返回脏数据
        return shop;
    }

    public void saveShopRedis(Long id, Long time) {
        ShopPO shop = getById(id);
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusMinutes(time));
        stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(redisData));
    }

    @Transactional
    @Override
    public boolean update(ShopPO shop) {
        ShopPO byId = getById(shop.getId());
        if (byId == null) {
            return false;
        }
        updateById(shop);
        stringRedisTemplate.delete(RedisConstants.CACHE_SHOP_KEY);
        return true;
    }

}
