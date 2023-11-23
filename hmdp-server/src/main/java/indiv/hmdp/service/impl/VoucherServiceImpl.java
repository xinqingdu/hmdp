package indiv.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import indiv.hmdp.constants.RedisConstants;
import indiv.hmdp.entity.dto.Result;
import indiv.hmdp.entity.po.SeckillVoucherPO;
import indiv.hmdp.entity.po.VoucherPO;
import indiv.hmdp.mapper.VoucherMapper;
import indiv.hmdp.service.api.ISeckillVoucherService;
import indiv.hmdp.service.api.IVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, VoucherPO> implements IVoucherService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryVoucherOfShop(Long shopId) {
        // 查询优惠券信息
        List<VoucherPO> vouchers = getBaseMapper().queryVoucherOfShop(shopId);
        // 返回结果
        return Result.ok(vouchers);
    }

    @Override
    @Transactional
    public void addSeckillVoucher(VoucherPO voucher) {
        // 保存优惠券
        save(voucher);
        // 保存秒杀信息
        SeckillVoucherPO seckillVoucher = new SeckillVoucherPO();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucherService.save(seckillVoucher);
        stringRedisTemplate.opsForValue().set(RedisConstants.SECKILL_STOCK_KEY+voucher.getId(), voucher.getStock().toString());
    }
}
