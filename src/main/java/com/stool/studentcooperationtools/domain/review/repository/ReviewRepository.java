package com.stool.studentcooperationtools.domain.review.repository;

import com.stool.studentcooperationtools.domain.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("select r from Review r where r.part.id = :partId order by r.id desc")
    List<Review> findAllByPartIdOrderByIdDesc(@Param("partId") Long partId);

}