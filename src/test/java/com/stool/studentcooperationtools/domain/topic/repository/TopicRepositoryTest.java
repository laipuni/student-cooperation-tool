package com.stool.studentcooperationtools.domain.topic.repository;

import com.stool.studentcooperationtools.domain.member.Member;
import com.stool.studentcooperationtools.domain.member.Role;
import com.stool.studentcooperationtools.domain.member.repository.MemberRepository;
import com.stool.studentcooperationtools.domain.room.Room;
import com.stool.studentcooperationtools.domain.room.repository.RoomRepository;
import com.stool.studentcooperationtools.domain.topic.Topic;
import com.stool.studentcooperationtools.domain.vote.Vote;
import com.stool.studentcooperationtools.domain.vote.respository.VoteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class TopicRepositoryTest {

    @Autowired
    TopicRepository topicRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    VoteRepository voteRepository;

    @DisplayName("방의 식별키로 해당 방의 주제들을 조회한다.")
    @Test
    void findAllByRoomId(){
        //given
        Room room = Room.builder()
                .password("password")
                .title("제목")
                .participationNum(0)
                .build();
        roomRepository.save(room);
        Member member = Member.builder()
                .email("email")
                .nickName("닉네임")
                .profile("profile")
                .role(Role.USER)
                .build();
        memberRepository.save(member);
        Topic topic =Topic.builder()
                .topic("주제")
                .member(member)
                .room(room)
                .build();
        Vote vote = Vote.builder()
                .voter(member)
                .topic(topic)
                .build();
        topic.addVote(vote);
        topicRepository.save(topic);
        voteRepository.save(vote);

        //when
        List<Topic> topics = topicRepository.findAllByRoomId(room.getId());
        //then
        assertThat(topics).hasSize(1);
        assertThat(topics.get(0).getRoom())
                .extracting("password","title","participationNum")
                .containsExactly(room.getPassword(),room.getTitle(),room.getParticipationNum());
        assertThat(topics.get(0).getMember())
                .extracting("email","nickName","profile")
                .containsExactly(member.getEmail(),member.getNickName(),member.getProfile());
        assertThat(topics.get(0).getVotes().get(0).getId()).isNotNull();
    }

}