package com.stool.studentcooperationtools.aop.aspect;

import com.stool.studentcooperationtools.aop.LogTrace.LogTrace;
import com.stool.studentcooperationtools.aop.LogTrace.Trace.TraceStatus;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Slf4j
@Aspect
public class LogTraceAspect {

    private final LogTrace logTrace;

    public LogTraceAspect(final LogTrace logTrace) {
        this.logTrace = logTrace;
    }

    @Pointcut("execution(* com.stool.studentcooperationtools..*(..))")
    private void basePackage(){};

    //이름에 Service가 붙은 class들
    @Pointcut("execution(* *..*Service.*(..))")
    private void allService(){};

    //이름에 Controller가 붙은 class들
    @Pointcut("execution(* *..*Controller.*(..))")
    private void allController(){};

    //이름에 Repository가 붙은 class들
    @Pointcut("execution(* *..*Repository.*(..))")
    private void allRepository(){};

    @Pointcut("allController() || allService() || allRepository()")
    private void allClass(){};

    @Around("basePackage() && allClass()")
    public Object execute(final ProceedingJoinPoint joinPoint) throws Throwable{
        TraceStatus status = null;
        String message = joinPoint.getSignature().toShortString();
        try{
            status = logTrace.begin(message);
            Object result = joinPoint.proceed();
            logTrace.end(status);
            return result;
        } catch (Exception e){
            logTrace.exception(status,e);
            throw e;
        }
    }

}
