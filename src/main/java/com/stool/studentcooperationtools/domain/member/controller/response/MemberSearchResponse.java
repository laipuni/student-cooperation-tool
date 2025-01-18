package com.stool.studentcooperationtools.domain.member.controller.response;

import com.stool.studentcooperationtools.domain.member.controller.request.MemberSearchMemberDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class MemberSearchResponse {

    private int num;
    private List<MemberSearchMemberDto> members = new ArrayList<>();

    @Builder
    private MemberSearchResponse(final int num, final List<MemberSearchMemberDto> members) {
        this.num = num;
        this.members = members;
    }

    public static MemberSearchResponse of(List<MemberSearchMemberDto> members){
        return MemberSearchResponse.builder()
                .num(members.size())
                .members(members)
                .build();
    }
}
