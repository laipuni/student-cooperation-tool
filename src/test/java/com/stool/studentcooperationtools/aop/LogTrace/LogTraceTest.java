package com.stool.studentcooperationtools.aop.LogTrace;

import com.stool.studentcooperationtools.aop.LogTrace.Trace.TraceStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


class LogTraceTest {

    /*
    아래와 같이 로그가 찍힘
     hello1
     |--> hello2
     |  |--> hello3
     |  |<-- hello3 time = 0ms
     |<-- hello2 time = 0ms
      hello1 time = 6ms
     */
    @DisplayName("로그 트레이스 테스트")
    @Test
    void LogTest(){
        LogTrace logTrace = new ThreadLocalLogTrace();
        TraceStatus status1 = logTrace.begin("hello1");
        TraceStatus status2 = logTrace.begin("hello2");
        TraceStatus status3 = logTrace.begin("hello3");
        logTrace.end(status3);
        logTrace.end(status2);
        logTrace.end(status1);
    }
}