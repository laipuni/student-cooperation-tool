package com.stool.studentcooperationtools.domain.member.service;

import com.stool.studentcooperationtools.IntegrationTest;
import com.stool.studentcooperationtools.domain.friendship.Friendship;
import com.stool.studentcooperationtools.domain.friendship.repository.FriendshipRepository;
import com.stool.studentcooperationtools.domain.member.Member;
import com.stool.studentcooperationtools.domain.member.Role;
import com.stool.studentcooperationtools.domain.member.controller.request.MemberAddRequest;
import com.stool.studentcooperationtools.domain.member.controller.response.MemberFindResponse;
import com.stool.studentcooperationtools.domain.member.controller.response.MemberSearchResponse;
import com.stool.studentcooperationtools.domain.member.repository.MemberRepository;
import com.stool.studentcooperationtools.security.oauth2.dto.SessionMember;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
class MemberServiceTest extends IntegrationTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    FriendshipRepository friendshipRepository;
    @Autowired
    MemberService memberService;

    @Test
    @DisplayName("사용자의 id로 친구 목록 조회")
    void findFriendsByMemberId() {
        //given
        Member user = Member.builder()
                .profile("profile")
                .email("email")
                .nickName("nickName")
                .role(Role.USER)
                .build();
        Member memberA = Member.builder()
                .profile("A")
                .email("A")
                .nickName("A")
                .role(Role.USER)
                .build();
        memberRepository.saveAll(List.of(user, memberA));
        friendshipRepository.save(Friendship.of(user, memberA));
        SessionMember sessionMember = SessionMember.of(user);
        //when
        MemberFindResponse response = memberService.findFriends(sessionMember);
        //then
        assertThat(response.getNum()).isGreaterThan(0);
    }

    @Test
    @DisplayName("유효한 닉네임으로 해당 친구가 있는 유저의 친구 목록 조회")
    void searchFriendWithValidFriendNickName() {
        //given
        Member user = Member.builder()
                .profile("profile")
                .email("email")
                .nickName("nickName")
                .role(Role.USER)
                .build();
        Member memberA = Member.builder()
                .profile("A")
                .email("A")
                .nickName("A")
                .role(Role.USER)
                .build();
        memberRepository.saveAll(List.of(user, memberA));
        friendshipRepository.save(Friendship.of(user, memberA));
        SessionMember sessionMember = SessionMember.of(user);
        //when
        MemberSearchResponse response = memberService.searchFriend(sessionMember, true, memberA.getNickName());
        //then
        assertThat(response.getNum()).isGreaterThan(0);
    }

    @Test
    @DisplayName("유효한 닉네임으로 해당 친구가 없는 유저의 친구 목록 조회")
    void searchFriendWithValidMemberNickName() {
        //given
        Member user = Member.builder()
                .profile("profile")
                .email("email")
                .nickName("nickName")
                .role(Role.USER)
                .build();
        Member memberA = Member.builder()
                .profile("A")
                .email("A")
                .nickName("A")
                .role(Role.USER)
                .build();
        memberRepository.saveAll(List.of(user, memberA));
        SessionMember sessionMember = SessionMember.of(user);
        //when
        MemberSearchResponse response = memberService.searchFriend(sessionMember, true, memberA.getNickName());
        //then
        assertThat(response.getNum()).isEqualTo(0);
    }

    @Test
    @DisplayName("유효하지 않은 닉네임으로 친구 목록 조회")
    void searchFriendWithInvalidMemberNickName() {
        //given
        String invalidNickName = "invalidNickName";
        Member user = Member.builder()
                .profile("profile")
                .email("email")
                .nickName("nickName")
                .role(Role.USER)
                .build();
        Member memberA = Member.builder()
                .profile("A")
                .email("A")
                .nickName("A")
                .role(Role.USER)
                .build();
        memberRepository.saveAll(List.of(user, memberA));
        friendshipRepository.save(Friendship.of(user, memberA));
        SessionMember sessionMember = SessionMember.of(user);
        //when
        MemberSearchResponse response = memberService.searchFriend(sessionMember, true, invalidNickName);
        //then
        assertThat(response.getNum()).isEqualTo(0);
    }

    @Test
    @DisplayName("유효한 닉네임으로 해당 친구가 없는 유저의 친구가 아닌 목록 조회")
    void searchNonFriendWithValidMemberNickName() {
        //given
        Member user = Member.builder()
                .profile("profile")
                .email("email")
                .nickName("nickName")
                .role(Role.USER)
                .build();
        Member memberA = Member.builder()
                .profile("A")
                .email("A")
                .nickName("A")
                .role(Role.USER)
                .build();
        memberRepository.saveAll(List.of(user, memberA));
        SessionMember sessionMember = SessionMember.of(user);
        //when
        MemberSearchResponse response = memberService.searchFriend(sessionMember, false, memberA.getNickName());
        //then
        assertThat(response.getNum()).isGreaterThan(0);
    }

    @Test
    @DisplayName("유효한 닉네임으로 해당 친구가 있는 유저의 친구가 아닌 목록 조회")
    void searchNonFriendWithValidFriendNickName() {
        //given
        Member user = Member.builder()
                .email("email")
                .profile("profile")
                .nickName("nickName")
                .role(Role.USER)
                .build();

        Member memberA = Member.builder()
                .email("emailA")
                .profile("profileA")
                .nickName("nickA")
                .role(Role.USER)
                .build();
        memberRepository.saveAll(List.of(user, memberA));
        friendshipRepository.save(Friendship.of(user, memberA));
        SessionMember sessionMember = SessionMember.of(user);
        //when
        MemberSearchResponse response = memberService.searchFriend(sessionMember, false, memberA.getNickName());
        //then
        assertThat(response.getNum()).isEqualTo(0);
    }

    @Test
    @DisplayName("유효하지 않은 닉네임으로 유저의 친구가 아닌 목록 조회")
    void searchNonFriendWithInValidMemberNickName() {
        //given
        String invalidNickName = "invalidNickName";
        Member user = Member.builder()
                .profile("profile")
                .email("email")
                .nickName("nickName")
                .role(Role.USER)
                .build();
        Member memberA = Member.builder()
                .profile("A")
                .email("A")
                .nickName("A")
                .role(Role.USER)
                .build();
        memberRepository.saveAll(List.of(user, memberA));
        SessionMember sessionMember = SessionMember.of(user);
        //when
        MemberSearchResponse response = memberService.searchFriend(sessionMember, false, invalidNickName);
        //then
        assertThat(response.getNum()).isEqualTo(0);
    }

    @Test
    @DisplayName("유효한 이메일으로 친구 등록")
    void addFriendWithValidEmail() {
        //given
        Member user = Member.builder()
                .profile("profile")
                .email("email")
                .nickName("nickName")
                .role(Role.USER)
                .build();
        Member memberA = Member.builder()
                .profile("A")
                .email("A")
                .nickName("A")
                .role(Role.USER)
                .build();
        memberRepository.saveAll(List.of(user, memberA));
        SessionMember sessionMember = SessionMember.of(user);
        MemberAddRequest request = MemberAddRequest.builder()
                .friendId(memberA.getId())
                .build();
        //when
        //then
        assertThat(memberService.addFriend(sessionMember, request).booleanValue()).isTrue();
    }

    @Test
    @DisplayName("유효하지 않은 이메일으로 친구 등록")
    void addFriendWithInValidEmail() {
        //given
        Long invalidId = 2025L;
        Member user = Member.builder()
                .profile("profile")
                .email("email")
                .nickName("nickName")
                .role(Role.USER)
                .build();
        Member memberA = Member.builder()
                .profile("A")
                .email("A")
                .nickName("A")
                .role(Role.USER)
                .build();
        memberRepository.saveAll(List.of(user, memberA));
        SessionMember sessionMember = SessionMember.of(user);
        MemberAddRequest request = MemberAddRequest.builder()
                .friendId(invalidId)
                .build();
        //when
        //then
        assertThrows(IllegalArgumentException.class, () -> memberService.addFriend(sessionMember, request));
    }
}