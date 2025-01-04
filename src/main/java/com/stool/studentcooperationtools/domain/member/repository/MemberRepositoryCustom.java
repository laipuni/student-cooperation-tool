package com.stool.studentcooperationtools.domain.member.repository;

import com.stool.studentcooperationtools.domain.member.controller.request.MemberSearchMemberDto;
import com.stool.studentcooperationtools.domain.member.controller.response.MemberFindMemberDto;

import java.util.List;

public interface MemberRepositoryCustom {

    List<MemberFindMemberDto> findFriendsByMemberId(final Long memberId);
    List<MemberSearchMemberDto> findFriendsByMemberNickName(final String nickName, final Long memberId, final boolean relation);

}
