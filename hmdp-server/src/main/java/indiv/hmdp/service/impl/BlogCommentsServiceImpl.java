package indiv.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import indiv.hmdp.entity.po.BlogCommentsPO;
import indiv.hmdp.mapper.BlogCommentsMapper;
import indiv.hmdp.service.api.IBlogCommentsService;
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
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogCommentsPO> implements IBlogCommentsService {

}
