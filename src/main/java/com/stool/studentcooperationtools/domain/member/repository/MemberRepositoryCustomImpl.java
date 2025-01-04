package com.stool.studentcooperationtools.domain.member.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stool.studentcooperationtools.domain.member.controller.request.MemberSearchMemberDto;
import com.stool.studentcooperationtools.domain.member.controller.request.QMemberSearchMemberDto;
import jakarta.persistence.EntityManager;

import java.util.List;

import static com.stool.studentcooperationtools.domain.friendship.QFriendship.friendship;
import static com.stool.studentcooperationtools.domain.member.QMember.member;

public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public MemberRepositoryCustomImpl(final EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public List<MemberSearchMemberDto> findFriendsByMemberNickName(final String nickName, final Long memberId, final boolean isFriendRelation) {
        return queryFactory.select(
                        new QMemberSearchMemberDto(
                                member.nickName,
                                member.profile,
                                member.id
                        )
                )
                .from(member)
                .leftJoin(friendship)
                .on(friendship.me.id.eq(memberId).and(friendship.friend.id.eq(member.id)))
                .where(
                        member.nickName.contains(nickName).and(member.id.ne(memberId)),
                        filterBy(isFriendRelation)
                )
                .orderBy(member.nickName.asc(), member.id.asc()) // 가나다라 순으로, 만약 같다면 가입 순으로
                .fetch();
    }

    private BooleanBuilder filterBy(final boolean isFriendRelation){
        if(isFriendRelation){
            //친구 관계인 유저를 검색할 경우
            return new BooleanBuilder(friendship.isNotNull());
        } else{
            //친구 관계가 아닌 유저를 검색할 경우
            return new BooleanBuilder(friendship.isNull());
        }
    }
}
