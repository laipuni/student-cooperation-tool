package com.stool.studentcooperationtools.domain.part.service;

import com.stool.studentcooperationtools.IntegrationTest;
import com.stool.studentcooperationtools.domain.member.Member;
import com.stool.studentcooperationtools.domain.member.Role;
import com.stool.studentcooperationtools.domain.member.repository.MemberRepository;
import com.stool.studentcooperationtools.domain.part.Part;
import com.stool.studentcooperationtools.domain.part.controller.response.PartFindResponse;
import com.stool.studentcooperationtools.domain.part.repository.PartRepository;
import com.stool.studentcooperationtools.domain.room.Room;
import com.stool.studentcooperationtools.domain.room.repository.RoomRepository;
import com.stool.studentcooperationtools.exception.global.UnAuthorizationException;
import com.stool.studentcooperationtools.security.oauth2.dto.SessionMember;
import com.stool.studentcooperationtools.websocket.controller.part.request.PartAddWebsocketRequest;
import com.stool.studentcooperationtools.websocket.controller.part.request.PartDeleteWebsocketRequest;
import com.stool.studentcooperationtools.websocket.controller.part.request.PartUpdateWebsocketRequest;
import com.stool.studentcooperationtools.websocket.controller.part.response.PartAddWebsocketResponse;
import com.stool.studentcooperationtools.websocket.controller.part.response.PartUpdateWebsocketResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@Transactional
class PartServiceTest extends IntegrationTest {

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
                .memberId(invalidMemberId)
                .partName(partName)
                .build();

        SessionMember sessionMember = SessionMember.builder()
                .memberSeq(invalidMemberId)
                .profile(invalidProfile)
                .nickName(invalidNickname)
                .build();

        //when
        //then
        assertThatThrownBy(() -> partService.addPart(request))
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
                .memberId(member.getId())
                .partName(partName)
                .build();

        SessionMember sessionMember = SessionMember.builder()
                .memberSeq(member.getId())
                .profile(member.getProfile())
                .nickName(member.getNickName())
                .build();

        //when
        //then
        assertThatThrownBy(() -> partService.addPart(request))
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
                .memberId(member.getId())
                .partName(partName)
                .build();

        SessionMember sessionMember = SessionMember.builder()
                .memberSeq(member.getId())
                .profile(member.getProfile())
                .nickName(member.getNickName())
                .build();

        //when
        PartAddWebsocketResponse response = partService.addPart(request);
        List<Part> result = partRepository.findAll();
        //then
        assertThat(response).isNotNull()
                .extracting("partId","partName")
                .containsExactlyInAnyOrder(result.get(0).getId(),partName);
    }

    @DisplayName("조사 자료 역할를 삭제할 때, 방장은 역할을 삭제할 수 있다.")
    @Test
    void deletePartWithLeader(){
        //given
        Member leader = Member.builder()
                .role(Role.USER)
                .email("email")
                .profile("profile")
                .nickName("nickname")
                .build();

        Member owner = Member.builder()
                .role(Role.USER)
                .email("OwnerEmail")
                .profile("OwnerProfile")
                .nickName("OwnerNickname")
                .build();

        memberRepository.saveAll(List.of(leader,owner));
        Room room = Room.builder()
                .leader(leader)
                .password("password")
                .participationNum(2)
                .build();
        roomRepository.save(room);
        Part part = Part.builder()
                .room(room)
                .partName("조사할 부분")
                .member(owner)
                .build();
        partRepository.save(part);

        PartDeleteWebsocketRequest request = PartDeleteWebsocketRequest.builder()
                .partId(part.getId())
                .roomId(room.getId())
                .build();

        //방장이 역할을 삭제하기 때문에 sessionMember도 방장의 정보를 넣음
        SessionMember sessionMember = SessionMember.builder()
                .memberSeq(leader.getId())
                .profile(leader.getProfile())
                .nickName(leader.getNickName())
                .build();

        //when
        partService.deletePart(request,sessionMember);
        List<Part> result = partRepository.findAll();
        //then
        assertThat(result).hasSize(0);
    }


    @DisplayName("조사 자료 역할를 삭제할 때, 역할을 만든 본인은 역할을 삭제할 수 있다.")
    @Test
    void deletePartWithOwner(){
        //given
        Member leader = Member.builder()
                .role(Role.USER)
                .email("email")
                .profile("profile")
                .nickName("nickname")
                .build();

        Member owner = Member.builder()
                .role(Role.USER)
                .email("OwnerEmail")
                .profile("OwnerProfile")
                .nickName("OwnerNickname")
                .build();

        memberRepository.saveAll(List.of(leader,owner));
        Room room = Room.builder()
                .leader(leader)
                .password("password")
                .participationNum(2)
                .build();
        roomRepository.save(room);
        Part part = Part.builder()
                .room(room)
                .partName("조사할 부분")
                .member(owner)
                .build();
        partRepository.save(part);

        PartDeleteWebsocketRequest request = PartDeleteWebsocketRequest.builder()
                .partId(part.getId())
                .roomId(room.getId())
                .build();

        //방장이 역할을 삭제하기 때문에 sessionMember도 방장의 정보를 넣음
        SessionMember sessionMember = SessionMember.builder()
                .memberSeq(owner.getId())
                .profile(owner.getProfile())
                .nickName(owner.getNickName())
                .build();

        //when
        partService.deletePart(request,sessionMember);
        List<Part> result = partRepository.findAll();
        //then
        assertThat(result).hasSize(0);
    }


    @DisplayName("조사 자료 역할를 삭제할 때, 역할을 만든 본인, 방장이 아닐 경우 에러가 발생한다.")
    @Test
    void deletePartWithAnother(){
        //given
        Member leader = Member.builder()
                .role(Role.USER)
                .email("email")
                .profile("profile")
                .nickName("nickname")
                .build();

        Member owner = Member.builder()
                .role(Role.USER)
                .email("email")
                .profile("profile")
                .nickName("nickname")
                .build();

        memberRepository.saveAll(List.of(leader,owner));
        Room room = Room.builder()
                .leader(leader)
                .password("password")
                .participationNum(2)
                .build();
        roomRepository.save(room);
        Part part = Part.builder()
                .room(room)
                .partName("조사할 부분")
                .member(owner)
                .build();
        partRepository.save(part);

        PartDeleteWebsocketRequest request = PartDeleteWebsocketRequest.builder()
                .partId(part.getId())
                .roomId(room.getId())
                .build();

        //방장이 역할을 삭제하기 때문에 sessionMember도 방장의 정보를 넣음
        Long InvalidMemberId = 2024L;
        SessionMember sessionMember = SessionMember.builder()
                .memberSeq(InvalidMemberId)
                .profile(owner.getProfile())
                .nickName(owner.getNickName())
                .build();

        //when
        //then
        assertThatThrownBy(() -> partService.deletePart(request,sessionMember))
                .isInstanceOf(UnAuthorizationException.class)
                .hasMessageMatching("역할을 삭제할 권한이 없습니다.");
    }

    @DisplayName("역할을 수정할 때, 수정할 역할이 없다면 예외가 발생한다.")
    @Test
    void updatePartWithNotExistPart(){
        //given
        String newPartName = "newPartName";
        Long invalidPartId = 1L;

        Member leader = Member.builder()
                .role(Role.USER)
                .email("email")
                .profile("profile")
                .nickName("nickname")
                .build();

        Member oldMember = Member.builder()
                .role(Role.USER)
                .email("oldMemberEmail")
                .profile("oldMemberProfile")
                .nickName("oldMemberNickname")
                .build();

        Member newMember = Member.builder()
                .role(Role.USER)
                .email("newMemberEmail")
                .profile("newMemberProfile")
                .nickName("newMemberNickname")
                .build();

        memberRepository.saveAll(List.of(leader,oldMember,newMember));
        Room room = Room.builder()
                .leader(leader)
                .password("password")
                .participationNum(2)
                .build();
        roomRepository.save(room);

        SessionMember sessionMember = SessionMember.builder()
                .memberSeq(leader.getId())
                .profile(leader.getProfile())
                .nickName(leader.getNickName())
                .build();

        PartUpdateWebsocketRequest request = PartUpdateWebsocketRequest.builder()
                .memberId(newMember.getId())
                .partId(invalidPartId)
                .partName(newPartName)
                .build();

        //when
        //then
        assertThatThrownBy(() -> partService.updatePart(request,sessionMember))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageMatching("수정할 역할이 존재하지 않습니다.");
    }

    @DisplayName("역할을 수정할 때, 역할을 맡은 유저가 바뀌었다면 수정한다.")
    @Test
    void updatePartWithChangeMember(){
        //given
        String oldPartName = "oldPartName";
        String newPartName = "newPartName";

        Member leader = Member.builder()
                .role(Role.USER)
                .email("email")
                .profile("profile")
                .nickName("nickname")
                .build();

        Member oldMember = Member.builder()
                .role(Role.USER)
                .email("oldMemberEmail")
                .profile("oldMemberProfile")
                .nickName("oldMemberNickname")
                .build();

        Member newMember = Member.builder()
                .role(Role.USER)
                .email("newMemberEmail")
                .profile("newMemberProfile")
                .nickName("newMemberNickname")
                .build();

        memberRepository.saveAll(List.of(leader,oldMember,newMember));
        Room room = Room.builder()
                .leader(leader)
                .password("password")
                .participationNum(3)
                .build();
        roomRepository.save(room);
        Part part = Part.builder()
                .room(room)
                .partName(oldPartName)
                .member(oldMember)
                .build();
        partRepository.save(part);

        SessionMember sessionMember = SessionMember.builder()
                .memberSeq(leader.getId())
                .profile(leader.getProfile())
                .nickName(leader.getNickName())
                .build();

        PartUpdateWebsocketRequest request = PartUpdateWebsocketRequest.builder()
                .memberId(newMember.getId())
                .partId(part.getId())
                .partName(newPartName)
                .build();

        //when
        PartUpdateWebsocketResponse response = partService.updatePart(request, sessionMember);

        //then
        assertThat(response).isNotNull()
                .extracting("partId","partName","nickName","profile")
                .containsExactlyInAnyOrder(
                        part.getId(),
                        newPartName,
                        newMember.getNickName(),
                        newMember.getProfile());
    }

    @DisplayName("역할을 수정할 때, 역할을 맡은 유저가 바뀌지 않고 역할 이름만 바꿨다면 수정한다.")
    @Test
    void updatePartWithOnlyChangePartName(){
        //given
        String oldPartName = "oldPartName";
        String newPartName = "newPartName";

        Member leader = Member.builder()
                .role(Role.USER)
                .email("email")
                .profile("profile")
                .nickName("nickname")
                .build();

        Member oldMember = Member.builder()
                .role(Role.USER)
                .email("oldMemberEmail")
                .profile("oldMemberProfile")
                .nickName("oldMemberNickname")
                .build();

        memberRepository.saveAll(List.of(leader,oldMember));
        Room room = Room.builder()
                .leader(leader)
                .password("password")
                .participationNum(2)
                .build();
        roomRepository.save(room);
        Part part = Part.builder()
                .room(room)
                .partName(oldPartName)
                .member(oldMember)
                .build();
        partRepository.save(part);

        SessionMember sessionMember = SessionMember.builder()
                .memberSeq(leader.getId())
                .profile(leader.getProfile())
                .nickName(leader.getNickName())
                .build();

        PartUpdateWebsocketRequest request = PartUpdateWebsocketRequest.builder()
                .memberId(oldMember.getId())
                .partId(part.getId())
                .partName(newPartName)
                .build();

        //when
        PartUpdateWebsocketResponse response = partService.updatePart(request, sessionMember);

        //then
        assertThat(response).isNotNull()
                .extracting("partId","partName","nickName","profile")
                .containsExactlyInAnyOrder(
                        part.getId(),
                        newPartName,
                        oldMember.getNickName(),
                        oldMember.getProfile()
                );
    }

    @DisplayName("역할을 수정할 때, 방장이 아닌 유저가 수정할 경우 에러가 발생한다..")
    @Test
    void updatePartWithNotLeader(){
        //given
        String oldPartName = "oldPartName";
        String newPartName = "newPartName";

        Member leader = Member.builder()
                .role(Role.USER)
                .email("email")
                .profile("profile")
                .nickName("nickname")
                .build();

        Member oldMember = Member.builder()
                .role(Role.USER)
                .email("oldMemberEmail")
                .profile("oldMemberProfile")
                .nickName("oldMemberNickname")
                .build();

        Member newMember = Member.builder()
                .role(Role.USER)
                .email("newMemberEmail")
                .profile("newMemberProfile")
                .nickName("newMemberNickname")
                .build();

        memberRepository.saveAll(List.of(leader,oldMember,newMember));
        Room room = Room.builder()
                .leader(leader)
                .password("password")
                .participationNum(3)
                .build();
        roomRepository.save(room);
        Part part = Part.builder()
                .room(room)
                .partName(oldPartName)
                .member(oldMember)
                .build();
        partRepository.save(part);

        SessionMember sessionMember = SessionMember.builder()
                .memberSeq(oldMember.getId())
                .profile(oldMember.getProfile())
                .nickName(oldMember.getNickName())
                .build();

        PartUpdateWebsocketRequest request = PartUpdateWebsocketRequest.builder()
                .memberId(newMember.getId())
                .partId(part.getId())
                .partName(newPartName)
                .build();

        //when
        //then
        assertThatThrownBy(() -> partService.updatePart(request,sessionMember))
                .isInstanceOf(UnAuthorizationException.class)
                .hasMessageMatching("역할을 수정할 권한이 없습니다.");
    }

    @DisplayName("역할을 수정할 때, 역할을 맡은 유저가 존재하지 않는다면 에러가 발생한다.")
    @Test
    void updatePartWithNotExistMember(){
        //given
        String oldPartName = "oldPartName";
        String newPartName = "newPartName";

        Member leader = Member.builder()
                .role(Role.USER)
                .email("email")
                .profile("profile")
                .nickName("nickname")
                .build();

        Member oldMember = Member.builder()
                .role(Role.USER)
                .email("email")
                .profile("profile")
                .nickName("nickname")
                .build();

        Long invalidMemberId = 2024L;

        memberRepository.saveAll(List.of(leader,oldMember));
        Room room = Room.builder()
                .leader(leader)
                .password("password")
                .participationNum(3)
                .build();
        roomRepository.save(room);
        Part part = Part.builder()
                .room(room)
                .partName(oldPartName)
                .member(oldMember)
                .build();
        partRepository.save(part);

        SessionMember sessionMember = SessionMember.builder()
                .memberSeq(leader.getId())
                .profile(leader.getProfile())
                .nickName(leader.getNickName())
                .build();

        PartUpdateWebsocketRequest request = PartUpdateWebsocketRequest.builder()
                .memberId(invalidMemberId)
                .partId(part.getId())
                .partName(newPartName)
                .build();

        //when
        //then
        assertThatThrownBy(() -> partService.updatePart(request,sessionMember))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageMatching("해당 유저는 존재하지 않습니다.");
    }
}