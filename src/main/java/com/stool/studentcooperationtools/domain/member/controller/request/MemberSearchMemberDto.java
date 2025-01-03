package com.stool.studentcooperationtools.domain.member.controller.request;

import com.querydsl.core.annotations.QueryProjection;
import com.stool.studentcooperationtools.domain.member.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberSearchMemberDto {

    private String email;
    private String nickname;
    private String profile;
    private Long id;

    @Builder
    @QueryProjection
    public MemberSearchMemberDto(final String email, final String nickname, final String profile, final Long id) {
        this.email = email;
        this.nickname = nickname;
        this.profile = profile;
        this.id = id;
    }

    public static MemberSearchMemberDto of(Member member){
        return MemberSearchMemberDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickName())
                .profile(member.getProfile())
                .build();
    }

}
