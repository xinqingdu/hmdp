package indiv.hmdp.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import com.hmdp.utils.RegexUtils;
import indiv.hmdp.constants.EmailTemplate;
import indiv.hmdp.constants.UserConstant;
import indiv.hmdp.entity.dto.LoginFormDTO;
import indiv.hmdp.entity.dto.UserDTO;
import indiv.hmdp.entity.po.UserInfoPO;
import indiv.hmdp.entity.po.UserPO;
import indiv.hmdp.entity.vo.UserVO;
import indiv.hmdp.service.api.RedisService;
import indiv.hmdp.service.api.UserInfoService;
import indiv.hmdp.service.api.UserService;
import indiv.hmdp.utils.EmailUtil;
import indiv.hmdp.utils.ResultUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
public class UserController {

    @Autowired
    UserService userService;
    @Autowired
    UserInfoService userInfoService;

    @Autowired
    RedisService redisService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @PostMapping("/user/login")
    public ResultUtil<String> login(@RequestBody LoginFormDTO loginForm) {
        // 1.校验手机号
        String email = loginForm.getEmail();
        if (RegexUtils.isEmailInvalid(email)) {
            // 2.如果不符合，返回错误信息
            return ResultUtil.fail("邮箱格式错误！");
        }
        // 3.校验验证码
        String cacheCode = redisService.get(EmailTemplate.EMAIL_KEY + email);
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
            //3.不一致，报错
            return ResultUtil.fail("验证码错误");
        }
        UserPO userByEmail = userService.getUserByEmail(email);
        if (userByEmail == null) {
            userByEmail = userService.createUserWithEmail(email);
        }
        String token = UUID.randomUUID().toString(true);
        UserDTO userDTO = BeanUtil.copyProperties(userByEmail, UserDTO.class);

        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        // 7.3.存储
        String tokenKey = EmailTemplate.EMAIL_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        // 7.4.设置token有效期
        stringRedisTemplate.expire(tokenKey, UserConstant.LOGIN_USER_TTL, TimeUnit.MINUTES);
        return ResultUtil.success(token);
    }

    @PostMapping("/user/code")
    public ResultUtil<String> sendSms(@RequestParam("email") String email) {

        if (RegexUtils.isEmailInvalid(email)) {
            return ResultUtil.fail("邮箱格式错误！");
        }
        //生成验证码，并发送
        String code = EmailUtil.getEmailCode(8);
        String content = EmailTemplate.getEmailMessage(code, EmailTemplate.VALIDATE_TIME);
        boolean sendRes = EmailUtil.send(email, EmailTemplate.EMAIL_SUBJECT, content, EmailTemplate.CONTENT_TYPE);
        if (!sendRes) return ResultUtil.fail();
        //发送成功后，存入redis...
        redisService.set(EmailTemplate.EMAIL_KEY + email, code, Duration.ofMinutes(EmailTemplate.VALIDATE_TIME));
        return ResultUtil.success();
    }

    @PostMapping("/logout")
    public ResultUtil<String> logout() {
        // TODO 实现登出功能
        return ResultUtil.success();
    }

    @GetMapping("/me")
    public ResultUtil<String> me() {
        // TODO 获取当前登录的用户并返回
        return ResultUtil.success();
    }

    @GetMapping("/info/{id}")
    public ResultUtil<String> info(@PathVariable("id") Long userId) {
        // 查询详情
        UserInfoPO info = userInfoService.getById(userId);
        if (info == null) {
            // 没有详情，应该是第一次查看详情
            return ResultUtil.success();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // 返回
        return ResultUtil.success();
    }

}
