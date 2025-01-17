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
import com.stool.studentcooperationtools.domain.topic.repository.TopicRepository;
import com.stool.studentcooperationtools.security.oauth2.dto.SessionMember;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

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
                .orElseThrow(() -> new IllegalArgumentException("유저 정보가 등록되어 있지 않습니다"));
        Room room = Room.builder()
                .password(request.getPassword())
                .participationNum(request.getParticipation().size())
                .title(request.getTitle())
                .leader(user)
                .build();
        try {
            roomRepository.save(room);
        } catch (DataIntegrityViolationException e){
            throw new DataIntegrityViolationException("방 제목 중복 오류입니다");
        }
        participationRepository.save(Participation.of(user, room));
        List<Member> memberList = memberRepository.findMembersByMemberIdList(request.getParticipation());
        List<Participation> participation = memberList.stream()
                .map(findMember -> Participation.of(findMember, room))
                .toList();
        participationRepository.saveAll(participation);
        return RoomAddResponse.builder()
                .roomId(room.getId())
                .title(room.getTitle())
                .build();
    }

    public RoomSearchResponse searchRoom(
            final String title, final int page,
            final boolean isParticipation, final Long memberId
    ) {
        return roomRepository.searchRoomsBy(title, page, isParticipation,memberId);
    }

    @Transactional
    public RoomEnterResponse enterRoom(SessionMember member, final RoomEnterRequest request){
        Room room = roomRepository.findRoomWithPLock(request.getRoomId())
                .orElseThrow(()-> new IllegalArgumentException("방 id 오류"));
        if(!room.verifyPassword(request.getPassword())) {
            throw new IllegalArgumentException("올바르지 않은 비밀번호입니다");
        }
        addParticipation(member,room);
        return RoomEnterResponse.builder()
                .leaderId(room.getLeader().getId())
                .build();
    }

    private void addParticipation(SessionMember member, Room room){
        Member user = memberRepository.findById(member.getMemberSeq())
                .orElseThrow(() -> new IllegalArgumentException("유저 정보가 올바르지 않습니다"));
        if(!participationRepository.existsByMemberIdAndRoomId(member.getMemberSeq(), room.getId())){
            participationRepository.save(Participation.of(user, room));
        }
    }

    @Transactional
    public Boolean updateRoomTopic(SessionMember member, final RoomTopicUpdateRequest request) {
        Room room = roomRepository.findRoomByRoomId(member.getMemberSeq(), request.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("올바르지 않은 방 정보입니다"));
        if(!Objects.equals(member.getMemberSeq(), room.getLeader().getId())){
            throw new IllegalArgumentException("팀장의 권한이 부여되지 않았습니다");
        }
        room.updateTopic(topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new IllegalArgumentException("올바르지 않은 주제 정보입니다")));
        return true;
    }

    //해당 방에 참여한 인원인지 확인하고, 아니라면 접근 제한 예외 발생
    public void validParticipationInRoom(final Long roomId, final SessionMember sessionMember) {
        if(!roomRepository.existMemberInRoom(sessionMember.getMemberSeq(),roomId)){
            throw new AccessDeniedException("해당 방에 참여하지 않아 권한이 없습니다.");
        }
    }
}
