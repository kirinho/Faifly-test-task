package com.liushukov.testTask.controller;

import com.liushukov.testTask.dto.VisitCreateDto;
import com.liushukov.testTask.entity.Doctor;
import com.liushukov.testTask.entity.Patient;
import com.liushukov.testTask.entity.Visit;
import com.liushukov.testTask.exceptions.CustomException;
import com.liushukov.testTask.service.PatientService;
import com.liushukov.testTask.service.VisitService;
import com.liushukov.testTask.service.impl.DoctorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping(path = "/visits")
public class VisitController {
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final VisitService visitService;

    public VisitController(DoctorService doctorService, PatientService patientService, VisitService visitService) {
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.visitService = visitService;
    }

    @PostMapping(path = "/visit/create")
    public ResponseEntity<Visit> createVisit(@Valid @RequestBody VisitCreateDto visitCreateDto) throws CustomException {
        Optional<Doctor> doctor = doctorService.getDoctorById(visitCreateDto.doctorId());
        Optional<Patient> patient = patientService.getPatientById(visitCreateDto.patientId());
        if (doctor.isPresent() && patient.isPresent()) {
            Visit visit = visitService.createVisit(visitCreateDto, doctor.get(), patient.get());
            return ResponseEntity.status(HttpStatus.CREATED).body(visit);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
