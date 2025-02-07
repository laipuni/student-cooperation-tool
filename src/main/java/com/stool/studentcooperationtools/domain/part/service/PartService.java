package com.stool.studentcooperationtools.domain.part.service;

import com.stool.studentcooperationtools.domain.file.repository.FileRepository;
import com.stool.studentcooperationtools.domain.member.Member;
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
import com.stool.studentcooperationtools.websocket.controller.part.response.PartDeleteWebsocketResponse;
import com.stool.studentcooperationtools.websocket.controller.part.response.PartUpdateWebsocketResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PartService {

    private final PartRepository partRepository;
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;
    private final FileRepository fileRepository;

    public PartFindResponse findParts(final Long roomId) {
        List<Part> parts = partRepository.findAllByRoomId(roomId);
        return PartFindResponse.of(parts);
    }

    @Transactional
    public PartAddWebsocketResponse addPart(final PartAddWebsocketRequest request, final SessionMember sessionMember) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저는 존재하지 않습니다."));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("해당 방은 존재하지 않습니다."));

        Part part = Part.of(request.getPartName(), member, room);
        return PartAddWebsocketResponse.of(partRepository.save(part),member);
    }

    @Transactional(rollbackFor = AccessDeniedException.class)
    public PartDeleteWebsocketResponse deletePart(final PartDeleteWebsocketRequest request, final SessionMember member) {
        fileRepository.deleteAllByInPartId(List.of(request.getPartId()));
        int result = partRepository.deletePartByLeaderOrOwner(request.getPartId(), member.getMemberSeq());
        if(result == 0){
            throw new UnAuthorizationException("역할을 삭제할 권한이 없습니다.");
        }
        return PartDeleteWebsocketResponse.of(request.getPartId());
    }

    @Transactional
    public PartUpdateWebsocketResponse updatePart(final PartUpdateWebsocketRequest request, final SessionMember member) {
        Part part = partRepository.findById(request.getPartId())
                .orElseThrow(() -> new IllegalArgumentException("수정할 역할이 존재하지 않습니다."));
        if(!part.getRoom().isLeader(request.getMemberId())){
            //방장이 아닌 경우
            throw new UnAuthorizationException("역할을 수정할 권한이 없습니다.");
        }

        Member newMember = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저는 존재하지 않습니다."));
        part.update(newMember, request.getPartName());
        return PartUpdateWebsocketResponse.of(part);
    }
}
