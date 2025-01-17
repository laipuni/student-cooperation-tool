package com.stool.studentcooperationtools.domain.room.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stool.studentcooperationtools.domain.room.controller.response.*;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

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
    public RoomSearchResponse searchRoomsBy(final String title, final int page, final boolean isParticipation, final Long memberId) {
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
                        filterBy(isParticipation,title)
                )
                .orderBy(room.title.desc(),room.id.desc())
                .offset(page)
                .limit(ROOM_PAGE_SIZE)
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(room.count())
                .from(room)
                .leftJoin(participation)
                .on(participation.member.id.eq(memberId).and(participation.room.id.eq(room.id)))
                .where(
                        filterBy(isParticipation,title)
                );

        PageRequest pageRequest = PageRequest.of(page, ROOM_PAGE_SIZE);

        Page<RoomSearchDto> paginationResult = PageableExecutionUtils.getPage(
                result, pageRequest, countQuery::fetchOne
        );

        return RoomSearchResponse.of(paginationResult);
    }

    private BooleanBuilder filterBy(final boolean isParticipation,final String title){
        return filterWithParticipation(isParticipation)
                .and(filterWithTitle(title));
    }

    private static BooleanBuilder filterWithTitle(final String title){
        if(StringUtils.hasText(title)){
            return new BooleanBuilder(room.title.contains(title));
        } else{
            return new BooleanBuilder();
        }
    }

    private static BooleanBuilder filterWithParticipation(final boolean isParticipation) {
        if(isParticipation){
            return new BooleanBuilder(participation.isNotNull());
        } else{
            return new BooleanBuilder(participation.isNull());
        }
    }
}
