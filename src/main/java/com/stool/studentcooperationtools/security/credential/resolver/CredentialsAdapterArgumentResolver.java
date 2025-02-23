package com.stool.studentcooperationtools.security.credential.resolver;

import com.google.auth.http.HttpCredentialsAdapter;
import com.google.common.base.VerifyException;
import com.stool.studentcooperationtools.security.credential.GoogleCredentialProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class CredentialsAdapterArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private final GoogleCredentialProvider googleCredentialProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(HttpCredentialsAdapter.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpCredentialsAdapter credentialsAdapter = googleCredentialProvider.getCredentialsAdapter();
        if (credentialsAdapter == null) {
            throw new VerifyException("credential adapter가 생성되지 않았습니다");
        }
        return credentialsAdapter;
    }
}