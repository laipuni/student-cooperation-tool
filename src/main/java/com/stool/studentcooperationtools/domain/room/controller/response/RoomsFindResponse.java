package com.stool.studentcooperationtools.domain.room.controller.response;

import com.stool.studentcooperationtools.domain.room.Room;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

import static com.stool.studentcooperationtools.domain.PagingUtils.*;

@Getter
public class RoomsFindResponse {

    private int num;
    private int totalPage;
    private int firstPage;
    private int lastPage;
    private List<RoomFindDto> rooms;

    @Builder
    private RoomsFindResponse(final int num, final int totalPage, final int firstPage, final int lastPage, final List<RoomFindDto> rooms) {
        this.num = num;
        this.firstPage = firstPage;
        this.lastPage = lastPage;
        this.totalPage = totalPage;
        this.rooms = rooms;
    }

    public static RoomsFindResponse of(final int num,final int nowPage, final int totalPage, final List<Room> rooms){
        int roomPagingStartPage = getRoomPagingStartPage(nowPage);
        return RoomsFindResponse.builder()
                .num(num)
                .firstPage(roomPagingStartPage)
                .lastPage(getEndPage(roomPagingStartPage,totalPage))
                .totalPage(totalPage)
                .rooms(
                        rooms.stream()
                                .map(RoomFindDto::of)
                                .toList()
                )
                .build();
    }

    public static RoomsFindResponse of(final Page<RoomFindDto> paginationResult) {
        int startPage = getRoomPagingStartPage(paginationResult.getNumber());
        int endPage = getRoomPagingLastPage(startPage,paginationResult.getTotalPages());

        return RoomsFindResponse.builder()
                .num(paginationResult.getContent().size())
                .firstPage(startPage)
                .lastPage(endPage)
                .totalPage(paginationResult.getTotalPages())
                .rooms(
                        paginationResult.getContent()
                )
                .build();
    }
}
