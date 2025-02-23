package com.stool.studentcooperationtools.domain.room.service;

import com.stool.studentcooperationtools.domain.member.Member;
import com.stool.studentcooperationtools.domain.member.repository.MemberRepository;
import com.stool.studentcooperationtools.domain.participation.Participation;
import com.stool.studentcooperationtools.domain.participation.repository.ParticipationRepository;
import com.stool.studentcooperationtools.domain.room.Room;
import com.stool.studentcooperationtools.domain.room.controller.request.RoomAddRequest;
import com.stool.studentcooperationtools.domain.room.controller.request.RoomEnterRequest;
import com.stool.studentcooperationtools.domain.room.controller.request.RoomTopicUpdateRequest;
import com.stool.studentcooperationtools.domain.room.controller.response.RoomAddResponse;
import com.stool.studentcooperationtools.domain.room.controller.response.RoomEnterResponse;
import com.stool.studentcooperationtools.domain.room.controller.response.RoomSearchResponse;
import com.stool.studentcooperationtools.domain.room.repository.RoomRepository;
import com.stool.studentcooperationtools.domain.topic.Topic;
import com.stool.studentcooperationtools.domain.topic.repository.TopicRepository;
import com.stool.studentcooperationtools.exception.global.DuplicateDataException;
import com.stool.studentcooperationtools.exception.global.UnAuthorizationException;
import com.stool.studentcooperationtools.security.oauth2.dto.SessionMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final TopicRepository topicRepository;
    private final ParticipationRepository participationRepository;

    @Transactional
    public RoomAddResponse addRoom(SessionMember member, final RoomAddRequest request) {
        Member user = memberRepository.findById(member.getMemberSeq())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저는 존재하지 않습니다."));
        Room room = Room.of(request, user);
        saveRoom(room, member.getMemberSeq());
        List<Participation> participationList = new ArrayList<>();
        participationList.add(Participation.of(user, room));
        List<Member> memberList = memberRepository.findMembersByMemberIdList(request.getParticipation());
        memberList.forEach(findMember -> {
            participationList.add(Participation.of(findMember, room));
        });
        participationRepository.saveAll(participationList);
        return RoomAddResponse.of(room);
    }

    private void saveRoom(final Room room,final Long memberId) {
        try {
            roomRepository.save(room);
            log.info("사용자(Id : {})가 방(id : {})을 생성했다.",memberId, room.getTitle());
        } catch (DataIntegrityViolationException e){
            throw new DuplicateDataException("동일한 제목의 방이 존재합니다.",e.getCause());
        }
    }

    public RoomSearchResponse searchRoom(final String title, final int page,
                                         final boolean isParticipation, final Long memberId) {
        return roomRepository.searchRoomsBy(title, page, isParticipation,memberId);
    }

    @Transactional
    public RoomEnterResponse enterRoom(SessionMember member, final RoomEnterRequest request){
        Room room = roomRepository.findRoomWithPLock(request.getRoomId())
                .orElseThrow(()-> new IllegalArgumentException("해당 방은 존재하지 않습니다."));
        if(!room.verifyPassword(request.getPassword())) {
            log.debug("사용자(Id : {})가 방(id : {})의 비밀번호를 틀렸습니다.", member.getMemberSeq(),request.getRoomId());
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }
        addParticipation(member.getMemberSeq(),room);
        return RoomEnterResponse.of(room.getLeader().getId());
    }

    private void addParticipation(Long memberId, Room room){
        if(participationRepository.existsByMemberIdAndRoomId(memberId, room.getId())){
            return;
        }
        Member user = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저는 존재하지 않습니다."));
        participationRepository.save(Participation.of(user, room));
    }

    @Transactional
    public Boolean updateRoomTopic(SessionMember member, final RoomTopicUpdateRequest request) {
        Room room = roomRepository.findRoomByRoomId(member.getMemberSeq(), request.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("방이 존재하지 않습니다"));
        if(!room.isLeader(member.getMemberSeq())){
            log.debug("사용자(Id : {})는 방(id : {})의 메인 주제를 변경할 권한이 없다.",
                    member.getMemberSeq(),request.getRoomId());
            throw new UnAuthorizationException("방의 주제를 변경할 권한이 없습니다.");
        }
        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new IllegalArgumentException("해당 주제는 존재하지 않습니다"));
        room.updateTopic(topic);
        return true;
    }

    //해당 방에 참여한 인원인지 확인하고, 아니라면 접근 제한 예외 발생
    public void validateParticipationInRoom(final Long roomId, final SessionMember sessionMember) {
        if(!roomRepository.existMemberInRoom(sessionMember.getMemberSeq(),roomId)){
            log.debug("사용자(Id : {})는 방(id : {})의 참여 권한이 없다.", sessionMember.getMemberSeq(),roomId);
            throw new UnAuthorizationException("해당 방의 참여 권한이 없습니다.");
        }
    }
}
