package com.stool.studentcooperationtools.aop.LogTrace;

import com.stool.studentcooperationtools.aop.LogTrace.Trace.TraceId;
import com.stool.studentcooperationtools.aop.LogTrace.Trace.TraceStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadLocalLogTrace implements LogTrace{

    private static final String START_PREFIX = "-->";
    private static final String COMPLETE_PREFIX = "<--";
    private static final String EX_PREFIX = "<X-";


    private ThreadLocal<TraceId> threadLocal = new ThreadLocal<>();

    @Override
    public TraceStatus begin(final String message) {
        syncTraceId();
        TraceId traceId = threadLocal.get();
        long startTime = System.currentTimeMillis();
        TraceStatus traceStatus = new TraceStatus(traceId,startTime,message);
        log.info("[{}] {} {}",traceId.getId(),addStep(START_PREFIX,traceId.getLevel()), message);
        return traceStatus;
    }

    private void syncTraceId(){
        TraceId traceId = threadLocal.get();
        if(traceId == null){
           threadLocal.set(new TraceId());
        } else {
            threadLocal.set(traceId.createNextTraceId());
        }
    }

    @Override
    public void end(final TraceStatus status) {
        complete(status,null);
    }


    @Override
    public void exception(final TraceStatus status, final Exception e) {
        complete(status,e);
    }

    private void complete(final TraceStatus status, final Exception e){
        Long totalTime = status.getTotalTime(System.currentTimeMillis());
        TraceId traceId = threadLocal.get();
        String message = status.getMessage();
        if(e == null){
            log.info("[{}] {} {} time = {}ms", traceId.getId(),
                    addStep(COMPLETE_PREFIX,traceId.getLevel()), message, totalTime);
        } else{
            log.info("[{}] {} {} time = {}ms ex = {}", traceId.getId(),
                    addStep(EX_PREFIX,traceId.getLevel()), message, totalTime, e.getMessage());
        }
        releaseTraceId();
    }

    private void releaseTraceId(){
        TraceId traceId = threadLocal.get();
        if(traceId.isFirstLevel()){
            threadLocal.remove();
        } else{
            threadLocal.set(traceId.createPrevTraceId());
        }
    }

    public String addStep(final String prefix, final int level){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append( (i == level - 1) ? "|" + prefix : "| ");
        }
        return sb.toString();
    }
}
