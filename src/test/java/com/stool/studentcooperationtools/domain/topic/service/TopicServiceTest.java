package com.stool.studentcooperationtools.domain.topic.service;

import com.stool.studentcooperationtools.IntegrationTest;
import com.stool.studentcooperationtools.domain.member.Member;
import com.stool.studentcooperationtools.domain.member.Role;
import com.stool.studentcooperationtools.domain.member.repository.MemberRepository;
import com.stool.studentcooperationtools.domain.room.Room;
import com.stool.studentcooperationtools.domain.room.repository.RoomRepository;
import com.stool.studentcooperationtools.domain.topic.Topic;
import com.stool.studentcooperationtools.domain.topic.repository.TopicRepository;
import com.stool.studentcooperationtools.exception.global.UnAuthorizationException;
import com.stool.studentcooperationtools.security.oauth2.dto.SessionMember;
import com.stool.studentcooperationtools.websocket.controller.topic.request.TopicAddSocketRequest;
import com.stool.studentcooperationtools.websocket.controller.topic.request.TopicDeleteSocketRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class TopicServiceTest extends IntegrationTest {

    @Autowired
    TopicService topicService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    TopicRepository topicRepository;

    @DisplayName("주제를 등록할 때, 주제를 등록하는 유저가 조회가 안될 경우 에러가 발생한다.")
    @Test
    void addTopicWithNotExistMember(){
        //given
        TopicAddSocketRequest request = TopicAddSocketRequest.builder()
                .roomId(1L)
                .topic("주제 제목")
                .build();
        Long InvalidMemberId = 1L;

        SessionMember sessionMember = SessionMember.builder()
                .profile("profile")
                .memberSeq(InvalidMemberId)
                .nickName("닉네임")
                .build();

        //when
        //then
        assertThatThrownBy(() -> topicService.addTopic(request,sessionMember))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageMatching("해당 유저는 존재하지 않습니다");
    }


    @DisplayName("주제를 등록할 때, 주제를 등록할 방이 조회가 안될 경우 에러가 발생한다.")
    @Test
    void addTopicWithNotExistRoom(){
        //given
        Member member = Member.builder()
                .email("email")
                .nickName("nickname")
                .profile("profile")
                .role(Role.USER)
                .build();
        memberRepository.save(member);
        SessionMember sessionMember = SessionMember.builder()
                .profile(member.getProfile())
                .memberSeq(member.getId())
                .nickName(member.getNickName())
                .build();


        long invalidRoomId = 2024L;
        TopicAddSocketRequest request = TopicAddSocketRequest.builder()
                .roomId(invalidRoomId)
                .topic("주제 제목")
                .build();
        //when
        //then
        assertThatThrownBy(() -> topicService.addTopic(request, sessionMember))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageMatching("해당 방은 존재하지 않습니다.");
    }

    @DisplayName("주제와 방 정보를 받아서 주제를 등록한다.")
    @Test
    void addTopic() {
        //given
        Member member = Member.builder()
                .email("email")
                .nickName("nickname")
                .profile("profile")
                .role(Role.USER)
                .build();

        memberRepository.save(member);

        Room room = Room.builder()
                .participationNum(0)
                .title("방 제목")
                .password("password")
                .build();

        roomRepository.save(room);

        SessionMember sessionMember = SessionMember.builder()
                .profile(member.getProfile())
                .memberSeq(member.getId())
                .nickName(member.getNickName())
                .build();

        TopicAddSocketRequest request = TopicAddSocketRequest.builder()
                .roomId(room.getId())
                .topic("주제 제목")
                .build();
        //when
        topicService.addTopic(request, sessionMember);
        List<Topic> topics = topicRepository.findAll();
        //then
        assertThat(topics).hasSize(1)
                .extracting("topic")
                .containsExactlyInAnyOrder(request.getTopic());
    }

    @DisplayName("주제를 삭제할 때, 주제를 삭제할 권한(방장,본인)이 없다면 에러가 발생한다.")
    @Test
    void deleteTopic(){
        //given
        Member member = Member.builder()
                .email("email")
                .nickName("nickname")
                .profile("profile")
                .role(Role.USER)
                .build();

        memberRepository.save(member);

        Room room = Room.builder()
                .participationNum(0)
                .title("방 제목")
                .password("password")
                .build();

        roomRepository.save(room);

        Long InvalidMemberId = 2024L;
        SessionMember sessionMember = SessionMember.builder()
                .profile(member.getProfile())
                .memberSeq(InvalidMemberId)
                .nickName(member.getNickName())
                .build();

        String topicContent = "주제";
        Topic topic = Topic.builder()
                .room(room)
                .member(member)
                .topic(topicContent)
                .build();

        topicRepository.save(topic);

        TopicDeleteSocketRequest request = TopicDeleteSocketRequest.builder()
                .roomId(room.getId())
                .topicId(topic.getId())
                .build();
        //when
        List<Topic> topics = topicRepository.findAll();
        //then
        assertThatThrownBy(() -> topicService.deleteTopic(request, sessionMember))
                .isInstanceOf(UnAuthorizationException.class)
                .hasMessageMatching("주제를 삭제할 권한이 없습니다.");
        assertThat(topics).hasSize(1);
    }
}