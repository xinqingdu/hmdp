package indiv.hmdp.interceptor;

import com.hmdp.dto.UserDTO;
import indiv.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class RefreshTokenInterceptor implements HandlerInterceptor {
    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {

    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        return true;
    }
}
