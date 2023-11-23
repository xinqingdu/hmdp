package indiv.hmdp.common.redis;

import java.util.concurrent.TimeUnit;

public interface Lock<T> {
    boolean lock(T t);

    boolean lock(String key, String value);

    boolean lock(T t, Long time);

    boolean lock(String key, String value, Long time);

    boolean lock(String key, String value, Long time, TimeUnit unit);

    void unlock(T t);

    void unlock(String key, String expect);
}
