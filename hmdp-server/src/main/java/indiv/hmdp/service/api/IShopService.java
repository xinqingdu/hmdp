package indiv.hmdp.service.api;

import com.baomidou.mybatisplus.extension.service.IService;
import indiv.hmdp.entity.po.ShopPO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IShopService extends IService<ShopPO> {

    ShopPO queryById(Long id);

    boolean update(ShopPO shop);
}
