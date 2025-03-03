package com.liushukov.testTask.service;

import com.liushukov.testTask.dto.PatientResponse;
import com.liushukov.testTask.entity.Patient;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface PatientService {
    Optional<Patient> getPatientById(int id);

    PatientResponse getAllPatients(int page, int size, String search, List<Integer> ids);
}
