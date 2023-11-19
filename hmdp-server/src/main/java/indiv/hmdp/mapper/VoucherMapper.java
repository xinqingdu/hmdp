package indiv.hmdp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import indiv.hmdp.entity.po.VoucherPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface VoucherMapper extends BaseMapper<VoucherPO> {

    List<VoucherPO> queryVoucherOfShop(@Param("shopId") Long shopId);
}
