package indiv.hmdp.service.impl;

import indiv.hmdp.service.api.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisServiceImpl implements RedisService {
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Override
    public void set(String s, String code, Duration ofMinutes) {
        stringRedisTemplate.opsForValue().set(s, code, ofMinutes);
    }
    public String get(String key){
        return stringRedisTemplate.opsForValue().get(key);
    }

}
