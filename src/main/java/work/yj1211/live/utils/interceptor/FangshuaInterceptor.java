package work.yj1211.live.utils.interceptor;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import work.yj1211.live.utils.annotation.AccessLimit;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

/**
 * @author YJ1211
 * @created 2023/1/26 11:48
 */
@Slf4j
@Component
public class FangshuaInterceptor extends HandlerInterceptorAdapter {
    @Resource
    RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handler1 = (HandlerMethod) handler;
            //3. 获取方法中的注解,看是否有该注解
            AccessLimit accessLimit = handler1.getMethodAnnotation(AccessLimit.class);
            //3.1 : 不包含此注解,则不进行操作
            if (accessLimit != null) {
                //3.2 ： 判断请求是否受限制
                if (isLimit(request, accessLimit)) {
                    render(response, "{\"code\":\"30001\",\"message\":\"请求过快\"}");
                    return false;
                }
            }
        }
        return true;
    }

    //判断请求是否受限
    public boolean isLimit(HttpServletRequest request, AccessLimit accessLimit) {
        // 受限的redis 缓存key ,因为这里用浏览器做测试，我就用sessionid 来做唯一key,如果是app ,可以使用 用户ID 之类的唯一标识。
        String limitKey = request.getParameter("uid");
        if (StrUtil.isEmpty(limitKey)) {
            return true;
        }
        // 从缓存中获取，当前这个请求访问了几次
        Integer redisCount = (Integer) redisTemplate.opsForValue().get(limitKey);
        if (redisCount == null) {
            //初始 次数
            redisTemplate.opsForValue().set(limitKey, 1, accessLimit.seconds(), TimeUnit.SECONDS);
        } else {
            if (redisCount.intValue() >= accessLimit.maxCount()) {
                log.warn("【防刷】uid:[{}]  超过请求限制，拦截！！", limitKey);
                return true;
            }
            log.info("【防刷】uid:[{}]  [{}]秒内第[{}]次搜索", limitKey, accessLimit.seconds(), redisCount.intValue()+1);
            // 次数自增
            redisTemplate.opsForValue().increment(limitKey);
        }
        return false;
    }

    private void render(HttpServletResponse response, String cm) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        OutputStream out = response.getOutputStream();
        out.write(cm.getBytes("UTF-8"));
        out.flush();
        out.close();
    }
}
