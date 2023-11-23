package indiv.hmdp.service.api;

import indiv.hmdp.entity.po.UserPO;

public interface UserService {

    UserPO getUserInfoByUserId(Integer id);

    UserPO getUserByEmail(String email);
    UserPO getUserByPhone(String phone);

    UserPO createUserWithEmail(String email);

    UserPO createUserWithPhone(String phone);

    UserPO getById(Long userId);
}
