package indiv.hmdp.controller;

import indiv.hmdp.entity.dto.Result;
import indiv.hmdp.entity.po.ShopTypePO;
import indiv.hmdp.service.api.IShopTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {
    @Resource
    private IShopTypeService typeService;

    @GetMapping("list")
    public Result queryTypeList() {
        List<ShopTypePO> typeList = typeService
                .query().orderByAsc("sort").list();
        return Result.ok(typeList);
    }
}
