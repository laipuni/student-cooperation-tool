package com.stool.studentcooperationtools.domain.chat.service;

import com.stool.studentcooperationtools.domain.chat.Chat;
import com.stool.studentcooperationtools.domain.chat.controller.response.ChatFindResponse;
import com.stool.studentcooperationtools.domain.chat.repository.ChatRepository;
import com.stool.studentcooperationtools.domain.member.Member;
import com.stool.studentcooperationtools.domain.member.repository.MemberRepository;
import com.stool.studentcooperationtools.domain.room.Room;
import com.stool.studentcooperationtools.domain.room.repository.RoomRepository;
import com.stool.studentcooperationtools.exception.global.UnAuthorizationException;
import com.stool.studentcooperationtools.security.oauth2.dto.SessionMember;
import com.stool.studentcooperationtools.websocket.controller.chat.request.ChatAddWebsocketRequest;
import com.stool.studentcooperationtools.websocket.controller.chat.request.ChatDeleteWebsocketRequest;
import com.stool.studentcooperationtools.websocket.controller.chat.response.ChatAddWebsocketResponse;
import com.stool.studentcooperationtools.websocket.controller.chat.response.ChatDeleteWebsocketResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatService {

    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final ChatRepository chatRepository;

    public ChatFindResponse findChats(final Long roomId,final int page, final Long lastMessageId) {
        PageRequest pageRequest = PageRequest.of(page, 10);
        if (lastMessageId == null){
            return ChatFindResponse.of(
                    chatRepository.findChatsByIdAndSlicingDESC(roomId, pageRequest));
        }
        return ChatFindResponse.of(
                chatRepository.findPrevChatsByIdAndSlicingDESC(roomId,pageRequest, lastMessageId)
        );
    }

    @Transactional
    public ChatAddWebsocketResponse addChat(final ChatAddWebsocketRequest request, final SessionMember sessionMember) {
        Member member = memberRepository.findById(sessionMember.getMemberSeq())
                .orElseThrow(() -> new IllegalArgumentException("채팅 등록에 실패했습니다."));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("채팅을 등록할 방이 존재하지 않습니다."));
        Chat chat = Chat.of(request.getContent(), member,room);
        log.info("사용자 {}이 채팅을 생성했다. (방: {})", sessionMember.getNickName(), room.getTitle());
        return ChatAddWebsocketResponse.of(chatRepository.save(chat));
    }

    @Transactional
    public ChatDeleteWebsocketResponse deleteChat(final ChatDeleteWebsocketRequest request, final SessionMember member) {
        Chat chat = chatRepository.findById(request.getChatId())
                .orElseThrow(() -> new IllegalArgumentException("삭제할 채팅이 존재하지 않습니다."));
        if(chat.isWrittenBy(member.getMemberSeq())){
            log.debug("사용자 {}은 권환 없이 채팅 제거 요청을 했다.",member.getNickName());
            throw new UnAuthorizationException("채팅을 제거할 자격이 없습니다.");
        }
        chatRepository.deleteById(request.getChatId());
        log.info("사용자 {}이 채팅을 제거했다. (채팅 {})", member.getNickName(), chat.getId());
        return new ChatDeleteWebsocketResponse(request.getChatId());
    }
}
