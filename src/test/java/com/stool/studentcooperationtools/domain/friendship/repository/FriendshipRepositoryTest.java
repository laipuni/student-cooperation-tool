package com.stool.studentcooperationtools.domain.friendship.repository;

import com.stool.studentcooperationtools.IntegrationTest;
import com.stool.studentcooperationtools.domain.friendship.Friendship;
import com.stool.studentcooperationtools.domain.member.Member;
import com.stool.studentcooperationtools.domain.member.Role;
import com.stool.studentcooperationtools.domain.member.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
class FriendshipRepositoryTest extends IntegrationTest{

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    FriendshipRepository friendshipRepository;

    @Test
    @DisplayName("친구정보 삭제")
    void deleteByFriendId() {
        //given
        Member user = Member.builder()
                .profile("profile")
                .email("email")
                .nickName("nickName")
                .role(Role.USER)
                .build();
        memberRepository.save(user);
        Member friend = Member.builder()
                .profile("A")
                .email("A")
                .nickName("A")
                .role(Role.USER)
                .build();
        memberRepository.save(friend);
        friendshipRepository.save(Friendship.of(user, friend));
        //when
        friendshipRepository.deleteByFriendId(user.getId(), friend.getId());
        //then
        assertThat(friendshipRepository.findAll()).isEmpty();
    }
}
