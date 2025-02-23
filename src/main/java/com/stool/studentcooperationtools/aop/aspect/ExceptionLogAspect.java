package com.stool.studentcooperationtools.aop.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;

@Slf4j
@Aspect
public class ExceptionLogAspect {

    @AfterThrowing(value = "com.stool.studentcooperationtools.aop.aspect.PointCuts.allClass()",throwing = "ex")
    public void doCheckException(final JoinPoint joinPoint, final Exception ex){
        log.debug("{}, ex = {}, msg = {}",
                joinPoint.getSignature().toShortString(), ex.getClass().getSimpleName(),ex.getMessage());
    }

}
