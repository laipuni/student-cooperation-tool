package com.stool.studentcooperationtools.domain.member.controller.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class MemberFindResponse {

    private int num;
    private List<MemberFindMemberDto> members = new ArrayList<>();

    @Builder
    private MemberFindResponse(final int num, final List<MemberFindMemberDto> members) {
        this.num = num;
        this.members = members;
    }

    public static MemberFindResponse of(List<MemberFindMemberDto> members){
        return MemberFindResponse.builder()
                .num(members.size())
                .members(members)
                .build();
    }
}
