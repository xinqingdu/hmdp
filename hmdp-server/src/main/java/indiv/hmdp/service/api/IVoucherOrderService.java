package indiv.hmdp.service.api;

import com.baomidou.mybatisplus.extension.service.IService;
import indiv.hmdp.entity.dto.Result;
import indiv.hmdp.entity.po.VoucherOrderPO;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IVoucherOrderService extends IService<VoucherOrderPO> {

    // 领取秒杀优惠券
    Result seckillVoucher(Long voucherId);

    @Transactional
    Result createSeckillVoucherOrderAsyn(VoucherOrderPO voucherOrderPO);

    Result createSeckillVoucherOrder(Long voucherId);
}
