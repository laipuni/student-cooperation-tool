package com.stool.studentcooperationtools.domain.chat.service;

import com.stool.studentcooperationtools.IntegrationTest;
import com.stool.studentcooperationtools.domain.chat.Chat;
import com.stool.studentcooperationtools.domain.chat.repository.ChatRepository;
import com.stool.studentcooperationtools.domain.member.Member;
import com.stool.studentcooperationtools.domain.member.Role;
import com.stool.studentcooperationtools.domain.member.repository.MemberRepository;
import com.stool.studentcooperationtools.domain.room.Room;
import com.stool.studentcooperationtools.domain.room.repository.RoomRepository;
import com.stool.studentcooperationtools.exception.global.UnAuthorizationException;
import com.stool.studentcooperationtools.security.oauth2.dto.SessionMember;
import com.stool.studentcooperationtools.websocket.controller.chat.request.ChatAddWebsocketRequest;
import com.stool.studentcooperationtools.websocket.controller.chat.request.ChatDeleteWebsocketRequest;
import com.stool.studentcooperationtools.websocket.controller.chat.response.ChatAddWebsocketResponse;
import com.stool.studentcooperationtools.websocket.controller.chat.response.ChatDeleteWebsocketResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class ChatServiceTest extends IntegrationTest {

    @Autowired
    ChatService chatService;

    @Autowired
    ChatRepository chatRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    RoomRepository roomRepository;

    @DisplayName("채팅 등록할 때, 채팅을 등록할 유저가 존재하지 않는다면 에러가 발생한다.")
    @Test
    void addChatWithNotExistUser(){
        //given
        Long invalidRoomId = 1L;
        Long invalidMemberId = 1L;
        ChatAddWebsocketRequest request = ChatAddWebsocketRequest.builder()
                .roomId(invalidRoomId)
                .content("채팅 내용")
                .build();

        SessionMember sessionMember =SessionMember.builder()
                .memberSeq(invalidMemberId)
                .nickName("닉네임")
                .profile("프로필")
                .build();

        //when
        //then
        assertThatThrownBy(() -> chatService.addChat(request,sessionMember))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageMatching("채팅 등록에 실패했습니다.");
    }


    @DisplayName("채팅 등록할 때, 채팅을 등록할 방이 존재하지 않는다면 에러가 발생한다.")
    @Test
    void addChatWithNotExistRoom(){
        //given
        Long invalidRoomId = 1L;

        Member member = Member.builder()
                .role(Role.USER)
                .email("email")
                .profile("profile")
                .nickName("nickname")
                .build();
        memberRepository.save(member);

        ChatAddWebsocketRequest request = ChatAddWebsocketRequest.builder()
                .roomId(invalidRoomId)
                .content("채팅 내용")
                .build();

        SessionMember sessionMember =SessionMember.builder()
                .memberSeq(member.getId())
                .nickName("닉네임")
                .profile("프로필")
                .build();

        //when
        //then
        assertThatThrownBy(() -> chatService.addChat(request,sessionMember))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageMatching("채팅을 등록할 방이 존재하지 않습니다.");
    }



    @DisplayName("채팅 등록 요청을 받아 채팅을 등록한다.")
    @Test
    void addChat(){
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

        ChatAddWebsocketRequest request = ChatAddWebsocketRequest.builder()
                .roomId(room.getId())
                .content("채팅 내용")
                .build();

        SessionMember sessionMember =SessionMember.builder()
                .memberSeq(member.getId())
                .nickName("닉네임")
                .profile("프로필")
                .build();
        //when
        ChatAddWebsocketResponse response = chatService.addChat(request, sessionMember);
        List<Chat> chats = chatRepository.findAll();
        //then
        assertThat(chats).hasSize(1);
    }

    @DisplayName("삭제할 채팅의 식별키를 받아 채팅을 삭제한다.")
    @Test
    void deleteChat(){
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

        Chat chat = Chat.builder()
                .room(room)
                .member(member)
                .content("댓글 내용")
                .build();
        chatRepository.save(chat);

        ChatDeleteWebsocketRequest request = ChatDeleteWebsocketRequest.builder()
                .chatId(chat.getId())
                .roomId(room.getId())
                .build();

        SessionMember sessionMember =SessionMember.builder()
                .memberSeq(member.getId())
                .nickName("닉네임")
                .profile("프로필")
                .build();
        //when
        ChatDeleteWebsocketResponse response = chatService.deleteChat(request, sessionMember);
        List<Chat> chats = chatRepository.findAll();
        //then
        assertThat(chats).hasSize(0);
        assertThat(response).isNotNull();
    }

    @DisplayName("채팅을 삭제할 때, 삭제할 채팅이 존재하지 않으면 에러가 발생한다.")
    @Test
    void deleteChatWithNotExistChat(){
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

        Long invalidChatId = 1L;
        ChatDeleteWebsocketRequest request = ChatDeleteWebsocketRequest.builder()
                .chatId(invalidChatId)
                .roomId(room.getId())
                .build();

        SessionMember sessionMember =SessionMember.builder()
                .memberSeq(member.getId())
                .nickName("닉네임")
                .profile("프로필")
                .build();
        //when
        //then
        assertThatThrownBy(() -> chatService.deleteChat(request, sessionMember))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageMatching("삭제할 채팅이 존재하지 않습니다.");
    }

    @DisplayName("채팅을 삭제할 때, 삭제하는 유저가 존재하지 않으면 에러가 발생한다.")
    @Test
    void deleteChatWithNotExistUser(){
        //given
        Member chatWriter = Member.builder()
                .role(Role.USER)
                .email("email")
                .profile("profile")
                .nickName("nickname")
                .build();
        Member anotherMember = Member.builder()
                .role(Role.USER)
                .email("email")
                .profile("profile")
                .nickName("nickname")
                .build();
        memberRepository.saveAll(List.of(chatWriter,anotherMember));

        Room room = Room.builder()
                .leader(anotherMember)
                .password("password")
                .participationNum(1)
                .build();
        roomRepository.save(room);

        Chat chat = Chat.builder()
                .room(room)
                .member(chatWriter)
                .content("댓글 내용")
                .build();
        chatRepository.save(chat);


        ChatDeleteWebsocketRequest request = ChatDeleteWebsocketRequest.builder()
                .chatId(chat.getId())
                .roomId(room.getId())
                .build();

        SessionMember sessionMember =SessionMember.builder()
                .memberSeq(anotherMember.getId())
                .nickName("닉네임")
                .profile("프로필")
                .build();
        //when
        //then
        assertThatThrownBy(() -> chatService.deleteChat(request, sessionMember))
                .isInstanceOf(UnAuthorizationException.class)
                .hasMessageMatching("채팅을 제거할 권한이 없습니다.");
    }

}