package indiv.hmdp.entity.dto;

import lombok.Data;

@Data
public class LoginFormDTO {
    private String email;
    private String code;
    private String password;
}
