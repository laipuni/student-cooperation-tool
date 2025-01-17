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

import static com.stool.studentcooperationtools.domain.PagingUtils.ROOM_PAGING_PARSE;
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
    @DisplayName("참여중인 방을 페이지당 5개씩 조회할 때, 첫번째 페이지를 조회한다.")
    void searchRoomByNotExistTitleAndExistParticipation() {
        //given
        Member member = Member.builder()
                .role(Role.USER)
                .email("email@.email.com")
                .profile("profile")
                .nickName("nickname")
                .build();
        memberRepository.save(member);

        List<String> titleList = List.of("title1","title2","title3","title4","title5","title6");

        for (String title : titleList) {
            // 6개의 room과 participation을 등록 함
            Room room = createRoom(title, member);
            Participation participation = Participation.builder()
                    .member(member)
                    .room(room)
                    .build();
            roomRepository.save(room);
            participationRepository.save(participation);
        }

        //when
        RoomSearchResponse result = roomRepository.searchRoomsBy("", 0, true, member.getId());
        //then
        assertThat(result).extracting("num","totalPage","firstPage","lastPage")
                        .containsExactlyInAnyOrder(ROOM_PAGING_PARSE,2,1,2);
        assertThat(result.getRooms()).hasSize(5);
    }

    @Test
    @DisplayName("참여하지 않는 방을 검색할 때, 검색어와 같은 방의 첫 페이지를 조회하고 다음 페이지가 존재한다.")
    void searchRoomByExistTitleAndExistParticipation() {
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
        RoomSearchResponse result = roomRepository.searchRoomsBy(
                searchTitle, 0,
                true, member.getId()
        );
        //then
        assertThat(result).extracting("num","totalPage","firstPage","lastPage")
                .containsExactlyInAnyOrder(ROOM_PAGING_PARSE,2,1,2);
        assertThat(result.getRooms()).hasSize(5);
    }

    @Test
    @DisplayName("참여한 방을 검색할 때, 검색어와 같은 방의 첫 페이지를 조회하고 다음 페이지가 존재한다.")
    void searchRoomByExistTitleAndNotExistParticipation() {
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
        RoomSearchResponse result = roomRepository.searchRoomsBy(
                searchTitle, 0,
                false, member.getId()
        );
        //then
        assertThat(result).extracting("num","totalPage","firstPage","lastPage")
                .containsExactlyInAnyOrder(ROOM_PAGING_PARSE,2,1,2);
        assertThat(result.getRooms()).hasSize(5);
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