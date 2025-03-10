package com.liushukov.testTask.repository;

import com.liushukov.testTask.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {
    @Query("SELECT p FROM Patient p WHERE p.id = :id")
    Optional<Patient> findPatientById(@Param("id") int id);

    @Query("""
            SELECT DISTINCT p.id FROM Patient p
            LEFT JOIN p.visits v
            WHERE (:search IS NULL OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY p.id
            """)
    Page<Integer> findPatientIdsBySearch(@Param("search") String search, Pageable pageable);

    @Query("""
            SELECT DISTINCT p.id FROM Patient p
            LEFT JOIN p.visits v
            LEFT JOIN v.doctor d
            WHERE (:search IS NULL OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%')))
            AND d.id IN :doctorIds
            ORDER BY p.id
            """)
    Page<Integer> findPatientIdsBySearchAndDoctorIds(@Param("search") String search,
                                                     @Param("doctorIds") List<Integer> doctorIds,
                                                     Pageable pageable);

    @Query("""
            SELECT DISTINCT p FROM Patient p
            LEFT JOIN FETCH p.visits v
            LEFT JOIN FETCH v.doctor d
            WHERE p.id IN :ids
            AND d.id IN :doctorIds
            AND v.endDateTime = (
                SELECT MAX(v2.endDateTime) FROM Visit v2
                WHERE v2.patient.id = p.id AND v2.doctor.id = d.id
            )
            ORDER BY p.id
            """)
    List<Patient> findPatientsWithVisitsAndDoctorsByIds(@Param("ids") List<Integer> ids, List<Integer> doctorIds);

    @Query("""
            SELECT DISTINCT p FROM Patient p
            LEFT JOIN FETCH p.visits v
            LEFT JOIN FETCH v.doctor d
            WHERE p.id IN :ids
            AND (
                v.id IS NULL
                OR v.endDateTime = (
                    SELECT MAX(v2.endDateTime) FROM Visit v2
                    WHERE v2.patient.id = p.id AND v2.doctor.id = d.id
                )
            )
            ORDER BY p.id
            """)
    List<Patient> findPatientsWithVisitsAndDoctors(@Param("ids") List<Integer> ids);
}
