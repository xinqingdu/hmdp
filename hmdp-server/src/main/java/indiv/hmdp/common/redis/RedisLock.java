package indiv.hmdp.common.redis;

import cn.hutool.core.io.resource.ClassPathResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @author
 * @date 2023/11/20 22 06
 * discription
 */
@Component
public class RedisLock implements Lock<String> {

    public DefaultRedisScript<Long> UNLOCK_SCRIPT;
    @Autowired
    public StringRedisTemplate stringRedisTemplate;

    public RedisLock() {
        this("scripts/unlock.lua");
    }


    public RedisLock(String path) {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation((Resource) new ClassPathResource(path));
        UNLOCK_SCRIPT.setResultType(Long.class);
    }

    @Override
    public boolean lock(String key) {
        return lock(key, 10L);
    }

    @Override
    public boolean lock(String key, String value) {
        return lock(key, value, 10L);
    }

    @Override
    public boolean lock(String key, Long time) {
        return lock(key, "1", time);
    }

    @Override
    public boolean lock(String key, String value, Long time) {
        return lock(key, value, time, TimeUnit.SECONDS);
    }

    @Override
    public boolean lock(String key, String value, Long time, TimeUnit unit) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, value, time, unit);
        return Boolean.TRUE.equals(flag);
    }

    @Override
    public void unlock(String key) {
        stringRedisTemplate.delete(key);
    }

    @Override
    public void unlock(String key, String expect) {
        stringRedisTemplate.execute(
                UNLOCK_SCRIPT,
                Collections.singletonList(key),
                expect);
    }
}
