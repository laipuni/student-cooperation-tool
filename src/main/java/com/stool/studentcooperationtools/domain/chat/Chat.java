package com.stool.studentcooperationtools.domain.chat;

import com.stool.studentcooperationtools.domain.BaseTimeEntity;
import com.stool.studentcooperationtools.domain.member.Member;
import com.stool.studentcooperationtools.domain.room.Room;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Chat extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private Room room;

    @Builder
    private Chat(final String content, final Member member, final Room room) {
        this.content = content;
        this.member = member;
        this.room = room;
    }

    public static Chat of(final String content, final Member member, final Room room){
        return Chat.builder()
                .content(content)
                .member(member)
                .room(room)
                .build();
    }

    public boolean isWrittenBy(final Long memberId){
        return this.member.getId().equals(memberId);
    }

}
