package com.stool.studentcooperationtools.aop.LogTrace.Trace;

public class TraceStatus {

    private TraceId traceId;
    private Long startTimeMs;
    private String message;

    public TraceStatus(final TraceId traceId, final Long startTimeMs, final String message) {
        this.traceId = traceId;
        this.startTimeMs = startTimeMs;
        this.message = message;
    }

    public Long getTotalTime(final Long endTime){
        return endTime - this.startTimeMs;
    }

    public String getMessage() {
        return this.message;
    }
}
