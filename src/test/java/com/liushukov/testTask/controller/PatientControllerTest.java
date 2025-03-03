package com.liushukov.testTask.controller;

import com.liushukov.testTask.entity.Doctor;
import com.liushukov.testTask.entity.Patient;
import com.liushukov.testTask.entity.Visit;
import com.liushukov.testTask.mapper.VisitMapper;
import com.liushukov.testTask.repository.DoctorRepository;
import com.liushukov.testTask.repository.PatientRepository;
import com.liushukov.testTask.repository.VisitRepository;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;
import static com.liushukov.testTask.controller.PatientControllerTest.TestResources.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class PatientControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private VisitMapper visitMapper;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private VisitRepository visitRepository;

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


    @PostConstruct
    public void init() {
        Optional<Patient> patient1 = patientRepository.findById(PATIENT_1_ID);
        Optional<Patient> patient2 = patientRepository.findById(PATIENT_2_ID);
        Optional<Patient> patient3 = patientRepository.findById(PATIENT_3_ID);
        Optional<Doctor> doctor1 = doctorRepository.findDoctorById(DOCTOR_1_ID);
        Optional<Doctor> doctor5 = doctorRepository.findDoctorById(DOCTOR_5_ID);
        if (patient1.isPresent() && patient2.isPresent() && patient3.isPresent() && doctor1.isPresent()
                && doctor5.isPresent()) {
            List<Visit> visits = new ArrayList<>();
            visits.add(visitMapper.toEntity(
                    VISIT_1_PATIENT_1_START,
                    VISIT_1_PATIENT_1_END,
                    doctor1.get(),
                    patient1.get()
            ));
            visits.add(visitMapper.toEntity(
                    VISIT_1_PATIENT_2_START,
                    VISIT_1_PATIENT_2_END,
                    doctor1.get(),
                    patient2.get()
            ));
            visits.add(visitMapper.toEntity(
                    VISIT_2_PATIENT_1_START,
                    VISIT_2_PATIENT_1_END,
                    doctor1.get(),
                    patient1.get()
            ));
            visits.add(visitMapper.toEntity(
                    VISIT_3_PATIENT_1_START,
                    VISIT_3_PATIENT_1_END,
                    doctor5.get(),
                    patient1.get()
            ));
            visits.add(visitMapper.toEntity(
                    VISIT_1_PATIENT_3_START,
                    VISIT_1_PATIENT_3_END,
                    doctor5.get(),
                    patient3.get()
            ));
            for (Visit element : visits) {
                visitRepository.save(element);
            }
        }
    }

    @Test
    void givenEmptySearchAndDoctorIds_all_shouldReturnAllPatients() throws Exception {
        mockMvc.perform(get(URL_PATIENTS_ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(20)))
                .andExpect(jsonPath("$.data", hasSize(20)))
                .andExpect(jsonPath("$.data[0].firstName", is("John")))
                .andExpect(jsonPath("$.data[0].lastName", is("Smith")))
                .andExpect(jsonPath("$.data[0].lastVisits", hasSize(2)))
                .andExpect(jsonPath("$.data[0].lastVisits[0].doctor.firstName", is("Robert")))
                .andExpect(jsonPath("$.data[0].lastVisits[0].doctor.lastName", is("Moore")))
                .andExpect(jsonPath("$.data[0].lastVisits[0].doctor.totalPatients", is(2)))
                .andExpect(jsonPath("$.data[1].firstName", is("Emily")))
                .andExpect(jsonPath("$.data[1].lastName", is("Johnson")))
                .andExpect(jsonPath("$.data[1].lastVisits", hasSize(1)))
                .andExpect(jsonPath("$.data[2].firstName", is("Michael")))
                .andExpect(jsonPath("$.data[2].lastName", is("Williams")))
                .andExpect(jsonPath("$.data[2].lastVisits", hasSize(1)))
                .andExpect(jsonPath("$.data[3].firstName", is("Jessica")))
                .andExpect(jsonPath("$.data[3].lastName", is("Brown")))
                .andExpect(jsonPath("$.data[3].lastVisits", hasSize(0)))
                .andDo(print());
    }

    @Test
    void givenSearch_all_shouldReturnPatientsBySearch() throws Exception {
        mockMvc.perform(get(URL_PATIENTS_ALL + SEARCH))
                .andExpect(jsonPath("$.count", is(1)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].firstName", is("John")))
                .andExpect(jsonPath("$.data[0].lastName", is("Smith")))
                .andExpect(jsonPath("$.data[0].lastVisits", hasSize(2)))
                .andExpect(jsonPath("$.data[0].lastVisits[0].doctor.firstName", is("Robert")))
                .andExpect(jsonPath("$.data[0].lastVisits[0].doctor.lastName", is("Moore")))
                .andExpect(jsonPath("$.data[0].lastVisits[0].doctor.totalPatients", is(2)))
                .andDo(print());
    }

    @Test
    void givenDoctorIds_all_shouldReturnPatientsWhoVisitedDoctors() throws Exception {
        mockMvc.perform(get(URL_PATIENTS_ALL + DOCTOR_IDS))
                .andExpect(jsonPath("$.count", is(2)))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].lastVisits", hasSize(1)))
                .andExpect(jsonPath("$.data[1].lastVisits", hasSize(1)))
                .andDo(print());
    }

    static class TestResources {
        static final Integer PATIENT_1_ID = 1;
        static final Integer PATIENT_2_ID = 2;
        static final Integer PATIENT_3_ID = 3;
        static final Integer DOCTOR_1_ID = 1;
        static final Integer DOCTOR_5_ID = 5;
        static final String VISIT_1_PATIENT_1_START = "2024-10-31T13:50:00"; // doctor 1
        static final String VISIT_1_PATIENT_1_END = "2024-10-31T14:50:00"; // doctor 1
        static final String VISIT_1_PATIENT_2_START = "2024-10-31T15:50:00"; // doctor 1
        static final String VISIT_1_PATIENT_2_END = "2024-10-31T16:50:00"; // doctor 1
        static final String VISIT_2_PATIENT_1_START = "2024-11-05T13:50:00"; // doctor 1
        static final String VISIT_2_PATIENT_1_END = "2024-11-05T14:50:00"; // doctor 1
        static final String VISIT_3_PATIENT_1_START = "2024-11-06T13:50:00"; // doctor 5
        static final String VISIT_3_PATIENT_1_END = "2024-11-06T14:50:00"; // doctor 5
        static final String VISIT_1_PATIENT_3_START = "2024-12-31T15:50:00"; // doctor 5
        static final String VISIT_1_PATIENT_3_END = "2024-12-31T16:50:00"; // doctor 5
        static final String URL_PATIENTS_ALL = "/patients/all";
        static final String SEARCH = "?search=John"; // patient 1 due to migrations
        static final String DOCTOR_IDS = "?doctorIds=1";
    }
}
