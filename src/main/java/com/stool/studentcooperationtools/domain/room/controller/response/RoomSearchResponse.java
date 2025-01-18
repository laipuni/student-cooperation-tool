package com.stool.studentcooperationtools.domain.room.controller.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

import static com.stool.studentcooperationtools.domain.PagingUtils.getEndPage;
import static com.stool.studentcooperationtools.domain.PagingUtils.getRoomPagingStartPage;

@Getter
public class RoomSearchResponse {

    private int num;
    private int totalPage;
    private int firstPage;
    private int lastPage;
    private List<RoomSearchDto> rooms;

    @Builder
    private RoomSearchResponse(final int num, final int totalPage, final int firstPage, final int lastPage, final List<RoomSearchDto> rooms) {
        this.num = num;
        this.firstPage = firstPage;
        this.lastPage = lastPage;
        this.totalPage = totalPage;
        this.rooms = rooms;
    }


    public static RoomSearchResponse of(final Page<RoomSearchDto> paginationResult) {
        int startPage = getRoomPagingStartPage(paginationResult.getNumber());
        int endPage = getEndPage(startPage,paginationResult.getTotalPages());

        return RoomSearchResponse.builder()
                .num((int)paginationResult.getTotalElements())
                .firstPage(startPage)
                .lastPage(endPage)
                .totalPage(paginationResult.getTotalPages())
                .rooms(paginationResult.getContent())
                .build();
    }
}
