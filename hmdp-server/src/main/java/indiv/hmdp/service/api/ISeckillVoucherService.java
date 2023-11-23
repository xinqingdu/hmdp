package indiv.hmdp.service.api;

import com.baomidou.mybatisplus.extension.service.IService;
import indiv.hmdp.entity.dto.Result;
import indiv.hmdp.entity.po.SeckillVoucherPO;
import indiv.hmdp.entity.po.VoucherPO;

/**
 * <p>
 * 秒杀优惠券表，与优惠券是一对一关系 服务类
 * </p>
 *
 * @author 虎哥
 * @since 2022-01-04
 */
public interface ISeckillVoucherService extends IService<SeckillVoucherPO> {

}
