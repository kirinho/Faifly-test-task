package com.liushukov.testTask.repository;

import com.liushukov.testTask.entity.Doctor;
import com.liushukov.testTask.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Integer> {
    @Query("SELECT v FROM Visit v WHERE v.doctor = :doctor AND v.startDateTime <= :endDateTime AND v.endDateTime >= :startDateTime")
    List<Visit> findOverlappingVisits(@Param("doctor") Doctor doctor, @Param("endDateTime") LocalDateTime endDateTime,
            @Param("startDateTime") LocalDateTime startDateTime);
}
