package work.yj1211.live.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import static org.springframework.web.cors.CorsConfiguration.ALL;

@Configuration
public class CORSConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                //注册一个全路径映射
                registry.addMapping("/**")
                        .allowedOriginPatterns("*")
                        .allowedMethods(ALL)
                        .allowedHeaders(ALL)
                        .allowCredentials(true);
            }
        };
    }

}
