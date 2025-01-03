package com.stool.studentcooperationtools.domain.member.repository;

import com.stool.studentcooperationtools.domain.member.controller.request.MemberSearchMemberDto;

import java.util.List;

public interface MemberRepositoryCustom {

    List<MemberSearchMemberDto> findFriendsByMemberNickName(final String nickName, final Long memberId, final boolean relation);

}
