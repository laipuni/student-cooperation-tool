package com.stool.studentcooperationtools.domain.room.repository;

import com.stool.studentcooperationtools.domain.room.controller.response.RoomSearchResponse;

public interface RoomRepositoryCustom {

    RoomSearchResponse searchRoomsBy(final String title, final int page, final boolean isParticipation, final Long memberId);

}
