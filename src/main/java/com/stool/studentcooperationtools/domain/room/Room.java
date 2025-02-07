package com.stool.studentcooperationtools.domain.room;

import com.stool.studentcooperationtools.domain.BaseTimeEntity;
import com.stool.studentcooperationtools.domain.member.Member;
import com.stool.studentcooperationtools.domain.participation.Participation;
import com.stool.studentcooperationtools.domain.room.controller.request.RoomAddRequest;
import com.stool.studentcooperationtools.domain.topic.Topic;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true,length = 20)
    private String title;

    @Column(nullable = false, length = 20)
    private String password;

    @OneToMany(mappedBy = "room")
    private List<Participation> participationList = new ArrayList<>();

    @Column
    private int participationNum;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member leader;

    @OneToOne(fetch = FetchType.LAZY,cascade = CascadeType.PERSIST, orphanRemoval = true)
    private Topic mainTopic;

    @Builder
    private Room(final String title, final String password, final int participationNum, final Member leader) {
        this.title = title;
        this.password = password;
        this.participationNum = participationNum;
        this.leader = leader;
    }

    public static Room of(final RoomAddRequest request, final Member leader) {
        return Room.builder()
                .leader(leader)
                .password(request.getPassword())
                .title(request.getTitle())
                .participationNum(0)
                .build();
    }

    public String getTopic(){
        if(mainTopic == null){
            return "미정";
        }
        return this.mainTopic.getTopic();
    }

    public void addParticipation(Participation participation){
        participationList.add(participation);
        addParticipantNum();
    }

    public void deleteParticipation(Participation participation){
        participationList.remove(participation);
        minusParticipantNum();
    }

    public Boolean verifyPassword(String password){
        return this.password.equals(password);
    }

    public void updateTopic(Topic topic){
        this.mainTopic = topic;
    }

    public void addParticipantNum(){
        this.participationNum++;
    }

    public void minusParticipantNum(){
        this.participationNum--;
    }

    public boolean isLeader(final Long memberId){
        return this.leader.getId().equals(memberId);
    }
}
