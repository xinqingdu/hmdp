package indiv.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import indiv.hmdp.entity.po.UserInfoPO;
import indiv.hmdp.mapper.UserInfoMapper;
import indiv.hmdp.service.api.IUserInfoService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-24
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfoPO> implements IUserInfoService {

}
