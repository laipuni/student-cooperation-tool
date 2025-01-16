package com.stool.studentcooperationtools.domain.room.repository;

import com.stool.studentcooperationtools.IntegrationTest;
import com.stool.studentcooperationtools.domain.member.Member;
import com.stool.studentcooperationtools.domain.member.Role;
import com.stool.studentcooperationtools.domain.member.repository.MemberRepository;
import com.stool.studentcooperationtools.domain.participation.Participation;
import com.stool.studentcooperationtools.domain.participation.repository.ParticipationRepository;
import com.stool.studentcooperationtools.domain.room.Room;
import com.stool.studentcooperationtools.domain.room.controller.response.RoomSearchResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class RoomRepositoryCustomImplTest extends IntegrationTest {

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ParticipationRepository participationRepository;

    @Test
    @DisplayName("참여한 방을 검색할 때, 검색어와 같은 방의 첫 페이지를 조회하고 다음 페이지가 존재한다.")
    void findRoomsWithParticipation() {
        //given
        Member member = Member.builder()
                .role(Role.USER)
                .email("email@.email.com")
                .profile("profile")
                .nickName("nickname")
                .build();
        memberRepository.save(member);

        String searchTitle = "title";
        List<String> titleList = List.of("title1","title2","title3","title4","title5","title6");

        for (String title : titleList) {
            Room room = createRoom(title, member);
            Participation participation = Participation.builder()
                    .member(member)
                    .room(room)
                    .build();
            roomRepository.save(room);
            participationRepository.save(participation);
        }


        //when
        RoomSearchResponse result = roomRepository.findRooms(
                searchTitle, 0,
                true, member.getId()
        );
        //then
        assertThat(result.isLast()).isFalse();
        assertThat(result.getNum()).isEqualTo(titleList.size());
        assertThat(result.getRooms()).hasSize(titleList.size());
    }

    @Test
    @DisplayName("참여한 방을 검색할 때, 검색어와 같은 방의 첫 페이지를 조회하고 다음 페이지가 존재한다.")
    void findRoomsWithNotParticipation() {
        //given
        Member leader = Member.builder()
                .role(Role.USER)
                .email("leader@.email.com")
                .profile("leaderProfile")
                .nickName("leader")
                .build();
        Member member = Member.builder()
                .role(Role.USER)
                .email("email@.email.com")
                .profile("profile")
                .nickName("nickname")
                .build();
        memberRepository.saveAll(List.of(member,leader));

        String searchTitle = "title";
        List<String> titleList = List.of("title1","title2","title3","title4","title5","title6");

        for (String title : titleList) {
            Room room = createRoom(title, leader);
            Participation participation = Participation.builder()
                    .member(leader)
                    .room(room)
                    .build();
            roomRepository.save(room);
            participationRepository.save(participation);
        }

        //when
        RoomSearchResponse result = roomRepository.findRooms(
                searchTitle, 0,
                false, member.getId()
        );
        //then
        assertThat(result.isLast()).isFalse();
        assertThat(result.getNum()).isEqualTo(titleList.size());
        assertThat(result.getRooms()).hasSize(titleList.size());
    }

    private static Room createRoom(final String title,final Member member) {
        return Room.builder()
                .leader(member)
                .title(title)
                .participationNum(1)
                .password("password")
                .build();
    }

}