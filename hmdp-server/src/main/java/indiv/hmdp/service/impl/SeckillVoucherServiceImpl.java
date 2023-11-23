package indiv.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import indiv.hmdp.entity.dto.Result;
import indiv.hmdp.entity.po.SeckillVoucherPO;
import indiv.hmdp.entity.po.VoucherPO;
import indiv.hmdp.mapper.SeckillVoucherMapper;
import indiv.hmdp.service.api.ISeckillVoucherService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 秒杀优惠券表，与优惠券是一对一关系 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2022-01-04
 */
@Service
public class SeckillVoucherServiceImpl extends ServiceImpl<SeckillVoucherMapper, SeckillVoucherPO> implements ISeckillVoucherService {


}
