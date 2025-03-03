package com.liushukov.testTask.service.impl;

import com.liushukov.testTask.dto.*;
import com.liushukov.testTask.entity.Patient;
import com.liushukov.testTask.entity.Visit;
import com.liushukov.testTask.repository.DoctorRepository;
import com.liushukov.testTask.repository.PatientRepository;
import com.liushukov.testTask.service.PatientService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PatientServiceImpl implements PatientService {
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final static Logger logger = LoggerFactory.getLogger(PatientServiceImpl.class);

    public PatientServiceImpl(PatientRepository patientRepository, DoctorRepository doctorRepository) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    public Optional<Patient> getPatientById(int id) {
        logger.info("retrieving patient with id={}", id);
        return patientRepository.findPatientById(id);
    }

    @Override
    public PatientResponse getAllPatients(int page, int size, String search, List<Integer> doctorIds) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Integer> patientIds = (doctorIds == null)
                ? patientRepository.findPatientIdsBySearch(search, pageable)
                : patientRepository.findPatientIdsBySearchAndDoctorIds(search, doctorIds, pageable);
        logger.info("retrieved patient ids: {}", patientIds.getContent());
        List<Patient> patients = (patientIds.hasContent())
                ? patientRepository.findPatientsWithVisitsAndDoctorsByIds(patientIds.getContent())
                : Collections.emptyList();
        logger.info("retrieved patients: {}", patients);
        Map<Integer, Long> doctorPatientCounts = doctorRepository.countTotalPatientsPerDoctor()
                .stream()
                .collect(Collectors.toMap(
                    DoctorPatientCountProjection::getDoctorId,
                    DoctorPatientCountProjection::getTotalPatients
                ));
        logger.info("retrieved count of patients per doctor: {}", doctorPatientCounts);
        List<PatientDto> patientDtos = patients.stream().map(patient -> {
            Map<Integer, Visit> lastDoctorVisits = new HashMap<>();
            for (Visit visit : patient.getVisits()) {
                int doctorId = visit.getDoctor().getId();
                if (doctorIds == null || doctorIds.contains(doctorId)) {
                    if (!lastDoctorVisits.containsKey(doctorId) || visit.getEndDateTime()
                            .isAfter(lastDoctorVisits.get(doctorId).getEndDateTime())) {
                        lastDoctorVisits.put(doctorId, visit);
                    }
                }
            }
            List<VisitDto> visitDtos = lastDoctorVisits.values().stream()
                    .map(v -> new VisitDto(
                            v.getStartDateTime().atZone(ZoneId.of(v.getDoctor().getTimezone())).toString(),
                            v.getEndDateTime().atZone(ZoneId.of(v.getDoctor().getTimezone())).toString(),
                            new DoctorDto(
                                    v.getDoctor().getFirstName(),
                                    v.getDoctor().getLastName(),
                                    doctorPatientCounts.get(v.getDoctor().getId()).intValue()
                            )
                    ))
                    .toList();
            return new PatientDto(patient.getFirstName(), patient.getLastName(), visitDtos);
        }).toList();
        logger.info("list of patientDto: {}", patientDtos);
        logger.info("count: {}", patientDtos.size());
        return new PatientResponse(patientDtos, patientDtos.size());
    }
}
