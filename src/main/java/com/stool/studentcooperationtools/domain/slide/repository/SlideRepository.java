package com.stool.studentcooperationtools.domain.slide.repository;

import com.stool.studentcooperationtools.domain.slide.Slide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface SlideRepository extends JpaRepository<Slide, Long> {

    @Query(value = "select s from Slide s join fetch s.script where s.presentation.id = :presentationId order by s.slide_idx")
    List<Slide> findSlidesAndScriptsByPresentationId(@Param("presentationId") Long presentationId);

    @Modifying
    @Transactional
    @Query(value = "delete from Slide s where s.presentation.id = :presentationId")
    void deleteByPresentationId(@Param("presentationId") Long presentationId);

    @Query(value = "select s from Slide s where s.slide_idx = 0 and s.presentation.id = :presentationId")
    Optional<Slide> findFirstByPresentationId(@Param("presentationId") Long presentationId);


}
