package indiv.hmdp.service.api;

import java.time.Duration;

public interface RedisService {
    void set(String s, String code, Duration ofMinutes);

    String get(String s);
}
