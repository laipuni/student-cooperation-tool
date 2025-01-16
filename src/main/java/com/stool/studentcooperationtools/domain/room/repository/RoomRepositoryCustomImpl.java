package com.stool.studentcooperationtools.domain.room.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stool.studentcooperationtools.domain.room.controller.response.*;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.stool.studentcooperationtools.domain.participation.QParticipation.participation;
import static com.stool.studentcooperationtools.domain.room.QRoom.room;


public class RoomRepositoryCustomImpl implements RoomRepositoryCustom{

    public static final int ROOM_PAGE_SIZE = 5;

    private final JPAQueryFactory queryFactory;

    public RoomRepositoryCustomImpl(final EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public RoomsFindResponse findRoomsByMemberIdWithPagination(final Long memberId, final int page) {
        List<RoomFindDto> result = queryFactory.select(
                        new QRoomFindDto(
                                room.id,
                                room.title,
                                room.mainTopic.topic,
                                room.participationNum
                        )
                )
                .from(room)
                .leftJoin(room.mainTopic)
                .where(room.leader.id.eq(memberId))
                .orderBy(room.title.desc(), room.id.desc())
                .offset(page)
                .limit(ROOM_PAGE_SIZE)
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(room.count())
                .from(room)
                .where(room.leader.id.eq(memberId));

        PageRequest pageRequest = PageRequest.of(page, ROOM_PAGE_SIZE);

        Page<RoomFindDto> paginationResult = PageableExecutionUtils.getPage(
                result, pageRequest, countQuery::fetchOne
        );

        return RoomsFindResponse.of(paginationResult);
    }

    @Override
    public RoomSearchResponse findRooms(final String title, final int page, final boolean isParticipation, final Long memberId) {
        List<RoomSearchDto> result = queryFactory.select(
                        new QRoomSearchDto(
                                room.id,
                                room.title,
                                room.mainTopic.topic,
                                room.participationNum
                        )
                )
                .from(room)
                .leftJoin(room.mainTopic)
                .leftJoin(participation)
                .on(participation.member.id.eq(memberId).and(participation.room.id.eq(room.id)))
                .where(
                        room.title.contains(title),
                        filterBy(isParticipation)
                )
                .orderBy(room.title.desc(),room.id.desc())
                .offset(page)
                .limit(ROOM_PAGE_SIZE + 1)
                .fetch();

        boolean isLast = result.size() <= ROOM_PAGE_SIZE;

        return RoomSearchResponse.of(isLast,result,null);
    }

    private BooleanBuilder filterBy(final boolean isParticipation){
        if(isParticipation){
            return new BooleanBuilder(participation.isNotNull());
        } else{
            return new BooleanBuilder(participation.isNull());
        }
    }
}
