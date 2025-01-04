package com.stool.studentcooperationtools.domain.member.controller.response;


import com.querydsl.core.annotations.QueryProjection;
import com.stool.studentcooperationtools.domain.member.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberFindMemberDto {

    private String nickname;
    private String profile;
    private Long id;

    @Builder
    @QueryProjection
    public MemberFindMemberDto(final String nickname, final String profile, final Long id) {
        this.nickname = nickname;
        this.profile = profile;
        this.id = id;
    }

    public static MemberFindMemberDto of(Member member){
        return MemberFindMemberDto.builder()
                .id(member.getId())
                .nickname(member.getNickName())
                .profile(member.getProfile())
                .build();
    }
}
