package com.stool.studentcooperationtools.security.oauth2;

import com.stool.studentcooperationtools.domain.member.Member;
import com.stool.studentcooperationtools.domain.member.repository.MemberRepository;
import com.stool.studentcooperationtools.security.credential.GoogleCredentialProvider;
import com.stool.studentcooperationtools.security.oauth2.dto.OAuthAttributes;
import com.stool.studentcooperationtools.security.oauth2.dto.SessionMember;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService  {

    public static final String SESSION_MEMBER_NAME = "member";

    private final MemberRepository memberRepository;
    private final HttpSession httpSession;
    private final GoogleCredentialProvider googleCredentialProvider;

    @Override
    public OAuth2User loadUser(final OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String attributeName = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();
        String accessToken = userRequest.getAccessToken().getTokenValue();
        initializeCredential(accessToken);

        OAuthAttributes attributes = OAuthAttributes.of(
                registrationId,
                attributeName,
                oAuth2User.getAttributes()
        );

        Member member = saveOrUpdate(attributes);

        httpSession.setAttribute("member",SessionMember.of(member));

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(member.getRole())),
                attributes.getAttributes(),
                attributes.getAttributesNameKey()
        );
    }

    private void initializeCredential(final String accessToken) {
        try {
            googleCredentialProvider.initializeCredential(accessToken);
        }
        catch(IOException e){
            throw new OAuth2AuthenticationException(e.getMessage());
        }
    }

    private Member saveOrUpdate(final OAuthAttributes attributes) {
        Member member = memberRepository.findMemberByEmail(attributes.getEmail())
                .map(findMember -> findMember.update(
                        attributes.getUserName(), attributes.getEmail(), attributes.getPicture()
                ))
                .orElse(attributes.toEntity());

        return memberRepository.save(member);
    }
}
