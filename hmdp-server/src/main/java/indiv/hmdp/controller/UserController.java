package indiv.hmdp.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import indiv.hmdp.common.redis.RedisClient;
import indiv.hmdp.constants.EmailTemplate;
import indiv.hmdp.constants.RedisConstants;
import indiv.hmdp.constants.UserConstant;
import indiv.hmdp.entity.dto.LoginFormDTO;
import indiv.hmdp.entity.dto.Result;
import indiv.hmdp.entity.dto.UserDTO;
import indiv.hmdp.entity.po.UserInfoPO;
import indiv.hmdp.entity.po.UserPO;
import indiv.hmdp.service.api.IUserInfoService;
import indiv.hmdp.service.api.UserInfoService;
import indiv.hmdp.service.api.UserService;
import indiv.hmdp.utils.EmailUtil;
import indiv.hmdp.utils.RegexUtils;
import indiv.hmdp.utils.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;
    @Autowired
    IUserInfoService userInfoService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @PostMapping("login")
    public Result login(@RequestBody LoginFormDTO loginForm) {
        // 1.校验手机号
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2.如果不符合，返回错误信息
            return Result.fail("手机号格式错误！");
        }
        // 3.从redis获取验证码并校验
        String cacheCode = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + phone);
        log.debug("验证码 {}", cacheCode);
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
            // 不一致，报错
            return Result.fail("验证码错误");
        }

        // 4.一致，根据手机号查询用户 select * from tb_user where phone = ?
        UserPO user = userService.getUserByPhone(phone);

        // 5.判断用户是否存在
        if (user == null) {
            // 6.不存在，创建新用户并保存
            user = userService.createUserWithPhone(phone);
        }

        // 7.保存用户信息到 redis中
        // 7.1.随机生成token，作为登录令牌
        String token = UUID.randomUUID().toString(true);
        // 7.2.将User对象转为HashMap存储
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> {
                            if (fieldValue == null){
                                fieldValue = "";
                            }else {
                                fieldValue = fieldValue.toString();
                            }
                            return fieldValue;
                        }));
        // 7.3.存储
        String tokenKey = RedisConstants.LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        // 7.4.设置token有效期
        stringRedisTemplate.expire(tokenKey, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);

        // 8.返回token
        return Result.ok(token);
    }

    @PostMapping("code")
    public Result sendCode(@RequestParam("phone") String phone, HttpSession session) {
        // 1.校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2.如果不符合，返回错误信息
            return Result.fail("手机号格式错误！");
        }
        // 3.符合，生成验证码
        String code = RandomUtil.randomNumbers(6);

        // 4.保存验证码到 redis
        stringRedisTemplate.opsForValue().set(RedisConstants.LOGIN_CODE_KEY + phone, code,RedisConstants.LOGIN_CODE_TTL,TimeUnit.MINUTES);
        // 5.发送验证码, 这里就不真发了
        log.debug("发送短信验证码成功，验证码：{}", code);
        // 返回ok
        return Result.ok();
    }

    @PostMapping("email/code")
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
        stringRedisTemplate.opsForValue().set(EmailTemplate.EMAIL_KEY + email, code, Duration.ofMinutes(EmailTemplate.VALIDATE_TIME));
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
    public Result info(@PathVariable("id") Long userId) {
        // 查询详情
        UserInfoPO info = userInfoService.getById(userId);
        if (info == null) {
            // 没有详情，应该是第一次查看详情
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // 返回
        return Result.ok();
    }

}
