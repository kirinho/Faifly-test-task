package com.liushukov.testTask.service;

import com.liushukov.testTask.dto.DoctorPatientCountProjection;
import com.liushukov.testTask.dto.PatientResponse;
import com.liushukov.testTask.entity.Doctor;
import com.liushukov.testTask.entity.Patient;
import com.liushukov.testTask.entity.Visit;
import com.liushukov.testTask.repository.DoctorRepository;
import com.liushukov.testTask.repository.PatientRepository;
import com.liushukov.testTask.service.impl.PatientServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static com.liushukov.testTask.service.PatientServiceImplTest.TestResources.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PatientServiceImplTest {
    @Mock
    PatientRepository patientRepository;
    @Mock
    DoctorRepository doctorRepository;
    @InjectMocks
    PatientServiceImpl patientService;

    @Test
    void givenPatientId_getPatientById_shouldReturnNonEmptyPatient() {
        when(patientRepository.findPatientById(PATIENT_ID)).thenReturn(Optional.of(buildPatientEntity()));

        Optional<Patient> patient = patientService.getPatientById(PATIENT_ID);

        verify(patientRepository).findPatientById(PATIENT_ID);
        Assertions.assertTrue(patient.isPresent());
        Assertions.assertEquals(patient.get(), buildPatientEntity());
    }

    @Test
    void givenInvalidPatientId_getPatientById_shouldReturnEmptyPatient() {
        when(patientRepository.findPatientById(PATIENT_INVALID_ID)).thenReturn(Optional.empty());

        Optional<Patient> patient = patientService.getPatientById(PATIENT_INVALID_ID);

        verify(patientRepository).findPatientById(PATIENT_INVALID_ID);
        Assertions.assertTrue(patient.isEmpty());
    }

    @Test
    void givenPageableAndEmptySearchAndDoctorIds_getAllPatients_shouldReturnAllPatients() {
        when(patientRepository.findPatientIdsBySearch(null, buildPageable()))
                .thenReturn(buildFindPatientIdsBySearchResponse());

        Patient patient1 = buildPatientEntity();
        patient1.setVisits(List.of(buildVisitEntity()));

        Patient patient2 = buildPatientEntity2();
        patient2.setVisits(List.of(buildVisitEntity2()));

        when(patientRepository.findPatientsWithVisitsAndDoctorsByIds(PATIENT_IDS))
                .thenReturn(List.of(patient1, patient2));

        DoctorPatientCountProjection doctorPatientCountProjection = new DoctorPatientCountProjection() {
            @Override
            public Integer getDoctorId() {
                return DOCTOR_ID;
            }

            @Override
            public Long getTotalPatients() {
                return 2L;
            }
        };
        when(doctorRepository.countTotalPatientsPerDoctor()).thenReturn(List.of(doctorPatientCountProjection));

        PatientResponse patientResponse = patientService.getAllPatients(PAGE, SIZE, null, null);

        verify(patientRepository).findPatientIdsBySearch(null, buildPageable());
        verify(patientRepository).findPatientsWithVisitsAndDoctorsByIds(PATIENT_IDS);
        verify(doctorRepository).countTotalPatientsPerDoctor();
        Assertions.assertEquals(2, patientResponse.count());
        Assertions.assertEquals(2, patientResponse.data().size());
        Assertions.assertEquals(PATIENT_FIRST_NAME, patientResponse.data().get(0).firstName());
        Assertions.assertEquals(PATIENT_LAST_NAME, patientResponse.data().get(0).lastName());
        Assertions.assertEquals(PATIENT_2_FIRST_NAME, patientResponse.data().get(1).firstName());
        Assertions.assertEquals(PATIENT_2_LAST_NAME, patientResponse.data().get(1).lastName());
    }

    @Test
    void givenPageableAndNonEmptySearch_getAllPatients_shouldReturnPatient() {
        when(patientRepository.findPatientIdsBySearch(SEARCH, buildPageable()))
                .thenReturn(buildFindPatientIdsBySearchResponse2());
        Patient patient = buildPatientEntity2();
        patient.setVisits(List.of(buildVisitEntity2()));
        when(patientRepository.findPatientsWithVisitsAndDoctorsByIds(List.of(PATIENT_2_ID)))
                .thenReturn(List.of(patient));
        DoctorPatientCountProjection doctorPatientCountProjection = new DoctorPatientCountProjection() {
            @Override
            public Integer getDoctorId() {
                return DOCTOR_ID;
            }

            @Override
            public Long getTotalPatients() {
                return 1L;
            }
        };
        when(doctorRepository.countTotalPatientsPerDoctor()).thenReturn(List.of(doctorPatientCountProjection));

        PatientResponse patientResponse = patientService.getAllPatients(PAGE, SIZE, SEARCH, null);

        verify(patientRepository).findPatientIdsBySearch(SEARCH, buildPageable());
        verify(patientRepository).findPatientsWithVisitsAndDoctorsByIds(List.of(PATIENT_2_ID));
        verify(doctorRepository).countTotalPatientsPerDoctor();
        Assertions.assertEquals(1, patientResponse.count());
        Assertions.assertEquals(1, patientResponse.data().size());
        Assertions.assertEquals(PATIENT_2_FIRST_NAME, patientResponse.data().get(0).firstName());
        Assertions.assertEquals(PATIENT_2_LAST_NAME, patientResponse.data().get(0).lastName());
    }

    @Test
    void givenVisitWithLaterEndDate_getAllPatients_shouldUpdateLastVisit() {
        when(patientRepository.findPatientIdsBySearchAndDoctorIds(SEARCH, DOCTOR_IDS, buildPageable()))
                .thenReturn(buildFindPatientIdsBySearchResponse2());
        Patient patient = buildPatientEntity2();
        patient.setVisits(List.of(buildVisitEntity(), buildVisitEntity2()));
        when(patientRepository.findPatientsWithVisitsAndDoctorsByIds(List.of(PATIENT_2_ID)))
                .thenReturn(List.of(patient));
        DoctorPatientCountProjection doctorPatientCountProjection = new DoctorPatientCountProjection() {
            @Override
            public Integer getDoctorId() {
                return DOCTOR_ID;
            }

            @Override
            public Long getTotalPatients() {
                return 1L;
            }
        };
        when(doctorRepository.countTotalPatientsPerDoctor()).thenReturn(List.of(doctorPatientCountProjection));

        PatientResponse patientResponse = patientService.getAllPatients(PAGE, SIZE, SEARCH, DOCTOR_IDS);

        verify(patientRepository).findPatientIdsBySearchAndDoctorIds(SEARCH, DOCTOR_IDS, buildPageable());
        verify(patientRepository).findPatientsWithVisitsAndDoctorsByIds(List.of(PATIENT_2_ID));
        verify(doctorRepository).countTotalPatientsPerDoctor();
        Assertions.assertEquals(1, patientResponse.count());
        Assertions.assertEquals(1, patientResponse.data().size());
        Assertions.assertEquals(PATIENT_2_FIRST_NAME, patientResponse.data().get(0).firstName());
        Assertions.assertEquals(PATIENT_2_LAST_NAME, patientResponse.data().get(0).lastName());
    }

    @Test
    void givenInvalidPageableAndSearchAndDoctorIds_getAllPatients_shouldReturnEmptyResponse() {
        when(patientRepository.findPatientIdsBySearchAndDoctorIds(SEARCH, DOCTOR_IDS, buildPageable()))
                .thenReturn(buildFindPatientIdsBySearchResponseEmpty());

        DoctorPatientCountProjection doctorPatientCountProjection = new DoctorPatientCountProjection() {
            @Override
            public Integer getDoctorId() {
                return DOCTOR_ID;
            }

            @Override
            public Long getTotalPatients() {
                return 1L;
            }
        };
        when(doctorRepository.countTotalPatientsPerDoctor()).thenReturn(List.of(doctorPatientCountProjection));

        PatientResponse patientResponse = patientService.getAllPatients(PAGE, SIZE, SEARCH, DOCTOR_IDS);

        verify(patientRepository).findPatientIdsBySearchAndDoctorIds(SEARCH, DOCTOR_IDS, buildPageable());
        verify(doctorRepository).countTotalPatientsPerDoctor();
        Assertions.assertEquals(0, patientResponse.count());
        Assertions.assertEquals(0, patientResponse.data().size());
    }

    @Test
    void givenVisitWithEarlierEndDate_getAllPatients_shouldNotUpdateLastVisit() {
        when(patientRepository.findPatientIdsBySearch(null, buildPageable()))
                .thenReturn(buildFindPatientIdsBySearchResponse());

        Visit oldVisit = buildVisitEntity();
        Visit newVisit = buildVisitEntity();
        newVisit.setEndDateTime(oldVisit.getEndDateTime().minusDays(1));

        Patient patient = buildPatientEntity();
        patient.setVisits(List.of(oldVisit, newVisit));

        when(patientRepository.findPatientsWithVisitsAndDoctorsByIds(PATIENT_IDS))
                .thenReturn(List.of(patient));

        DoctorPatientCountProjection doctorPatientCountProjection = new DoctorPatientCountProjection() {
            @Override
            public Integer getDoctorId() {
                return DOCTOR_ID;
            }

            @Override
            public Long getTotalPatients() {
                return 1L;
            }
        };
        when(doctorRepository.countTotalPatientsPerDoctor()).thenReturn(List.of(doctorPatientCountProjection));

        PatientResponse patientResponse = patientService.getAllPatients(PAGE, SIZE, null, null);

        verify(patientRepository).findPatientsWithVisitsAndDoctorsByIds(PATIENT_IDS);
        Assertions.assertEquals(1, patientResponse.count());
    }

    @Test
    void givenDoctorIdAndDoctorIdNotInDoctorIds_getAllPatients_shouldFilterOutVisits() {
        List<Integer> doctorIds = List.of(100);
        when(patientRepository.findPatientIdsBySearchAndDoctorIds(SEARCH, doctorIds, buildPageable()))
                .thenReturn(buildFindPatientIdsBySearchResponse2());

        Patient patient = buildPatientEntity2();
        Visit visit = buildVisitEntity2();
        visit.getDoctor().setId(DOCTOR_ID);
        patient.setVisits(List.of(visit));

        when(patientRepository.findPatientsWithVisitsAndDoctorsByIds(List.of(PATIENT_2_ID)))
                .thenReturn(List.of(patient));

        DoctorPatientCountProjection doctorPatientCountProjection = new DoctorPatientCountProjection() {
            @Override
            public Integer getDoctorId() {
                return DOCTOR_ID;
            }

            @Override
            public Long getTotalPatients() {
                return 1L;
            }
        };
        when(doctorRepository.countTotalPatientsPerDoctor()).thenReturn(List.of(doctorPatientCountProjection));

        PatientResponse patientResponse = patientService.getAllPatients(PAGE, SIZE, SEARCH, doctorIds);

        verify(patientRepository).findPatientIdsBySearchAndDoctorIds(SEARCH, doctorIds, buildPageable());
        verify(patientRepository).findPatientsWithVisitsAndDoctorsByIds(List.of(PATIENT_2_ID));
        verify(doctorRepository).countTotalPatientsPerDoctor();

        Assertions.assertEquals(1, patientResponse.count());
        Assertions.assertEquals(1, patientResponse.data().size());
        Assertions.assertEquals(PATIENT_2_FIRST_NAME, patientResponse.data().get(0).firstName());
        Assertions.assertEquals(PATIENT_2_LAST_NAME, patientResponse.data().get(0).lastName());
        Assertions.assertEquals(0, patientResponse.data().get(0).lastVisits().size());
    }

    static class TestResources {
        static final Integer PATIENT_ID = 1;
        static final Integer PATIENT_2_ID = 2;
        static final Integer PATIENT_INVALID_ID = 999;
        static final String PATIENT_FIRST_NAME = "patient_first_name";
        static final String PATIENT_LAST_NAME = "patient_last_name";
        static final String PATIENT_2_FIRST_NAME = "patient_2_first_name";
        static final String PATIENT_2_LAST_NAME = "patient_2_last_name";
        static final Integer DOCTOR_ID = 1;
        static final String DOCTOR_FIRST_NAME = "doctor_first_name";
        static final String DOCTOR_LAST_NAME = "doctor_last_name";
        final static String DOCTOR_TIMEZONE = "America/New_York";
        static final LocalDateTime VISIT_START_DATE_TIME =
                LocalDateTime.of(2024, 10, 31, 10, 0);
        static final LocalDateTime VISIT_END_DATE_TIME =
                LocalDateTime.of(2024, 10, 31, 11, 0);
        static final Integer PAGE = 0;
        static final Integer SIZE = 20;
        static final String SEARCH = "patient_2";
        static final List<Integer> DOCTOR_IDS = List.of(1);
        static final List<Integer> PATIENT_IDS = List.of(PATIENT_ID, PATIENT_2_ID);

        static Patient buildPatientEntity() {
            return new Patient()
                    .setFirstName(PATIENT_FIRST_NAME)
                    .setLastName(PATIENT_LAST_NAME);
        }

        static Patient buildPatientEntity2() {
            return new Patient()
                    .setFirstName(PATIENT_2_FIRST_NAME)
                    .setLastName(PATIENT_2_LAST_NAME);
        }

        static Doctor buildDoctorEntity() {
            return new Doctor()
                    .setId(DOCTOR_ID)
                    .setFirstName(DOCTOR_FIRST_NAME)
                    .setLastName(DOCTOR_LAST_NAME)
                    .setTimezone(DOCTOR_TIMEZONE);
        }

        static Visit buildVisitEntity() {
            return new Visit()
                    .setStartDateTime(VISIT_START_DATE_TIME)
                    .setEndDateTime(VISIT_END_DATE_TIME)
                    .setDoctor(buildDoctorEntity())
                    .setPatient(buildPatientEntity());
        }

        static Visit buildVisitEntity2() {
            return new Visit()
                    .setStartDateTime(VISIT_START_DATE_TIME.plusHours(2))
                    .setEndDateTime(VISIT_END_DATE_TIME.plusHours(2))
                    .setDoctor(buildDoctorEntity())
                    .setPatient(buildPatientEntity());
        }

        static Pageable buildPageable() {
            return PageRequest.of(PAGE, SIZE);
        }

        static Page<Integer> buildFindPatientIdsBySearchResponse() {
            return new PageImpl<>(PATIENT_IDS, buildPageable(), PATIENT_IDS.size());
        }

        static Page<Integer> buildFindPatientIdsBySearchResponse2() {
            return new PageImpl<>(List.of(PATIENT_2_ID), buildPageable(), 1);
        }

        static Page<Integer> buildFindPatientIdsBySearchResponseEmpty() {
            return new PageImpl<>(Collections.emptyList(), buildPageable(), 0);
        }
    }
}
