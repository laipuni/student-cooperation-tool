package com.stool.studentcooperationtools.domain.topic.service;

import com.stool.studentcooperationtools.domain.member.Member;
import com.stool.studentcooperationtools.domain.member.repository.MemberRepository;
import com.stool.studentcooperationtools.domain.room.Room;
import com.stool.studentcooperationtools.domain.room.repository.RoomRepository;
import com.stool.studentcooperationtools.domain.topic.Topic;
import com.stool.studentcooperationtools.domain.topic.controller.response.TopicFindResponse;
import com.stool.studentcooperationtools.domain.topic.repository.TopicRepository;
import com.stool.studentcooperationtools.domain.vote.respository.VoteRepository;
import com.stool.studentcooperationtools.exception.global.UnAuthorizationException;
import com.stool.studentcooperationtools.security.oauth2.dto.SessionMember;
import com.stool.studentcooperationtools.websocket.controller.topic.request.TopicAddSocketRequest;
import com.stool.studentcooperationtools.websocket.controller.topic.request.TopicDeleteSocketRequest;
import com.stool.studentcooperationtools.websocket.controller.topic.response.TopicAddSocketResponse;
import com.stool.studentcooperationtools.websocket.controller.topic.response.TopicDeleteSocketResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TopicService {

    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;
    private final TopicRepository topicRepository;
    private final VoteRepository voteRepository;

    public TopicFindResponse findTopics(final Long roomId) {
        List<Topic> topics = topicRepository.findAllByRoomId(roomId);
        return TopicFindResponse.of(topics);
    }

    @Transactional
    public TopicAddSocketResponse addTopic(final TopicAddSocketRequest request, final SessionMember sessionMember) {
        Member member = memberRepository.findById(sessionMember.getMemberSeq())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저는 존재하지 않습니다"));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("해당 방은 존재하지 않습니다."));
        Topic topic = Topic.builder()
                .topic(request.getTopic())
                .member(member)
                .room(room)
                .build();
        topicRepository.save(topic);
        return TopicAddSocketResponse.of(topic);
    }

    @Transactional(rollbackFor = UnAuthorizationException.class)
    public TopicDeleteSocketResponse deleteTopic(final TopicDeleteSocketRequest request, SessionMember member) {
        voteRepository.deleteAllByTopicId(request.getTopicId());
        if(topicRepository.deleteTopicByLeaderOrOwner(request.getTopicId(), member.getMemberSeq()) == 0){
            //본인,방장이 아닌 경우는 삭제를 할 수 없다.
            throw new UnAuthorizationException("주제를 삭제할 권한이 없습니다.");
        };
        return TopicDeleteSocketResponse.of(request.getTopicId());
    }
}
