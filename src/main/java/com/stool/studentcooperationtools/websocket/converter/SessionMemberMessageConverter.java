package com.stool.studentcooperationtools.websocket.converter;

import com.stool.studentcooperationtools.domain.member.Member;
import com.stool.studentcooperationtools.domain.member.repository.MemberRepository;
import com.stool.studentcooperationtools.exception.global.LoginRequiredException;
import com.stool.studentcooperationtools.security.oauth2.dto.SessionMember;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.AbstractMessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SessionMemberMessageConverter extends AbstractMessageConverter {

    private final MemberRepository memberRepository;

    @Override
    protected boolean supports(final Class<?> clazz) {
        return SessionMember.class.isAssignableFrom(clazz);
    }

    @Override
    protected Object convertFromInternal(final Message<?> message, final Class<?> targetClass, final Object conversionHint) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        OAuth2AuthenticationToken authentication = getOAuth2Authentication(accessor);
        OAuth2User principal = authentication.getPrincipal();
        //DefaultOAuth2User에 저장된 이메일로 유저의 정보를 조회
        //유저가 존재하지 않는 경우, 유저가 존재하지 않다는 예외가 발생한다.
        Member member = memberRepository.findMemberByEmail(principal.getAttribute("email"))
                .orElseThrow(() -> new IllegalArgumentException("해당 유저는 존재하지 않습니다."));
        return SessionMember.of(member);
    }


    private static OAuth2AuthenticationToken getOAuth2Authentication(final StompHeaderAccessor accessor) {
        return Optional.ofNullable((OAuth2AuthenticationToken) accessor.getHeader("simpUser"))
                .orElseThrow(() -> new LoginRequiredException("로그인이 필요합니다."));
    }
}
