package indiv.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import indiv.hmdp.common.redis.*;
import indiv.hmdp.constants.*;
import indiv.hmdp.entity.dto.Result;
import indiv.hmdp.entity.po.SeckillVoucherPO;
import indiv.hmdp.entity.po.VoucherOrderPO;
import indiv.hmdp.mapper.VoucherOrderMapper;
import indiv.hmdp.service.api.ISeckillVoucherService;
import indiv.hmdp.service.api.IVoucherOrderService;
import indiv.hmdp.utils.UserHolder;
import org.redisson.api.RLock;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrderPO> implements IVoucherOrderService {
    public static final DefaultRedisScript<Long> SECKILL_SCRIPT = new DefaultRedisScript<>();

    public static final ExecutorService SECKILL_POOL = Executors.newFixedThreadPool(10);

    public static final BlockingQueue<VoucherOrderPO> taskQueue = new ArrayBlockingQueue<>(1024);

    @PostConstruct
    private void init() {
        SECKILL_POOL.submit(() -> {
            while (true) {
                try {
                    VoucherOrderPO poll = taskQueue.take();
                    Result result = handleSeckillVoucherOrderAsyn(poll);
                    if (!result.getSuccess()) {
                        log.debug(result.getErrorMsg());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
    }

    static {
        SECKILL_SCRIPT.setLocation((org.springframework.core.io.Resource) new ClassPathResource("scripts/seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    @Resource
    RedisStringClient redisStringClient;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ISeckillVoucherService seckillVoucherService;

    @Autowired
    Lock<String> redisLock;

    @Autowired
    RedissonLockClient redissonLockClient;


    public Result seckillVoucherAsyn(Long voucherId) {
        Long userId = UserHolder.getUser().getId();
        long orderId = IDCreator.nextId(PrefixConstants.ORDER_PREFIX);
        Long result = stringRedisTemplate.execute(SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(),
                userId.toString(),
                String.valueOf(orderId)
        );
        if (result != 0) {
            if (result != 1) {
                return Result.fail("优惠券库存不足");
            } else {
                return Result.fail("不能重复下单");
            }
        }
        VoucherOrderPO voucherOrderPO = new VoucherOrderPO();
        voucherOrderPO.setId(orderId);
        voucherOrderPO.setUserId(userId);
        voucherOrderPO.setVoucherId(voucherId);
        taskQueue.add(voucherOrderPO);
        return Result.ok(orderId);
    }

    public Result seckillVoucherSyn(Long voucherId) {
        // 考虑的问题
        // 1. 秒杀开始和库存足够才能抢券
        SeckillVoucherPO seckillVoucherPO = seckillVoucherService.getById(voucherId);
        if (seckillVoucherPO == null) {
            return Result.fail("优惠券不存在");
        }
        if (seckillVoucherPO.getBeginTime().isAfter(LocalDateTime.now())) {
            return Result.fail("秒杀还未开始");
        }
        if (seckillVoucherPO.getEndTime().isBefore(LocalDateTime.now())) {
            return Result.fail("秒杀已经结束");
        }
        if (seckillVoucherPO.getStock() <= 0) {
            return Result.fail("优惠券库存不足");
        }
        // 2. 用户并发抢券等待时间问题，即并发性能问题
        Long uid = UserHolder.getUser().getId();
//        synchronized (uid.toString().intern()) {
//            IVoucherOrderService iVoucherOrderService = (IVoucherOrderService) AopContext.currentProxy();
//            return iVoucherOrderService.createSeckillVoucherOrder(voucherId);
//        }
        // 解决分布式系统中的线程安全问题
        String lockKey = RedisConstants.LOCK_KEY + RedisConstants.SECKILL_KEY + RedisConstants.VOUCHER_KEY + RedisConstants.ORDER_KEY + uid.toString().intern();
        String value = String.valueOf(Thread.currentThread().getId());
        boolean lock = redisLock.lock(lockKey, value);

        // Redisson 分布式锁
        RLock redissonLock = redissonLockClient.getLock(lockKey);
        lock = redissonLock.tryLock();
        if (!lock) {
            return Result.fail("不能重复下单");
        }
        try {
            IVoucherOrderService iVoucherOrderService = (IVoucherOrderService) AopContext.currentProxy();
            return iVoucherOrderService.createSeckillVoucherOrder(voucherId);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            redisLock.unlock(lockKey);
//            redisLock.unlock(lockKey, value);
            redissonLock.unlock();
        }
        return Result.ok();
    }


    public Result handleSeckillVoucherOrderAsyn(VoucherOrderPO voucherOrderPO) {
        Long uid = UserHolder.getUser().getId();
        // 解决分布式系统中的线程安全问题
        String lockKey = RedisConstants.LOCK_KEY + RedisConstants.SECKILL_KEY + RedisConstants.VOUCHER_KEY + RedisConstants.ORDER_KEY + uid.toString().intern();
        String value = String.valueOf(Thread.currentThread().getId());
        boolean lock = redisLock.lock(lockKey, value);

        // Redisson 分布式锁
        RLock redissonLock = redissonLockClient.getLock(lockKey);
        lock = redissonLock.tryLock();
        if (!lock) {
            return Result.fail("不能重复下单");
        }
        try {
            IVoucherOrderService iVoucherOrderService = (IVoucherOrderService) AopContext.currentProxy();
            return iVoucherOrderService.createSeckillVoucherOrderAsyn(voucherOrderPO);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            redisLock.unlock(lockKey);
//            redisLock.unlock(lockKey, value);
            redissonLock.unlock();
        }
        return Result.ok();
    }

    @Transactional
    @Override
    public Result createSeckillVoucherOrderAsyn(VoucherOrderPO voucherOrderPO) {
        // 3. 同一批或同一类型优惠券一人只能领取一个
        Long uid = UserHolder.getUser().getId();
        Integer count = query().eq("user_id", uid).eq("voucher_id", voucherOrderPO.getVoucherId()).count();
        if (count > 0) {
            return Result.fail(OrderConstants.ORDER_EXIST);
        }
        // 4. 解决超卖问题
        boolean update = seckillVoucherService.update().setSql("stock = stock - 1").eq("voucher_id", voucherOrderPO.getVoucherId()).gt("stock", 0).update();
        if (!update) {
            return Result.fail(OrderConstants.STOCK_NOT_ENOUGH);
        }
        save(voucherOrderPO);
        return Result.ok(voucherOrderPO.getId());
    }


    @Transactional
    public Result createSeckillVoucherOrder(Long voucherId) {
        // 3. 同一批或同一类型优惠券一人只能领取一个
        Long uid = UserHolder.getUser().getId();
        Integer count = query().eq("user_id", uid).eq("voucher_id", voucherId).count();
        if (count > 0) {
            return Result.fail(OrderConstants.ORDER_EXIST);
        }
        // 4. 解决超卖问题
        boolean update = seckillVoucherService.update().setSql("stock = stock - 1").eq("voucher_id", voucherId).gt("stock", 0).update();
        if (!update) {
            return Result.fail(OrderConstants.STOCK_NOT_ENOUGH);
        }
        VoucherOrderPO voucherOrderPO = new VoucherOrderPO();
        long orderId = IDCreator.nextId(PrefixConstants.ORDER_PREFIX);
        voucherOrderPO.setId(orderId);
        voucherOrderPO.setUserId(uid);
        voucherOrderPO.setVoucherId(voucherId);
        save(voucherOrderPO);
        return Result.ok(orderId);
    }

    @Override
    public Result seckillVoucher(Long voucherId) {
        return seckillVoucherAsyn(voucherId);
    }




    private class VoucherOrderHandler implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    // 1.获取消息队列中的订单信息 XREADGROUP GROUP g1 c1 COUNT 1 BLOCK 2000 STREAMS s1 >
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create("stream.orders", ReadOffset.lastConsumed())
                    );
                    // 2.判断订单信息是否为空
                    if (list == null || list.isEmpty()) {
                        // 如果为null，说明没有消息，继续下一次循环
                        continue;
                    }
                    // 解析数据
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> value = record.getValue();
                    VoucherOrderPO voucherOrder = BeanUtil.fillBeanWithMap(value, new VoucherOrderPO(), true);
                    // 3.创建订单
                    createSeckillVoucherOrderAsyn(voucherOrder);
                    // 4.确认消息 XACK
                    stringRedisTemplate.opsForStream().acknowledge("s1", "g1", record.getId());
                } catch (Exception e) {
                    log.error("处理订单异常", e);
                    //处理异常消息
                    handlePendingList();
                }
            }
        }

        private void handlePendingList() {
            while (true) {
                try {
                    // 1.获取pending-list中的订单信息 XREADGROUP GROUP g1 c1 COUNT 1 BLOCK 2000 STREAMS s1 0
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1),
                            StreamOffset.create("stream.orders", ReadOffset.from("0"))
                    );
                    // 2.判断订单信息是否为空
                    if (list == null || list.isEmpty()) {
                        // 如果为null，说明没有异常消息，结束循环
                        break;
                    }
                    // 解析数据
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> value = record.getValue();
                    VoucherOrderPO voucherOrder = BeanUtil.fillBeanWithMap(value, new VoucherOrderPO(), true);
                    // 3.创建订单
                    createSeckillVoucherOrderAsyn(voucherOrder);
                    // 4.确认消息 XACK
                    stringRedisTemplate.opsForStream().acknowledge("s1", "g1", record.getId());
                } catch (Exception e) {
                    log.error("处理pendding订单异常", e);
                    try{
                        Thread.sleep(20);
                    }catch(Exception ee){
                        ee.printStackTrace();
                    }
                }
            }
        }
    }

}
