package com.stool.studentcooperationtools.aop.aspect.config;

import com.stool.studentcooperationtools.aop.aspect.ExecutionTimeLoggerAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AspectConfig {
    @Bean
    public ExecutionTimeLoggerAspect executionTimeLoggerAspect(){
        return new ExecutionTimeLoggerAspect();
    }
}
