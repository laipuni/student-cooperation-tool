package com.stool.studentcooperationtools.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stool.studentcooperationtools.web.filter.wrapper.CachedBodyHttpServletRequest;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class RequestCachingFilter implements Filter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpServletRequest) {
            CachedBodyHttpServletRequest wrapper = new CachedBodyHttpServletRequest(httpServletRequest);
            String url = wrapper.getRequestURI();
            String param = wrapper.getParams(objectMapper);
            String method = wrapper.getMethod();
            String body = wrapper.getBody();
            log.trace("[Http Request] method = {}, url = {}, param = {}, body = {}", method, url, param, body);
            chain.doFilter(wrapper, response);
        } else {
            chain.doFilter(request, response);
        }
    }

}


