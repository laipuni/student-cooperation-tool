package com.stool.studentcooperationtools.domain.review.service;

import com.stool.studentcooperationtools.domain.member.Member;
import com.stool.studentcooperationtools.domain.member.repository.MemberRepository;
import com.stool.studentcooperationtools.domain.part.Part;
import com.stool.studentcooperationtools.domain.part.repository.PartRepository;
import com.stool.studentcooperationtools.domain.review.Review;
import com.stool.studentcooperationtools.domain.review.controller.request.ReviewAddRequest;
import com.stool.studentcooperationtools.domain.review.controller.request.ReviewDeleteRequest;
import com.stool.studentcooperationtools.domain.review.controller.response.ReviewAddResponse;
import com.stool.studentcooperationtools.domain.review.controller.response.ReviewFindResponse;
import com.stool.studentcooperationtools.domain.review.repository.ReviewRepository;
import com.stool.studentcooperationtools.exception.global.UnAuthorizationException;
import com.stool.studentcooperationtools.security.oauth2.dto.SessionMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final PartRepository partRepository;

    public ReviewFindResponse findReview(final Long partId) {
        List<Review> result = reviewRepository.findAllByPartIdOrderByIdDesc(partId);
        return ReviewFindResponse.of(result);
    }

    @Transactional
    public ReviewAddResponse addReview(final ReviewAddRequest request, final SessionMember sessionMember) {
        Member member = memberRepository.findById(sessionMember.getMemberSeq())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저는 존재하지 않습니다."));

        Part part = partRepository.findById(request.getPartId())
                .orElseThrow(() -> new IllegalArgumentException("해당 역할이 존재하지 않습니다."));
        Review review = reviewRepository.save(Review.of(request.getContent(),part,member));
        log.info("사용자(id : {})가 역할(id : {})에 리뷰(id :{})를 작성했다.",
                sessionMember.getMemberSeq(),request.getPartId(),review.getId());
        return ReviewAddResponse.of(review);
    }

    @Transactional(rollbackFor = AccessDeniedException.class)
    public Boolean deleteReview(final ReviewDeleteRequest request, final SessionMember sessionMember) {
        int result = reviewRepository.deleteReviewByMemberIdAndReviewId(request.getReviewId(), sessionMember.getMemberSeq());
        if(result == 0){
            //본인이 작성하지 않은 리뷰이거나 방장이 아닌 경우
            log.debug("사용자(id : {})가 리뷰(id : {})를 삭제할 권한이 없어 리뷰 삭제를 실패했다."
                    ,sessionMember.getMemberSeq(),request.getReviewId());
            throw new UnAuthorizationException("평가를 제거할 권한이 없습니다.");
        }
        log.info("사용자(id : {})가 리뷰(id : {})를 삭제했다.",sessionMember.getMemberSeq(),request.getReviewId());
        return true;
    }
}
