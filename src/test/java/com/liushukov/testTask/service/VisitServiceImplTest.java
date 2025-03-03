package com.liushukov.testTask.service;

import com.liushukov.testTask.dto.VisitCreateDto;
import com.liushukov.testTask.entity.Doctor;
import com.liushukov.testTask.entity.Patient;
import com.liushukov.testTask.entity.Visit;
import com.liushukov.testTask.exceptions.CustomException;
import com.liushukov.testTask.exceptions.Messages;
import com.liushukov.testTask.mapper.VisitMapper;
import com.liushukov.testTask.repository.VisitRepository;
import com.liushukov.testTask.service.impl.VisitServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import static com.liushukov.testTask.service.VisitServiceImplTest.TestResources.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VisitServiceImplTest {
    @Mock
    VisitRepository visitRepository;
    @Mock
    VisitMapper visitMapper;
    @InjectMocks
    VisitServiceImpl visitService;

    @Test
    void givenValidVisitCreateDtoAndEntities_createVisit_shouldCreateAndReturnVisit() throws CustomException {
        when(visitMapper.toEntity(START, END, buildDoctorEntity(), buildPatientEntity()))
                .thenReturn(buildVisitEntity());
        when(visitRepository.findOverlappingVisits(buildDoctorEntity(), LocalDateTime.parse(END),
                LocalDateTime.parse(START))).thenReturn(Collections.emptyList());
        when(visitRepository.save(any(Visit.class))).thenReturn(buildVisitEntity());

        Visit visit = visitService.createVisit(buildVisitCreateDto(), buildDoctorEntity(), buildPatientEntity());

        verify(visitRepository).save(buildVisitEntity());
        Assertions.assertEquals(visit, buildVisitEntity());
    }

    @Test
    void givenInvalidDatetimeOrder_createVisit_shouldThrowCustomException() {
        CustomException customException = Assertions.assertThrows(
                CustomException.class,
                () -> visitService.createVisit(buildVisitCreateDtoInvalidDatetimeOrder(),
                        buildDoctorEntity(),
                        buildPatientEntity())
        );

        Assertions.assertEquals(Messages.INVALID_DATETIME_ORDER.getDescription(), customException.getMessage());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, customException.getHttpStatus());
    }

    @Test
    void givenInvalidDatetimeFormat_createVisit_shouldThrowCustomException() {
        CustomException customException = Assertions.assertThrows(
                CustomException.class,
                () -> visitService.createVisit(buildVisitCreateDtoInvalidDatetimeFormat(),
                        buildDoctorEntity(),
                        buildPatientEntity())
        );

        Assertions.assertTrue(customException.getMessage().contains(Messages.INVALID_DATETIME_FORMAT.getDescription()));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, customException.getHttpStatus());
    }

    @Test
    void givenDatetimeThatWasAlreadyBooked_createVisit_shouldThrowCustomException() {
        when(visitRepository.findOverlappingVisits(buildDoctorEntity(), LocalDateTime.parse(END),
                LocalDateTime.parse(START))).thenReturn(List.of(buildVisitEntity()));

        CustomException customException = Assertions.assertThrows(
                CustomException.class,
                () -> visitService.createVisit(buildVisitCreateDto(), buildDoctorEntity(), buildPatientEntity())
        );

        Assertions.assertEquals(Messages.DOCTOR_DOES_NOT_AVAILABLE.getDescription(), customException.getMessage());
        Assertions.assertEquals(HttpStatus.CONFLICT, customException.getHttpStatus());
    }

    static class TestResources {
        final static String START = "2024-10-31T13:50:00";
        final static String END = "2024-10-31T14:50:00";
        final static Integer DOCTOR_ID = 1;
        final static Integer PATIENT_ID = 1;
        final static String DOCTOR_FIRST_NAME = "doctor_test_first_name";
        final static String DOCTOR_LAST_NAME = "doctor_test_last_name";
        final static String DOCTOR_TIMEZONE = "America/New_York";
        final static String PATIENT_FIRST_NAME = "patient_test_first_name";
        final static String PATIENT_LAST_NAME = "patient_test_last_name";
        final static String INVALID_START_DATETIME = "2024-10-31 13:50:00";
        final static String INVALID_END_DATETIME = "2024-10-31 14:50:00";

        static VisitCreateDto buildVisitCreateDto() {
            return new VisitCreateDto(
                    START,
                    END,
                    DOCTOR_ID,
                    PATIENT_ID
            );
        }

        static VisitCreateDto buildVisitCreateDtoInvalidDatetimeOrder() {
            return new VisitCreateDto(
                    END,
                    START,
                    DOCTOR_ID,
                    PATIENT_ID
            );
        }

        static VisitCreateDto buildVisitCreateDtoInvalidDatetimeFormat() {
            return new VisitCreateDto(
                    INVALID_START_DATETIME,
                    INVALID_END_DATETIME,
                    DOCTOR_ID,
                    PATIENT_ID
            );
        }

        static Doctor buildDoctorEntity() {
            return new Doctor()
                    .setFirstName(DOCTOR_FIRST_NAME)
                    .setLastName(DOCTOR_LAST_NAME)
                    .setTimezone(DOCTOR_TIMEZONE);
        }

        static Patient buildPatientEntity() {
            return new Patient()
                    .setFirstName(PATIENT_FIRST_NAME)
                    .setLastName(PATIENT_LAST_NAME);
        }

        static Visit buildVisitEntity() {
            return new Visit()
                    .setStartDateTime(LocalDateTime.parse(START))
                    .setEndDateTime(LocalDateTime.parse(END))
                    .setPatient(buildPatientEntity())
                    .setDoctor(buildDoctorEntity());
        }
    }
}
