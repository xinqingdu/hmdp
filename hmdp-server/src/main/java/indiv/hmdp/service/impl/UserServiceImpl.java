package indiv.hmdp.service.impl;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import indiv.hmdp.entity.po.UserPO;
import indiv.hmdp.mapper.UserMapper;
import indiv.hmdp.service.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery;
import static com.baomidou.mybatisplus.core.toolkit.Wrappers.query;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;
    private static QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();

    @Override
    public UserPO getUserInfoByUserId(Integer id) {
        return userMapper.selectById(id);
    }

    @Override
    public UserPO getUserByEmail(String email) {
        return userMapper.selectOne(queryWrapper.eq("email", email));
    }

    @Override
    public UserPO getUserByPhone(String phone) {
        return userMapper.selectOne(queryWrapper.eq("phone",phone));
    }

    @Override
    public UserPO createUserWithEmail(String email) {
        UserPO userPO = new UserPO();
        userPO.setEmail(email);
        String password = UUID.randomUUID().toString();
        String pass = new Digester(DigestAlgorithm.MD5).digestHex(password);
        userPO.setPassword(pass);
        userMapper.insert(userPO);
        return userPO;
    }

    @Override
    public UserPO createUserWithPhone(String phone) {
        UserPO userPO = new UserPO();
        userPO.setPhone(phone);
        String password = UUID.randomUUID().toString();
        String pass = new Digester(DigestAlgorithm.MD5).digestHex(password);
        userPO.setPassword(pass);
        userMapper.insert(userPO);
        return userPO;
    }

    @Override
    public UserPO getById(Long userId) {
        return userMapper.selectById(userId);
    }
}
