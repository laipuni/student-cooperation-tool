package com.stool.studentcooperationtools.domain.part;

import com.stool.studentcooperationtools.domain.BaseTimeEntity;
import com.stool.studentcooperationtools.domain.file.File;
import com.stool.studentcooperationtools.domain.member.Member;
import com.stool.studentcooperationtools.domain.room.Room;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Part extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String partName;

    @ManyToOne(fetch = FetchType.LAZY)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @BatchSize(size = 10)
    @OneToMany(mappedBy = "part", cascade = CascadeType.ALL)
    private List<File> fileList = new ArrayList<>();

    @Builder
    private Part(final String partName, final Room room, final Member member) {
        this.partName = partName;
        this.room = room;
        this.member = member;
    }

    public void addFile(File file){
        this.fileList.add(file);
    }

    public void changeMember(Member member){
        this.member = member;
    }

    public static Part of(final String partName, final Member member, final Room room){
        return Part.builder()
                .partName(partName)
                .member(member)
                .room(room)
                .build();
    }

    public void update(final String partName){
        if(!StringUtils.hasText(partName)){
            //이름이 빈칸 혹은 null값일 경우
            throw new IllegalArgumentException("역할 이름은 필수입니다.");
        }
        this.partName = partName;
    }

    public void update(final Member member, final String partName){
        if(member != null){
            changeMember(member);
        }
        this.update(partName);
    }
}
