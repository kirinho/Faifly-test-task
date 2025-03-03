package com.liushukov.testTask.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liushukov.testTask.dto.VisitCreateDto;
import com.liushukov.testTask.entity.Visit;
import com.liushukov.testTask.exceptions.Messages;
import com.liushukov.testTask.mapper.VisitMapper;
import com.liushukov.testTask.repository.DoctorRepository;
import com.liushukov.testTask.repository.PatientRepository;
import com.liushukov.testTask.repository.VisitRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.Optional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.liushukov.testTask.controller.VisitControllerTest.TestResources.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class VisitControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private VisitRepository visitRepository;
    @Autowired
    private VisitMapper visitMapper;

    @Container
    static MySQLContainer mySQLContainer = new MySQLContainer<>("mysql:8.0");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        dynamicPropertyRegistry.add("spring.datasource.username", mySQLContainer::getUsername);
        dynamicPropertyRegistry.add("spring.datasource.password", mySQLContainer::getPassword);
    }

    @BeforeAll
    static void beforeAll() {
        mySQLContainer.start();
    }

    @AfterAll
    static void afterAll() {
        mySQLContainer.stop();
    }

    @Test
    void givenVisitCreateDto_createVisit_shouldCreateVisit() throws Exception {
        mockMvc.perform(post(URL_VISIT_CREATE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildVisitCreateDto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startDateTime").value(VISIT_START))
                .andExpect(jsonPath("$.endDateTime").value(VISIT_END))
                .andExpect(jsonPath("$.patient.id").value(PATIENT_ID))
                .andExpect(jsonPath("$.doctor.id").value(DOCTOR_ID))
                .andDo(print());

        Assertions.assertEquals(1, visitRepository.findAll().size());
        Assertions.assertTrue(patientRepository.findPatientById(PATIENT_ID).isPresent());
        Assertions.assertTrue(doctorRepository.findDoctorById(DOCTOR_ID).isPresent());
    }

    @Test
    void givenInvalidPatient_createVisit_shouldReturnNotFound() throws Exception {
        mockMvc.perform(post(URL_VISIT_CREATE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildInvalidPatientInVisitCreateDto())))
                .andExpect(status().isNotFound())
                .andDo(print());
        Assertions.assertTrue(patientRepository.findPatientById(PATIENT_INVALID_ID).isEmpty());
        Assertions.assertTrue(doctorRepository.findDoctorById(DOCTOR_INVALID_ID).isEmpty());
    }

    @Test
    void givenInvalidDoctor_createVisit_shouldReturnNotFound() throws Exception {
        mockMvc.perform(post(URL_VISIT_CREATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildInvalidDoctorInVisitCreateDto())))
                .andExpect(status().isNotFound())
                .andDo(print());
        Assertions.assertTrue(patientRepository.findPatientById(PATIENT_INVALID_ID).isEmpty());
        Assertions.assertTrue(doctorRepository.findDoctorById(DOCTOR_INVALID_ID).isEmpty());
    }

    @Test
    void givenInvalidTimeOrder_createVisit_shouldThrowCustomException() throws Exception {
        mockMvc.perform(post(URL_VISIT_CREATE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildInvalidTimeOrderInVisitCreateDto())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(Messages.INVALID_DATETIME_ORDER.getDescription()))
                .andDo(print());
    }

    @Test
    void givenDatetimeWhenDoctorDoesNotAvailable_createVisit_shouldTrowCustomException() throws Exception {
        Optional<Visit> visit = visitRepository.findById(VISIT_ID);
        if (visit.isEmpty()) {
            Visit newVisit = visitMapper.toEntity(
                    VISIT_START,
                    VISIT_END,
                    doctorRepository.findDoctorById(DOCTOR_ID).get(),
                    patientRepository.findPatientById(PATIENT_ID).get()
            );
            visitRepository.save(newVisit);
        }

        mockMvc.perform(post(URL_VISIT_CREATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildVisitCreateDto())))
                .andExpect(status().isConflict())
                .andDo(print());
    }

    @Test
    void givenInvalidDatetimeFormat_createVisit_shouldThrowCustomException() throws Exception {
        mockMvc.perform(post(URL_VISIT_CREATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildInvalidDatetimeFormatInVisitCreateDto())))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    static class TestResources {
        static final Integer VISIT_ID = 1;
        static final String VISIT_START = "2024-10-31T13:50:00";
        static final String VISIT_END = "2024-10-31T14:50:00";
        static final String VISIT_START_AFTER_END = "2024-10-31T15:50:00";
        static final String VISIT_START_INVALID_FORMAT = "2024-10-31 15:50:00";
        static final Integer PATIENT_ID = 1;
        static final Integer DOCTOR_ID = 1;
        static final Integer PATIENT_INVALID_ID = 999;
        static final Integer DOCTOR_INVALID_ID = 999;
        static final String URL_VISIT_CREATE = "/visits/visit/create";

        static VisitCreateDto buildVisitCreateDto() {
            return new VisitCreateDto(
                    VISIT_START,
                    VISIT_END,
                    PATIENT_ID,
                    DOCTOR_ID
            );
        }

        static VisitCreateDto buildInvalidPatientInVisitCreateDto() {
            return new VisitCreateDto(
                    VISIT_START,
                    VISIT_END,
                    PATIENT_INVALID_ID,
                    DOCTOR_ID
            );
        }

        static VisitCreateDto buildInvalidDoctorInVisitCreateDto() {
            return new VisitCreateDto(
                    VISIT_START,
                    VISIT_END,
                    PATIENT_ID,
                    DOCTOR_INVALID_ID
            );
        }

        static VisitCreateDto buildInvalidTimeOrderInVisitCreateDto() {
            return new VisitCreateDto(
                    VISIT_START_AFTER_END,
                    VISIT_END,
                    PATIENT_ID,
                    DOCTOR_ID
            );
        }

        static VisitCreateDto buildInvalidDatetimeFormatInVisitCreateDto() {
            return new VisitCreateDto(
                    VISIT_START_INVALID_FORMAT,
                    VISIT_END,
                    PATIENT_ID,
                    DOCTOR_ID
            );
        }
    }
}
