package indiv.hmdp.service.api;

import indiv.hmdp.entity.po.UserPO;

public interface UserService {

    UserPO getUserInfoByUserId(Integer id);

    UserPO getUserByEmail(String email);

    UserPO createUserWithEmail(String email);
}
