package com.stool.studentcooperationtools.domain.script.repository;

import com.stool.studentcooperationtools.domain.script.Script;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


public interface ScriptRepository extends JpaRepository<Script, Long> {

    @Modifying
    @Query("delete from Script s where s.presentation.id =:presentationId")
    void deleteByPresentationId(@Param("presentationId") Long presentationId);
}
