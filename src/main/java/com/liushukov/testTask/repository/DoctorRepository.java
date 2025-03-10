package com.liushukov.testTask.repository;

import com.liushukov.testTask.dto.DoctorPatientCountProjection;
import com.liushukov.testTask.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {
    @Query("SELECT d FROM Doctor d WHERE d.id = :id")
    Optional<Doctor> findDoctorById(@Param("id") int id);

    @Query("""
            SELECT d.id AS doctorId, COUNT(DISTINCT v.patient.id) AS totalPatients
            FROM Doctor d
            LEFT JOIN Visit v ON d.id = v.doctor.id
            WHERE d.id IN :doctorIds
            GROUP BY d.id
            """)
    List<DoctorPatientCountProjection> countTotalPatientsPerDoctor(List<Integer> doctorIds);
}
