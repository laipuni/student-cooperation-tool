package com.stool.studentcooperationtools.domain.room.controller.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class RoomEnterResponse {
    private Long leaderId;

    @Builder
    private RoomEnterResponse(final Long leaderId) {
        this.leaderId = leaderId;
    }

    public static RoomEnterResponse of(final Long memberId){
        return RoomEnterResponse.builder()
                .leaderId(memberId)
                .build();
    }

}
