package com.stool.studentcooperationtools.domain.vote.service;

import com.stool.studentcooperationtools.domain.member.Member;
import com.stool.studentcooperationtools.domain.member.repository.MemberRepository;
import com.stool.studentcooperationtools.domain.topic.Topic;
import com.stool.studentcooperationtools.domain.topic.repository.TopicRepository;
import com.stool.studentcooperationtools.domain.vote.Vote;
import com.stool.studentcooperationtools.domain.vote.respository.VoteRepository;
import com.stool.studentcooperationtools.security.oauth2.dto.SessionMember;
import com.stool.studentcooperationtools.websocket.controller.vote.request.VoteUpdateWebSocketRequest;
import com.stool.studentcooperationtools.websocket.controller.vote.response.VoteUpdateWebSocketResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final MemberRepository memberRepository;
    private final TopicRepository topicRepository;

    @Transactional
    public VoteUpdateWebSocketResponse updateVote(final VoteUpdateWebSocketRequest request, final SessionMember sessionMember) {
        Vote vote = voteRepository.findVoteByMemberIdAndTopicId(sessionMember.getMemberSeq(), request.getTopicId());
        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new IllegalArgumentException("투표할 주제가 존재하지 않습니다."));
        if(vote != null){
            return cancelVote(topic, vote,sessionMember);
        } else{
            return saveVote(sessionMember, topic);
        }
    }

    private VoteUpdateWebSocketResponse saveVote(final SessionMember sessionMember, final Topic topic) {
        //만약 투표를 안 했다면 투표를 등록 한다.
        Member member = memberRepository.findById(sessionMember.getMemberSeq())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저는 존재하지 않습니다."));
        Vote vote = voteRepository.save(Vote.of(member, topic));
        log.info("사용자 (id : {})가 주제(id : {})에 투표(id:{})를 등록했다.",
                sessionMember.getMemberSeq(),topic.getId(),vote.getId());
        return VoteUpdateWebSocketResponse.of(topic);
    }

    private VoteUpdateWebSocketResponse cancelVote(final Topic topic, final Vote vote,final SessionMember sessionMember) {
        //만약 투표를 했다면 투표를 취소 한다.
        topic.minusVoteNum();
        voteRepository.deleteById(vote.getId());
        log.info("사용자 (id : {})가 주제(id : {})에 투표(id:{})를 취소했다.",
                sessionMember.getMemberSeq(),topic.getId(),vote.getId());
        return VoteUpdateWebSocketResponse.of(topic);
    }
}
