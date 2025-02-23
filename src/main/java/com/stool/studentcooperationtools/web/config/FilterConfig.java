package com.stool.studentcooperationtools.web.config;

import com.stool.studentcooperationtools.web.filter.RequestCachingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean<RequestCachingFilter> loggingFilter() {
        FilterRegistrationBean<RequestCachingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestCachingFilter());
        registrationBean.addUrlPatterns("/api/*","/");
        return registrationBean;
    }
}
