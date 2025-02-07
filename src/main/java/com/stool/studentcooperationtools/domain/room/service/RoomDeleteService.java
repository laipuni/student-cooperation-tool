package com.stool.studentcooperationtools.domain.room.service;

import com.stool.studentcooperationtools.domain.chat.repository.ChatRepository;
import com.stool.studentcooperationtools.domain.file.repository.FileRepository;
import com.stool.studentcooperationtools.domain.member.Member;
import com.stool.studentcooperationtools.domain.member.repository.MemberRepository;
import com.stool.studentcooperationtools.domain.part.repository.PartRepository;
import com.stool.studentcooperationtools.domain.participation.Participation;
import com.stool.studentcooperationtools.domain.participation.repository.ParticipationRepository;
import com.stool.studentcooperationtools.domain.presentation.repository.PresentationRepository;
import com.stool.studentcooperationtools.domain.review.repository.ReviewRepository;
import com.stool.studentcooperationtools.domain.room.Room;
import com.stool.studentcooperationtools.domain.room.controller.request.RoomRemoveRequest;
import com.stool.studentcooperationtools.domain.room.repository.RoomRepository;
import com.stool.studentcooperationtools.domain.script.repository.ScriptRepository;
import com.stool.studentcooperationtools.domain.slide.repository.SlideRepository;
import com.stool.studentcooperationtools.domain.topic.repository.TopicRepository;
import com.stool.studentcooperationtools.domain.vote.respository.VoteRepository;
import com.stool.studentcooperationtools.security.oauth2.dto.SessionMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomDeleteService {

    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final TopicRepository topicRepository;
    private final ParticipationRepository participationRepository;
    private final SlideRepository slideRepository;
    private final ChatRepository chatRepository;
    private final PartRepository partRepository;
    private final PresentationRepository presentationRepository;
    private final VoteRepository voteRepository;
    private final ScriptRepository scriptRepository;
    private final ReviewRepository reviewRepository;
    private final FileRepository fileRepository;

    @Transactional
    public Boolean removeRoom(final SessionMember member, final RoomRemoveRequest request) {
        Room room = roomRepository.findRoomWithPLock(request.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("해당 방을 찾을 수 없습니다."));
        if (room.isLeader(member.getMemberSeq())) {
            removeRoomAsLeader(room);
        } else {
            leaveRoomAsMember(member.getMemberSeq(), room);
        }
        return true;
    }

    private void removeRoomAsLeader(final Room room) {
        removePartIn(room);
        chatRepository.deleteByRoomId(room.getId());
        removeTopicIn(room);
        removePptIn(room);
        participationRepository.deleteByRoomId(room.getId());
        roomRepository.deleteById(room.getId());
        log.info("방이 삭제되었습니다. (Room ID: {})", room.getId());
    }

    private void leaveRoomAsMember(final Long memberId,final Room room) {
        Member teammate = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));
        if (!participationRepository.existsByMemberIdAndRoomId(teammate.getId(), room.getId())) {
            throw new IllegalArgumentException("참여 정보가 없는 유저입니다.");
        }
        removeParticipation(teammate, room);
        participationRepository.deleteByMemberIdAndRoomId(teammate.getId(), room.getId());
        log.info("사용자(Id : {}) 가 방(Id : {})을 나갔습니다.", teammate.getId(), room.getId());
    }

    private void removePartIn(final Room room) {
        List<Long> partIdsByRoomId = partRepository.findPartIdsByRoomId(room.getId());
        fileRepository.deleteAllByInPartId(partIdsByRoomId);
        reviewRepository.deleteAllByInPartId(partIdsByRoomId);
        partRepository.deleteByRoomId(room.getId());
        log.info("Part, 파일, 리뷰 삭제 완료 (Room ID: {})", room.getId());
    }

    private void removePptIn(final Room room) {
        Long presentationId = presentationRepository.findPresentationIdByRoomId(room.getId());
        if (presentationId != null) {
            slideRepository.deleteByPresentationId(presentationId);
            scriptRepository.deleteByPresentationId(presentationId);
            presentationRepository.deleteByRoomId(room.getId());
            log.info("슬라이드, 스크립트, PPT 삭제 완료 (Room ID: {})", room.getId());
        }
    }

    private void removeTopicIn(final Room room) {
        List<Long> topicIds = topicRepository.findTopicIdByRoomId(room.getId());
        if (!topicIds.isEmpty()) {
            voteRepository.deleteAllByInTopicId(topicIds);
        }
        if (room.getMainTopic() != null) {
            room.updateTopic(null);
        }
        topicRepository.deleteByRoomId(room.getId());
        log.info("토픽 및 투표 삭제 완료 (Room ID: {})", room.getId());
    }

    private void removeParticipation(final Member user,final Room room) {
        Participation participation = participationRepository.findByMemberIdAndRoomId(user.getId(), room.getId());
        if (participation != null) {
            room.deleteParticipation(participation);
        }
    }
}
