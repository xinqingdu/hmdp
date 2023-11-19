package indiv.hmdp.service.api;

import indiv.hmdp.entity.po.UserInfoPO;

public interface UserInfoService {
    UserInfoPO getById(Long userId);
}
