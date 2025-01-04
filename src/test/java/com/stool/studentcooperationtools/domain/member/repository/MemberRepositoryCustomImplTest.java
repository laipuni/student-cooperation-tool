package com.stool.studentcooperationtools.domain.member.repository;

import com.stool.studentcooperationtools.IntegrationTest;
import com.stool.studentcooperationtools.domain.friendship.Friendship;
import com.stool.studentcooperationtools.domain.friendship.repository.FriendshipRepository;
import com.stool.studentcooperationtools.domain.member.Member;
import com.stool.studentcooperationtools.domain.member.Role;
import com.stool.studentcooperationtools.domain.member.controller.request.MemberSearchMemberDto;
import com.stool.studentcooperationtools.domain.member.controller.response.MemberFindMemberDto;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@Transactional
class MemberRepositoryCustomImplTest extends IntegrationTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    FriendshipRepository friendshipRepository;

    @Test
    @DisplayName("유저 id로 친구 목록 조회")
    void findFriendsByMemberId() {
        //given
        Member memberA = Member.builder()
                .email("emailA")
                .profile("profileA")
                .nickName("nickA")
                .role(Role.USER)
                .build();

        Member memberB = Member.builder()
                .email("emailB")
                .profile("profileB")
                .nickName("nickB")
                .role(Role.USER)
                .build();
        memberRepository.saveAll(List.of(memberA, memberB));
        friendshipRepository.save(Friendship.of(memberA, memberB));
        //when
        List<MemberFindMemberDto> friendList = memberRepository.findFriendsByMemberId(memberA.getId());
        //then

        assertThat(friendList).hasSize(1)
                .extracting("nickname","profile")
                .containsExactlyInAnyOrder(
                        Tuple.tuple(memberB.getNickName(),memberB.getProfile())
                );

    }

    @Test
    @DisplayName("검색할 이름과 같고, 친구관계인 유저들을 조회한다.")
    void findFriendsByFriend() {
        //given
        boolean isFriendRelation = true;
        Member me = Member.builder()
                .email("emailA")
                .profile("profileA")
                .nickName("nickA")
                .role(Role.USER)
                .build();

        Member friend = Member.builder()
                .email("emailB")
                .profile("profileB")
                .nickName("nickB")
                .role(Role.USER)
                .build();
        memberRepository.saveAll(List.of(me, friend));
        friendshipRepository.save(
                Friendship.builder()
                        .me(me)
                        .friend(friend)
                        .build()
        );
        //when
        List<MemberSearchMemberDto> friendList = memberRepository.findFriendsByMemberNickName(
                friend.getNickName(),
                me.getId(),
                isFriendRelation
        );
        //then
        assertThat(friendList).hasSize(1)
                .extracting("email","nickname","profile")
                .containsExactly(
                        tuple(friend.getEmail(), friend.getNickName(), friend.getProfile())
                );
    }

    @Test
    @DisplayName("유효한 닉네임으로 해당 친구가 있는 유저의 친구가 아닌 목록 조회")
    void findFriendsByNotFriend() {
        //given
        boolean isFriendRelation = false;
        Member me = Member.builder()
                .email("emailA")
                .profile("profileA")
                .nickName("nickA")
                .role(Role.USER)
                .build();

        Member notFriendUser = Member.builder()
                .email("emailB")
                .profile("profileB")
                .nickName("nickB")
                .role(Role.USER)
                .build();
        memberRepository.saveAll(List.of(me, notFriendUser));
        //when
        List<MemberSearchMemberDto> friendList = memberRepository.findFriendsByMemberNickName(
                notFriendUser.getNickName(),
                me.getId(),
                isFriendRelation
        );
        //then
        assertThat(friendList).hasSize(1)
                .extracting("email","nickname","profile")
                .containsExactly(
                        tuple(notFriendUser.getEmail(), notFriendUser.getNickName(), notFriendUser.getProfile())
                );
    }

}