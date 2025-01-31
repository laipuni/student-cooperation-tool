package com.stool.studentcooperationtools.aop.LogTrace;

import com.stool.studentcooperationtools.aop.LogTrace.Trace.TraceStatus;

public interface LogTrace {

    TraceStatus begin(final String message);

    void end(final TraceStatus status);

    void exception(final TraceStatus status, final Exception e);

}
