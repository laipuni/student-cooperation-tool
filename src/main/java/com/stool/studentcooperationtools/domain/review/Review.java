package com.stool.studentcooperationtools.domain.review;

import com.stool.studentcooperationtools.domain.BaseTimeEntity;
import com.stool.studentcooperationtools.domain.member.Member;
import com.stool.studentcooperationtools.domain.part.Part;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private Part part;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Builder
    private Review(final String content, final Part part, final Member member) {
        this.content = content;
        this.part = part;
        this.member = member;
    }

    public static Review of(final String content, final Part part, final Member member){
        return Review.builder()
                .content(content)
                .part(part)
                .member(member)
                .build();
    }

}
