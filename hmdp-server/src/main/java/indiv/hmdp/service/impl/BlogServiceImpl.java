package indiv.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import indiv.hmdp.entity.po.BlogPO;
import indiv.hmdp.mapper.BlogMapper;
import indiv.hmdp.service.api.IBlogService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, BlogPO> implements IBlogService {

}
