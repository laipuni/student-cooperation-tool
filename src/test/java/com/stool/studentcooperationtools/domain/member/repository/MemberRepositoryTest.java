package com.stool.studentcooperationtools.domain.member.repository;

import com.stool.studentcooperationtools.IntegrationTest;
import com.stool.studentcooperationtools.domain.friendship.repository.FriendshipRepository;
import com.stool.studentcooperationtools.domain.member.Member;
import com.stool.studentcooperationtools.domain.member.Role;
import com.stool.studentcooperationtools.domain.participation.Participation;
import com.stool.studentcooperationtools.domain.participation.repository.ParticipationRepository;
import com.stool.studentcooperationtools.domain.room.Room;
import com.stool.studentcooperationtools.domain.room.repository.RoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
@Transactional
class MemberRepositoryTest extends IntegrationTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    FriendshipRepository friendshipRepository;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    ParticipationRepository participationRepository;

    @Test
    @DisplayName("유효한 이메일으로 유저 조회")
    void findMemberByValidEmail() {
        //given
        Member memberA = Member.builder()
                .email("emailA")
                .profile("profileA")
                .nickName("nickA")
                .role(Role.USER)
                .build();
        memberRepository.save(memberA);
        //when
        Optional<Member> memberByEmail = memberRepository.findMemberByEmail(memberA.getEmail());
        //then
        assertThat(memberByEmail.isPresent()).isTrue();
    }

    @Test
    @DisplayName("유효하지 않은 이메일으로 유저 조회")
    void findMemberByInvalidEmail() {
        //given
        String invalidEmail = "invalidEmail";
        Member memberA = Member.builder()
                .email("emailA")
                .profile("profileA")
                .nickName("nickA")
                .role(Role.USER)
                .build();
        memberRepository.save(memberA);
        //when
        //then
        assertThrows(IllegalArgumentException.class,
                () -> memberRepository.findMemberByEmail(invalidEmail)
                        .orElseThrow(IllegalArgumentException::new));
    }

    @Test
    @DisplayName("회원 id들이 담긴 리스트가 주어졌을 때 해당 회원들 조회")
    void findMembersByMemberIdList() {
        //given
        Member memberA = Member.builder()
                .email("emailA")
                .profile("profileA")
                .nickName("nickA")
                .role(Role.USER)
                .build();
        memberRepository.save(memberA);
        Member memberB = Member.builder()
                .email("emailB")
                .profile("profileB")
                .nickName("nickB")
                .role(Role.USER)
                .build();
        memberRepository.save(memberB);
        //when
        List<Member> members = memberRepository.findMembersByMemberIdList(List.of(memberA.getId(), memberB.getId()));
        //then
        assertThat(members.size()).isEqualTo(2);
    }

    @DisplayName("해당 방에 속하는 유저들을 조회한다.")
    @Test
    void findAllByRoomId(){
        //given
        String emailA = "emailA";
        String profileA = "profileA";
        String nickA = "nickA";
        Member member = Member.builder()
                .email(emailA)
                .profile(profileA)
                .nickName(nickA)
                .role(Role.USER)
                .build();
        memberRepository.save(member);

        Room room = Room.builder()
                .password("password")
                .title("방제목")
                .leader(member)
                .participationNum(0)
                .build();

        roomRepository.save(room);

        Participation participation = Participation.builder()
                .member(member)
                .room(room)
                .build();
        participationRepository.save(participation);

        //when
        List<Member> members = memberRepository.findAllByRoomId(room.getId());

        //then
        assertThat(members).hasSize(1)
                .extracting("email","profile","nickName")
                .containsExactlyInAnyOrder(tuple(emailA,profileA,nickA));
    }
}