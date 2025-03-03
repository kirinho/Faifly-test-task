package com.liushukov.testTask.controller;

import com.liushukov.testTask.dto.PatientResponse;
import com.liushukov.testTask.entity.Patient;
import com.liushukov.testTask.service.PatientService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(path = "/patients")
public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping(path = "/all")
    public ResponseEntity<PatientResponse> all(
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
            @RequestParam(value = "size", defaultValue = "20", required = false) Integer size,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "doctorIds", required = false) String doctorIds
    ) {
        List<Integer> ids = (doctorIds != null)
                ? Arrays.stream(doctorIds.split(",")).map(Integer::parseInt).toList()
                : null;

        PatientResponse response = patientService.getAllPatients(page, size, search, ids);
        return ResponseEntity.ok(response);
    }
}
