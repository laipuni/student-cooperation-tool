package com.stool.studentcooperationtools.domain.member.service;

import com.stool.studentcooperationtools.domain.friendship.Friendship;
import com.stool.studentcooperationtools.domain.friendship.repository.FriendshipRepository;
import com.stool.studentcooperationtools.domain.member.Member;
import com.stool.studentcooperationtools.domain.member.controller.request.MemberSearchMemberDto;
import com.stool.studentcooperationtools.domain.member.controller.response.MemberFindMemberDto;
import com.stool.studentcooperationtools.domain.member.controller.request.FriendRemoveRequest;
import com.stool.studentcooperationtools.domain.member.repository.MemberRepository;
import com.stool.studentcooperationtools.domain.member.controller.request.MemberAddRequest;
import com.stool.studentcooperationtools.domain.member.controller.response.MemberFindResponse;
import com.stool.studentcooperationtools.domain.member.controller.response.MemberSearchResponse;
import com.stool.studentcooperationtools.security.oauth2.dto.SessionMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final FriendshipRepository friendshipRepository;

    //유저의 친구 목록 조회
    public MemberFindResponse findFriends(SessionMember member) {
        List<MemberFindMemberDto> friends = memberRepository.findFriendsByMemberId(member.getMemberSeq());
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

    @Transactional
    public Boolean addFriend(SessionMember member, final MemberAddRequest request) {
        Member user = memberRepository.findById(member.getMemberSeq())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저는 존재하지 않습니다."));
        Member friend = memberRepository.findById(request.getFriendId())
                .orElseThrow(() -> new IllegalArgumentException("친구 추가할 유저가 존재하지 않습니다."));
        friendshipRepository.save(Friendship.of(user, friend));
        log.info("사용자(ID: {})가 친구(ID: {})를 추가했다.", user.getId(), friend.getId());
        return true;
    }

    @Transactional
    public Boolean removeFriend(SessionMember member, final FriendRemoveRequest request){
        Member user = memberRepository.findById(member.getMemberSeq())
                .orElseThrow(()->new IllegalArgumentException("해당 유저는 존재하지 않습니다."));
        Member friend = memberRepository.findMemberByEmail(request.getEmail())
                .orElseThrow(()->new IllegalArgumentException("친구 추가할 유저가 존재하지 않습니다."));
        friendshipRepository.deleteByFriendId(user.getId(), friend.getId());
        log.info("사용자(ID: {})가 친구(ID: {})를 제거했다.", user.getId(), friend.getId());
        return true;
    }
}
