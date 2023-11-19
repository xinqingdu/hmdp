package indiv.hmdp.service.impl;

import indiv.hmdp.entity.po.UserInfoPO;
import indiv.hmdp.mapper.UserInfoMapper;
import indiv.hmdp.mapper.UserMapper;
import indiv.hmdp.service.api.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
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
public class UserInfoServiceImpl  implements UserInfoService {
    @Autowired
    UserMapper userMapper;
    @Autowired
    UserInfoMapper userInfoMapper;
    @Override
    public UserInfoPO getById(Long userId) {
        return userInfoMapper.selectById(userId);
    }
}
