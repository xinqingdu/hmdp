package indiv.hmdp.entity.vo;

import lombok.Data;

@Data
public class UserVO {
    private String email;

    /**
     * 昵称，默认是随机字符
     */
    private String nickName;
}
