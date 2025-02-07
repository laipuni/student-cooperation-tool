package com.stool.studentcooperationtools.domain.room.service;

import com.stool.studentcooperationtools.IntegrationTest;
import com.stool.studentcooperationtools.domain.chat.repository.ChatRepository;
import com.stool.studentcooperationtools.domain.file.File;
import com.stool.studentcooperationtools.domain.file.FileType;
import com.stool.studentcooperationtools.domain.file.repository.FileRepository;
import com.stool.studentcooperationtools.domain.member.Member;
import com.stool.studentcooperationtools.domain.member.Role;
import com.stool.studentcooperationtools.domain.member.repository.MemberRepository;
import com.stool.studentcooperationtools.domain.part.Part;
import com.stool.studentcooperationtools.domain.part.repository.PartRepository;
import com.stool.studentcooperationtools.domain.participation.Participation;
import com.stool.studentcooperationtools.domain.participation.repository.ParticipationRepository;
import com.stool.studentcooperationtools.domain.presentation.Presentation;
import com.stool.studentcooperationtools.domain.presentation.repository.PresentationRepository;
import com.stool.studentcooperationtools.domain.review.Review;
import com.stool.studentcooperationtools.domain.review.repository.ReviewRepository;
import com.stool.studentcooperationtools.domain.room.Room;
import com.stool.studentcooperationtools.domain.room.controller.request.RoomRemoveRequest;
import com.stool.studentcooperationtools.domain.room.repository.RoomRepository;
import com.stool.studentcooperationtools.domain.script.Script;
import com.stool.studentcooperationtools.domain.script.repository.ScriptRepository;
import com.stool.studentcooperationtools.domain.slide.Slide;
import com.stool.studentcooperationtools.domain.slide.repository.SlideRepository;
import com.stool.studentcooperationtools.domain.topic.Topic;
import com.stool.studentcooperationtools.domain.topic.repository.TopicRepository;
import com.stool.studentcooperationtools.domain.vote.respository.VoteRepository;
import com.stool.studentcooperationtools.security.oauth2.dto.SessionMember;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
class RoomDeleteServiceTest extends IntegrationTest {

    @Autowired
    RoomRepository roomRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TopicRepository topicRepository;
    @Autowired
    ParticipationRepository participationRepository;
    @Autowired
    SlideRepository slideRepository;
    @Autowired
    ChatRepository chatRepository;
    @Autowired
    PartRepository partRepository;
    @Autowired
    PresentationRepository presentationRepository;
    @Autowired
    VoteRepository voteRepository;
    @Autowired
    ScriptRepository scriptRepository;
    @Autowired
    ReviewRepository reviewRepository;
    @Autowired
    FileRepository fileRepository;
    @Autowired
    RoomDeleteService roomDeleteService;

    @Test
    @DisplayName("소속되지 않은 방에 대해 삭제 요청 시 에러")
    void removeNotBelongingRoom() {
        // given
        Member user = Member.builder()
                .role(Role.USER)
                .email(UUID.randomUUID() + "@test.com")
                .profile("profile")
                .nickName("nickName")
                .build();
        memberRepository.save(user);
        SessionMember member = SessionMember.of(user);

        Member leader = Member.builder()
                .role(Role.USER)
                .email(UUID.randomUUID() + "@test.com")
                .profile("profile")
                .nickName("nickName")
                .build();
        memberRepository.save(leader);

        Room room = Room.builder()
                .title("room")
                .participationNum(1)
                .leader(leader)
                .password("password")
                .build();
        roomRepository.save(room);
        participationRepository.save(Participation.of(leader, room));

        RoomRemoveRequest roomRemoveRequest = RoomRemoveRequest.builder()
                .roomId(room.getId())
                .build();

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> roomDeleteService.removeRoom(member, roomRemoveRequest)
        );

        assertThat(exception.getMessage()).contains("참여 정보가 없는 유저입니다");
    }

    @Test
    @DisplayName("방장이 방 삭제 요청 시 방과 모든 참여 인원이 삭제")
    void removeRoomByLeader() {
        // given
        Member owner = Member.builder()
                .email(UUID.randomUUID() + "@test.com") // 중복 방지
                .nickName("평가자")
                .profile("평가자 프로필")
                .role(Role.USER)
                .build();
        memberRepository.save(owner);

        Room room = Room.builder()
                .password("password")
                .title("제목")
                .leader(owner)
                .participationNum(2)
                .build();
        roomRepository.save(room);

        participationRepository.save(Participation.of(owner, room));

        Topic topic = Topic.builder()
                .voteNum(1)
                .member(owner)
                .topic("주제")
                .room(room)
                .build();
        topicRepository.save(topic);

        Part part = Part.builder()
                .partName("조사할 부분")
                .room(room)
                .member(owner)
                .build();
        partRepository.save(part);

        File file = File.builder()
                .part(part)
                .originalName("originalFileName")
                .fileName(UUID.randomUUID().toString())
                .fileType(FileType.DOCX)
                .build();
        fileRepository.save(file);

        Review review = Review.builder()
                .content("평가 댓글")
                .member(owner)
                .part(part)
                .build();
        reviewRepository.save(review);

        Presentation presentation = Presentation.builder()
                .presentationPath("path")
                .room(room)
                .build();
        presentationRepository.save(presentation);

        Script script = Script.builder()
                .script("발표 내용")
                .presentation(presentation)
                .build();
        scriptRepository.save(script);

        Slide slide = Slide.builder()
                .script(script)
                .slide_index(0)
                .slideUrl("url")
                .presentation(presentation)
                .thumbnail("thumbnail")
                .build();
        slideRepository.save(slide);

        RoomRemoveRequest roomRemoveRequest = RoomRemoveRequest.builder()
                .roomId(room.getId())
                .build();
        SessionMember sessionMember = SessionMember.builder()
                .memberSeq(owner.getId())
                .profile(owner.getProfile())
                .nickName(owner.getNickName())
                .build();

        // when
        Boolean result = roomDeleteService.removeRoom(sessionMember, roomRemoveRequest);

        // then
        assertThat(result).isTrue();
        assertThat(roomRepository.count()).isEqualTo(0);
        assertThat(partRepository.count()).isEqualTo(0);
        assertThat(topicRepository.count()).isEqualTo(0);
        assertThat(participationRepository.count()).isEqualTo(0);
        assertThat(reviewRepository.count()).isEqualTo(0);
        assertThat(fileRepository.count()).isEqualTo(0);
        assertThat(scriptRepository.count()).isEqualTo(0);
        assertThat(presentationRepository.count()).isEqualTo(0);
        assertThat(slideRepository.count()).isEqualTo(0);
    }
}
