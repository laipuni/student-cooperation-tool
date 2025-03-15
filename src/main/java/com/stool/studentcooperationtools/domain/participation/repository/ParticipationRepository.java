package com.stool.studentcooperationtools.domain.participation.repository;

import com.stool.studentcooperationtools.domain.participation.Participation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    @Modifying
    @Query("delete from Participation p where p.room.id =:roomId")
    void deleteByRoomId(@Param("roomId") Long roomId);

    Boolean existsByMemberIdAndRoomId(Long memberId, Long roomId);
    Boolean existsByRoomId(Long roomId);
    @Modifying
    @Query("delete from Participation p where p.room.id =:roomId and p.member.id = :memberId")
    void deleteByMemberIdAndRoomId(@Param("memberId") Long memberId, @Param("roomId") Long roomId);

    Participation findByMemberIdAndRoomId(Long memberId, Long roomId);
}
