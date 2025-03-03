package com.liushukov.testTask.service.impl;

import com.liushukov.testTask.entity.Doctor;
import com.liushukov.testTask.repository.DoctorRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public Optional<Doctor> getDoctorById(int id) {
        return doctorRepository.findDoctorById(id);
    }
}
