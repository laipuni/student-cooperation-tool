package com.stool.studentcooperationtools.aop.LogTrace.Trace;

import java.util.UUID;

public class TraceId {

    private String id;
    private int level;

    public TraceId() {
        this.id = createId();
        this.level = 0;
    }

    private TraceId(final String id, final int level) {
        this.id = id;
        this.level = level;
    }

    public String getId(){
        return this.id;
    }

    public static String createId(){
        return UUID.randomUUID().toString().substring(0,10);
    }

    public boolean isFirstLevel(){
        return level == 0;
    }

    public TraceId createNextTraceId(){
        return new TraceId(this.id, this.level + 1);
    }

    public int getLevel() {
        return this.level;
    }

    public TraceId createPrevTraceId() {
        return new TraceId(this.id, level - 1);
    }
}
