package com.stool.studentcooperationtools.domain.member.service;

import com.stool.studentcooperationtools.domain.friendship.Friendship;
import com.stool.studentcooperationtools.domain.friendship.repository.FriendshipRepository;
import com.stool.studentcooperationtools.domain.member.Member;
import com.stool.studentcooperationtools.domain.member.controller.request.MemberSearchMemberDto;
import com.stool.studentcooperationtools.domain.member.repository.MemberRepository;
import com.stool.studentcooperationtools.domain.member.controller.request.MemberAddRequest;
import com.stool.studentcooperationtools.domain.member.controller.response.MemberFindResponse;
import com.stool.studentcooperationtools.domain.member.controller.response.MemberSearchResponse;
import com.stool.studentcooperationtools.security.oauth2.dto.SessionMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final FriendshipRepository friendshipRepository;

    //유저의 친구 목록 조회
    public MemberFindResponse findFriends(SessionMember member) {
        List<Member> friends = memberRepository.findFriendsByMemberId(member.getMemberSeq());
        return MemberFindResponse.of(friends);
    }

    //isFriendRelation = true, 친구 관계인 유저중에 nickName값과 비슷한 유저를 검색
    //isFriendRelation = false, 친구 관계가 아닌 유저 중에 nickName값과 비슷한 유저를 검색
    public MemberSearchResponse searchFriend(SessionMember member, final boolean isFriendRelation, final String nickName) {
        //친구인 유저를 검색하는 경우
        List<MemberSearchMemberDto> findFriends = memberRepository.findFriendsByMemberNickName(
                nickName.strip(),
                member.getMemberSeq(),
                isFriendRelation
        );
        return MemberSearchResponse.of(findFriends);
    }

    //친구추가 - 1. 검색한 email을 가진 멤버 조회 2. friendship 엔티티 만들어서 등록
    @Transactional
    public Boolean addFriend(SessionMember member, final MemberAddRequest request) {
        Member user = memberRepository.findById(member.getMemberSeq())
                .orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));
        String findEmail = request.getEmail();
        Member friend = memberRepository.findMemberByEmail(findEmail)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 유저가 존재하지 않습니다."));
        friendshipRepository.save(Friendship.of(user, friend));
        return true;
    }
}
