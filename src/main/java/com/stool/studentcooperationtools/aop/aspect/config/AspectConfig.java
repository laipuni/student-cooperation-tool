package com.stool.studentcooperationtools.aop.aspect.config;

import com.stool.studentcooperationtools.aop.LogTrace.LogTrace;
import com.stool.studentcooperationtools.aop.aspect.LogTraceAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AspectConfig {

    @Bean
    public LogTraceAspect logTraceAspect(LogTrace logTrace){
        return new LogTraceAspect(logTrace);
    }

}
