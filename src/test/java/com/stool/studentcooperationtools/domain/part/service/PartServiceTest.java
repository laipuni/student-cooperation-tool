package com.stool.studentcooperationtools.domain.part.service;

import com.stool.studentcooperationtools.domain.member.Member;
import com.stool.studentcooperationtools.domain.member.Role;
import com.stool.studentcooperationtools.domain.member.repository.MemberRepository;
import com.stool.studentcooperationtools.domain.part.Part;
import com.stool.studentcooperationtools.domain.part.controller.response.PartFindResponse;
import com.stool.studentcooperationtools.domain.part.repository.PartRepository;
import com.stool.studentcooperationtools.domain.room.Room;
import com.stool.studentcooperationtools.domain.room.repository.RoomRepository;
import com.stool.studentcooperationtools.security.oauth2.dto.SessionMember;
import com.stool.studentcooperationtools.websocket.controller.part.request.PartAddWebsocketRequest;
import com.stool.studentcooperationtools.websocket.controller.part.response.PartAddWebsocketResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class PartServiceTest {

    @Autowired
    PartService partService;

    @Autowired
    PartRepository partRepository;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    MemberRepository memberRepository;


    @DisplayName("해당 방의 자료 역할을 조회한다.")
    @Test
    void findParts(){
        //given
        Member member = Member.builder()
                .email("email")
                .nickName("닉네임")
                .profile("profile")
                .role(Role.USER)
                .build();
        memberRepository.save(member);
        Room room = Room.builder()
                .password("password")
                .title("제목")
                .leader(member)
                .participationNum(1)
                .build();
        roomRepository.save(room);

        String content = "조사할 부분";
        Part part = Part.builder()
                .partName(content)
                .room(room)
                .member(member)
                .build();
        partRepository.save(part);
        //when
        PartFindResponse response = partService.findParts(room.getId());

        //then
        assertThat(response.getNum()).isEqualTo(1);
        assertThat(response.getParts()).hasSize(1)
                .extracting("partName")
                .containsExactly(content);
    }

    @DisplayName("역할을 추가할 때, 역할 추가 요청을 한 유저가 존재하지 않을 경우 에러가 발생한다.")
    @Test
    void addPartWithNotExistMember(){
        //given
        Long invalidRoomId = 1L;
        Long invalidMemberId = 1L;
        String invalidProfile = "프로필";
        String invalidNickname = "닉네임";
        String partName = "조사할 부분";
        PartAddWebsocketRequest request = PartAddWebsocketRequest.builder()
                .roomId(invalidRoomId)
                .partName(partName)
                .build();

        SessionMember sessionMember = SessionMember.builder()
                .memberSeq(invalidMemberId)
                .profile(invalidProfile)
                .nickName(invalidNickname)
                .build();

        //when
        //then
        assertThatThrownBy(() -> partService.addPart(request, sessionMember))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageMatching("역할을 추가하는 것을 실패했습니다.");
    }


    @DisplayName("역할을 추가할 때, 역할을 추가할 방이 존재하지 않는다면 에러가 발생한다.")
    @Test
    void addPartWithNotExistRoom(){
        //given
        Member member = Member.builder()
                .role(Role.USER)
                .email("email")
                .profile("profile")
                .nickName("nickname")
                .build();
        memberRepository.save(member);

        String partName = "조사할 부분";
        Long invalidRoomId = 1L;
        PartAddWebsocketRequest request = PartAddWebsocketRequest.builder()
                .roomId(invalidRoomId)
                .partName(partName)
                .build();

        SessionMember sessionMember = SessionMember.builder()
                .memberSeq(member.getId())
                .profile(member.getProfile())
                .nickName(member.getNickName())
                .build();

        //when
        //then
        assertThatThrownBy(() -> partService.addPart(request, sessionMember))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageMatching("역할을 추가할 방이 존재하지 않습니다.");
    }

    @DisplayName("역할을 역할 요청을 받아 역할을 추가한다.")
    @Test
    void addPart(){
        //given
        Member member = Member.builder()
                .role(Role.USER)
                .email("email")
                .profile("profile")
                .nickName("nickname")
                .build();
        memberRepository.save(member);
        Room room = Room.builder()
                .leader(member)
                .password("password")
                .participationNum(1)
                .build();
        roomRepository.save(room);

        String partName = "조사할 부분";
        PartAddWebsocketRequest request = PartAddWebsocketRequest.builder()
                .roomId(room.getId())
                .partName(partName)
                .build();

        SessionMember sessionMember = SessionMember.builder()
                .memberSeq(member.getId())
                .profile(member.getProfile())
                .nickName(member.getNickName())
                .build();

        //when
        PartAddWebsocketResponse response = partService.addPart(request, sessionMember);
        List<Part> result = partRepository.findAll();
        //then
        assertThat(response).isNotNull()
                .extracting("partId","partName")
                .containsExactlyInAnyOrder(result.get(0).getId(),partName);
    }
}