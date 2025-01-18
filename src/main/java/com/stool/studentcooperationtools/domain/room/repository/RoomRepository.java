package com.stool.studentcooperationtools.domain.room.repository;

import com.stool.studentcooperationtools.domain.room.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long>, RoomRepositoryCustom{
    @Query("select r from Room r join Participation p on p.member.id = :memberId and p.room.id = r.id order by r.id desc")
    Page<Room> findRoomsByMemberIdWithPage(@Param("memberId") Long memberId, Pageable pageable);

    @Query("select r from Room r where r.title like %:title% order by r.id desc")
    Page<Room> findRoomsByTitleWithPage(@Param("title") String title, Pageable pageable);

    @Query("select r from Room r join Participation p on p.member.id = :memberId where r.id = :roomId")
    Optional<Room> findRoomByRoomId(@Param("memberId") Long memberId, @Param("roomId") Long roomId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r where r.id = :roomId")
    Optional<Room> findRoomWithPLock(@Param("roomId") Long roomId);

    @Query("select exists(select 1 from Participation p where p.member.id = :memberId and p.room.id = :roomId)")
    Boolean existMemberInRoom(@Param("memberId") Long memberId, @Param("roomId") Long roomId);

    @Query("select exists(select 1 from Participation p where p.member.email = :email and p.room.id = :roomId)")
    Boolean existMemberInRoom(@Param("email") String email, @Param("roomId") Long roomId);
}

