package com.stool.studentcooperationtools.aop.aspect;

import org.aspectj.lang.annotation.Pointcut;

public class PointCuts {
    @Pointcut("execution(* com.stool.studentcooperationtools.domain..*Service.*(..))")
    public void allService(){};

    //이름에 Controller가 붙은 class들
    @Pointcut("httpController() || websocketController()")
    public void allController(){};

    @Pointcut("execution(* com.stool.studentcooperationtools.domain..*Controller.*(..))")
    public void httpController(){};

    @Pointcut("execution(* com.stool.studentcooperationtools.websocket..*WebsocketController.*(..))")
    public void websocketController(){};

    //이름에 Repository가 붙은 class들
    @Pointcut("execution(* com.stool.studentcooperationtools.domain..*Repository.*(..))")
    public void allRepository(){};

    @Pointcut("allController() || allService() || allRepository()")
    public void allClass(){};

}
