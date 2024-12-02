package com.stool.studentcooperationtools.domain.slide;

import com.stool.studentcooperationtools.domain.BaseTimeEntity;
import com.stool.studentcooperationtools.domain.presentation.Presentation;
import com.stool.studentcooperationtools.domain.script.Script;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Slide extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String thumbnail;

    @Column(nullable = false)
    private String slideUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    private Presentation presentation;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Script script;

    @Column
    private int index;

    @Builder
    private Slide(
            final String thumbnail, final Presentation presentation,
            final Script script,final String slideUrl, final int index) {
        this.thumbnail = thumbnail;
        this.presentation = presentation;
        this.script = script;
        this.slideUrl = slideUrl;
        this.index = index;
    }


    public void updateIndex(int index) {
        this.index = index;
    }
}
