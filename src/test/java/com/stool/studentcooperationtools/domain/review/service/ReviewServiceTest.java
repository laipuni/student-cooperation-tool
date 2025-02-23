package com.stool.studentcooperationtools.domain.review.service;

import com.stool.studentcooperationtools.IntegrationTest;
import com.stool.studentcooperationtools.domain.member.Member;
import com.stool.studentcooperationtools.domain.member.Role;
import com.stool.studentcooperationtools.domain.member.repository.MemberRepository;
import com.stool.studentcooperationtools.domain.part.Part;
import com.stool.studentcooperationtools.domain.part.repository.PartRepository;
import com.stool.studentcooperationtools.domain.review.Review;
import com.stool.studentcooperationtools.domain.review.controller.request.ReviewAddRequest;
import com.stool.studentcooperationtools.domain.review.controller.request.ReviewDeleteRequest;
import com.stool.studentcooperationtools.domain.review.controller.response.ReviewAddResponse;
import com.stool.studentcooperationtools.domain.review.controller.response.ReviewFindResponse;
import com.stool.studentcooperationtools.domain.review.repository.ReviewRepository;
import com.stool.studentcooperationtools.domain.room.Room;
import com.stool.studentcooperationtools.domain.room.repository.RoomRepository;
import com.stool.studentcooperationtools.exception.global.UnAuthorizationException;
import com.stool.studentcooperationtools.security.oauth2.dto.SessionMember;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;


@Transactional
class ReviewServiceTest extends IntegrationTest {

    @Autowired
    ReviewService reviewService;

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    PartRepository partRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    RoomRepository roomRepository;

    @DisplayName("역할의 id로 리뷰들을 최신순으로 조회한다.")
    @Test
    void findAllByPartOrderByIdIdAsc(){
        //given
        Member member = Member.builder()
                .email("email")
                .nickName("닉네임")
                .profile("profile")
                .role(Role.USER)
                .build();

        Member anotherMember = Member.builder()
                .email("평가자 이메일")
                .nickName("평가자")
                .profile("평가자 프로필")
                .role(Role.USER)
                .build();

        memberRepository.saveAll(List.of(anotherMember,member));
        Room room = Room.builder()
                .password("password")
                .title("제목")
                .leader(member)
                .participationNum(1)
                .build();
        roomRepository.save(room);

        String content = "조사할 부분";
        Part part = Part.builder()
                .partName(content)
                .room(room)
                .member(member)
                .build();
        partRepository.save(part);

        String content1 = "평가 댓글1";
        String content2 = "평가 댓글2";
        String content3 = "평가 댓글3";
        reviewRepository.save(
                Review.builder()
                        .content(content1)
                        .member(anotherMember)
                        .part(part)
                        .build()
        );
        reviewRepository.save(
                Review.builder()
                        .content(content2)
                        .member(anotherMember)
                        .part(part)
                        .build()
        );
        reviewRepository.save(
                Review.builder()
                        .content(content3)
                        .member(anotherMember)
                        .part(part)
                        .build()
        );

        //when
        ReviewFindResponse response = reviewService.findReview(part.getId());

        //then
        assertThat(response.getNum()).isEqualTo(3);
        assertThat(response.getReviews()).hasSize(3)
                .extracting("content","memberId","nickName","profile")
                .containsExactly(
                        tuple(content3,anotherMember.getId(),anotherMember.getNickName(),anotherMember.getProfile()),
                        tuple(content2,anotherMember.getId(),anotherMember.getNickName(),anotherMember.getProfile()),
                        tuple(content1,anotherMember.getId(),anotherMember.getNickName(),anotherMember.getProfile())
                );
    }

    @DisplayName("리뷰를 등록할 때, 등록하는 유저의 정보가 유효하지 않을 경우 에러가 발생한다")
    @Test
    void addReviewWithNotExistMember(){
        //given
        Member member = Member.builder()
                .email("email")
                .nickName("닉네임")
                .profile("profile")
                .role(Role.USER)
                .build();
        memberRepository.save(member);

        Room room = Room.builder()
                .password("password")
                .title("제목")
                .leader(member)
                .participationNum(1)
                .build();
        roomRepository.save(room);

        String content = "조사할 부분";
        Part part = Part.builder()
                .partName(content)
                .room(room)
                .member(member)
                .build();
        partRepository.save(part);

        ReviewAddRequest request = ReviewAddRequest.builder()
                .partId(1L)
                .content("평가 내용")
                .build();

        Long invalidMemberId = 2024L;
        String invalidNickName = "invalidNickName";
        String invalidProfile = "invalidProfile";

        SessionMember sessionMember = SessionMember.builder()
                .memberSeq(invalidMemberId)
                .nickName(invalidNickName)
                .profile(invalidProfile)
                .build();
        //when
        //then
        assertThatThrownBy(() -> reviewService.addReview(request, sessionMember))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageMatching("해당 유저는 존재하지 않습니다.");
    }


    @DisplayName("리뷰를 등록 요청 값들을 받아 리뷰를 등록한다.")
    @Test
    void addReview(){
        //given
        Member member = Member.builder()
                .email("email")
                .nickName("닉네임")
                .profile("profile")
                .role(Role.USER)
                .build();
        memberRepository.save(member);

        Room room = Room.builder()
                .password("password")
                .title("제목")
                .leader(member)
                .participationNum(1)
                .build();
        roomRepository.save(room);

        String content = "조사할 부분";
        Part part = Part.builder()
                .partName(content)
                .room(room)
                .member(member)
                .build();
        partRepository.save(part);

        ReviewAddRequest request = ReviewAddRequest.builder()
                .partId(part.getId())
                .content("평가 내용")
                .build();

        SessionMember sessionMember = SessionMember.builder()
                .memberSeq(member.getId())
                .nickName(member.getNickName())
                .profile(member.getProfile())
                .build();
        //when
        ReviewAddResponse response = reviewService.addReview(request, sessionMember);

        //then
        assertThat(response).isNotNull()
                .extracting("content","nickName","profile")
                .containsExactlyInAnyOrder(
                        request.getContent(),member.getNickName(),member.getProfile()
                );
    }

    @DisplayName("리뷰를 등록할 때, 등록할 역할이 존재하지 않을 경우 에러가 발생한다")
    @Test
    void addReviewWithNotExistPart(){
        //given
        Member member = Member.builder()
                .email("email")
                .nickName("닉네임")
                .profile("profile")
                .role(Role.USER)
                .build();
        memberRepository.save(member);

        Room room = Room.builder()
                .password("password")
                .title("제목")
                .leader(member)
                .participationNum(1)
                .build();
        roomRepository.save(room);

        String content = "조사할 부분";
        Part part = Part.builder()
                .partName(content)
                .room(room)
                .member(member)
                .build();
        partRepository.save(part);


        Long invalidPartId = 2024L;
        ReviewAddRequest request = ReviewAddRequest.builder()
                .partId(invalidPartId)
                .content("평가 내용")
                .build();

        SessionMember sessionMember = SessionMember.builder()
                .memberSeq(member.getId())
                .nickName(member.getNickName())
                .profile(member.getProfile())
                .build();
        //when
        //then
        assertThatThrownBy(() -> reviewService.addReview(request, sessionMember))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageMatching("해당 역할이 존재하지 않습니다.");
    }

    @DisplayName("리뷰를 삭제할 때, 본인이 작성한 리뷰가 아니면 삭제할 수 없다.")
    @Test
    void deleteReviewByNotOwner(){
        //given
        Member leader = Member.builder()
                .email("email")
                .nickName("닉네임")
                .profile("profile")
                .role(Role.USER)
                .build();
        Member owner = Member.builder()
                .email("email")
                .nickName("닉네임")
                .profile("profile")
                .role(Role.USER)
                .build();
        memberRepository.saveAll(List.of(leader,owner));

        Room room = Room.builder()
                .password("password")
                .title("제목")
                .leader(leader)
                .participationNum(3)
                .build();
        roomRepository.save(room);

        String content = "조사할 부분";
        Part part = Part.builder()
                .partName(content)
                .room(room)
                .member(leader)
                .build();
        partRepository.save(part);

        Review review = Review.builder()
                .content("평가 내용")
                .member(owner)
                .part(part)
                .build();
        reviewRepository.save(review);

        ReviewDeleteRequest request = ReviewDeleteRequest.builder()
                .reviewId(review.getId())
                .build();

        Long invalidMemberId = 2024L;
        String invalidNickName = "invalidNickName";
        String invalidProfile = "invalidProfile";
        SessionMember sessionMember = SessionMember.builder()
                .memberSeq(invalidMemberId)
                .nickName(invalidNickName)
                .profile(invalidProfile)
                .build();
        //when
        //then
        assertThatThrownBy(() -> reviewService.deleteReview(request, sessionMember))
                .isInstanceOf(UnAuthorizationException.class)
                .hasMessageMatching("평가를 제거할 권한이 없습니다.");
    }


}