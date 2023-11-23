package indiv.hmdp.service.api;

import com.baomidou.mybatisplus.extension.service.IService;
import indiv.hmdp.entity.dto.Result;
import indiv.hmdp.entity.po.VoucherPO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IVoucherService extends IService<VoucherPO> {

    Result queryVoucherOfShop(Long shopId);

    void addSeckillVoucher(VoucherPO voucher);
}
