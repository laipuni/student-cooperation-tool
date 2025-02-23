package com.stool.studentcooperationtools.aop.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;

/**
 * 일정 수행시간(ms기준)이상 걸렸을 경우, 해당 api의 수행 과정을 로그로 찍는다.
 */
@Slf4j
@Aspect
public class ExecutionTimeLoggerAspect {

    //기본적으로 100ms를 넘는 api에 warn로그를 남기도록 했음
    //하지만 100ms가 적절한 수행시간 기준인지, 네트워크 지연, 서버부하로 인해 느려진다면 게속해서 로그를 남길 위험이 있음
    //추후에 redis를 통해 api별 평균 시간, 지연 횟수를 통해 로그를 남기도록 바꿀 예정
    @Value("${logging.executionTime.max:100}")
    private long EXECUTION_TIME; // ms 단위

    @Around("com.stool.studentcooperationtools.aop.aspect.PointCuts.allClass()")
    public Object doCheckExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable{
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long totalTime = System.currentTimeMillis() - startTime;
        if(totalTime > EXECUTION_TIME){
            log.warn("{} Exceeded time = {}ms", joinPoint.getSignature().toShortString(), totalTime);
        }
        return result;
    }
}
