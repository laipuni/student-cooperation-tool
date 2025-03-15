package com.stool.studentcooperationtools.domain.presentation.repository;

import com.stool.studentcooperationtools.domain.presentation.Presentation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface PresentationRepository extends JpaRepository<Presentation, Long> {

    @Query("select p.id from Presentation p where p.room.id = :roomId")
    Long findPresentationIdByRoomId(@Param("roomId") Long roomId);

    Optional<Presentation> findByRoomId(Long roomId);

    Boolean existsByRoomId(Long roomId);

    @Modifying
    @Query("delete from Presentation p where p.room.id =:roomId")
    void deleteByRoomId(@Param("roomId") Long roomId);

}