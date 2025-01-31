package com.stool.studentcooperationtools.aop.LogTrace.config;

import com.stool.studentcooperationtools.aop.LogTrace.LogTrace;
import com.stool.studentcooperationtools.aop.LogTrace.ThreadLocalLogTrace;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogTraceConfig {

    @Bean
    public LogTrace logTrace(){
        return new ThreadLocalLogTrace();
    }

}
