package com.stool.studentcooperationtools.domain.member.repository;

import com.stool.studentcooperationtools.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom{
    Optional<Member> findMemberByEmail(String email);
    @Query("select m from Member m where m.id in :memberIds")
    List<Member> findMembersByMemberIdList(@Param("memberIds") List<Long> memberIds);

    @Query(value = "select m from Member m join Participation p on p.member.id = m.id where p.room.id = :roomId")
    List<Member> findAllByRoomId(@Param("roomId") Long roomId);
}
