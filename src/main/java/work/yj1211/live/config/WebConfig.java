package work.yj1211.live.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import work.yj1211.live.utils.interceptor.FangshuaInterceptor;

/**
 * @author YJ1211
 * @created 2023/1/26 11:57
 */
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private FangshuaInterceptor interceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor);
    }
}
