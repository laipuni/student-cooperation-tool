package com.stool.studentcooperationtools.domain.room.controller.response;

import com.stool.studentcooperationtools.domain.room.Room;
import lombok.Builder;
import lombok.Getter;

@Getter
public class RoomAddResponse {
    private Long roomId;
    private String title;
    @Builder
    private RoomAddResponse(final Long roomId, final String title) {
        this.roomId = roomId;
        this.title = title;
    }

    public static RoomAddResponse of(final Room room){
        return RoomAddResponse.builder()
                .roomId(room.getId())
                .title(room.getTitle())
                .build();
    }
}
