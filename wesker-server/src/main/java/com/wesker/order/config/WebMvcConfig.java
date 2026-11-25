package com.wesker.order.config;

import com.wesker.order.interceptor.JwtTokenAdminInterceptor;
import com.wesker.order.properties.AliOssProperties;
import com.wesker.order.properties.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({JwtProperties.class, AliOssProperties.class})
public class WebMvcConfig implements WebMvcConfigurer {
    private final JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    public WebMvcConfig(JwtTokenAdminInterceptor jwtTokenAdminInterceptor) {
        this.jwtTokenAdminInterceptor = jwtTokenAdminInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/employee/login");
    }
}
