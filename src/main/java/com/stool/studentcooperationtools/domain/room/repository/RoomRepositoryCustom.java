package com.stool.studentcooperationtools.domain.room.repository;

import com.stool.studentcooperationtools.domain.room.controller.response.RoomSearchResponse;
import com.stool.studentcooperationtools.domain.room.controller.response.RoomsFindResponse;

public interface RoomRepositoryCustom {

    RoomsFindResponse findRoomsByMemberIdWithPagination(final Long memberId, final int page);
    RoomSearchResponse findRooms(final String title, final int page, final boolean isParticipation, final Long memberId);

}
