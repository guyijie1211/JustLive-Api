package work.yj1211.live.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author YJ1211
 * @created 2023/1/25 17:08
 */
@Aspect
@Component
@Slf4j
public class CostTimeAspect {
    @Pointcut("execution(* work.yj1211.live.controller.*.*(..))")
    public void costTime() {}

    @Pointcut("execution(* work.yj1211.live.utils.platForms.*.*(..))")
    public void costTimePlatform() {}

    @Around("costTime()")
    public Object costTimeAround(ProceedingJoinPoint joinPoint) {
        Object obj = null;
        try {
            long beginTime = System.currentTimeMillis();
            obj = joinPoint.proceed();
            //获取方法名称
            String method = joinPoint.getSignature().getName();
            //获取类名称
            String className=joinPoint.getSignature().getDeclaringTypeName();
            //获取参数
            Object[] args = joinPoint.getArgs();
            //计算耗时
            long end = System.currentTimeMillis();
            float cost = end - beginTime;
            if (cost > 1000) {
                if (args.length > 0) {
                    log.info("接口:[{}]耗时:[{}],参数{}", className + "." + method, cost / 1000 + "秒", args);
                } else {
                    log.info("接口:[{}]耗时:[{}]", className + "." + method, cost / 1000 + "秒");
                }
            }
        } catch (Throwable throwable) {
            log.error("切面costTimeAround异常\n{}", throwable);
        }
        return obj;
    }

    @Around("costTimePlatform()")
    public Object costTimeAroundPlatform(ProceedingJoinPoint joinPoint) {
        Object obj = null;
        try {
            long beginTime = System.currentTimeMillis();
            obj = joinPoint.proceed();
            //获取方法名称
            String method = joinPoint.getSignature().getName();
            //获取类名称
            String className=joinPoint.getSignature().getDeclaringTypeName();
            //获取参数
            Object[] args = joinPoint.getArgs();
            //计算耗时
            long end = System.currentTimeMillis();
            float cost = end - beginTime;
            if (cost > 500) {
                if (args.length > 0) {
                    log.info("调用平台:[{}]耗时:[{}],参数{}", className + "." + method, cost / 1000 + "秒", args);
                } else {
                    log.info("调用平台:[{}]耗时:[{}]", className + "." + method, cost / 1000 + "秒");
                }
            }
        } catch (Throwable throwable) {
            log.error("切面costTimeAround异常\n{}", throwable);
        }
        return obj;
    }

    @Before("costTime()")
    public void doBefore(JoinPoint joinPoint) {
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        // 记录请求的地址
        log.info("IP:[{}],URL:[{}]", request.getRemoteAddr(), request.getRequestURL().toString());
    }

}
