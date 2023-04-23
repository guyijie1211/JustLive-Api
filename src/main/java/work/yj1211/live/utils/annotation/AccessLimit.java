package work.yj1211.live.utils.annotation;

import java.lang.annotation.*;

/**
 * @author guyijie1211
 * @created 2023/1/26 11:46
 */
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AccessLimit {
    /**
     * 在  seconds 秒内 , 最大只能请求 maxCount 次
     * @return
     */
    int seconds() default 60;

    //   最大数量
    int maxCount() default 10;
}
