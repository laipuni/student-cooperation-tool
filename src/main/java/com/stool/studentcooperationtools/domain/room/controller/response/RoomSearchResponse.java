package com.stool.studentcooperationtools.domain.room.controller.response;

import com.stool.studentcooperationtools.domain.room.Room;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class RoomSearchResponse {

    private int num;
    private boolean isLast;
    private List<RoomSearchDto> rooms;

    @Builder
    private RoomSearchResponse(final int num, final boolean isLast, final List<RoomSearchDto> rooms) {
        this.num = num;
        this.isLast = isLast;
        this.rooms = rooms;
    }

    public static RoomSearchResponse of(final boolean isLast, final List<Room> rooms){
        return RoomSearchResponse.builder()
                .num(rooms.size())
                .isLast(isLast)
                .rooms(
                        rooms.stream()
                                .map(RoomSearchDto::of)
                                .toList()
                )
                .build();
    }

    public static RoomSearchResponse of(final boolean isLast, final List<RoomSearchDto> roomDtos, @Nullable final List<Room> rooms) {
        return RoomSearchResponse.builder()
                .num(roomDtos.size())
                .isLast(isLast)
                .rooms(roomDtos)
                .build();
    }
}
