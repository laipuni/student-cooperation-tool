package com.stool.studentcooperationtools.domain.member;

import com.stool.studentcooperationtools.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String nickName;

    @Column(nullable = false)
    private String profile;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder
    private Member(final String nickName,final String email, final String profile, final Role role) {
        this.nickName = nickName;
        this.email = email;
        this.profile = profile;
        this.role = role;
    }

}
