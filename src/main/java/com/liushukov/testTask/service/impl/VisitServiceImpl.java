package com.liushukov.testTask.service.impl;

import com.liushukov.testTask.dto.VisitCreateDto;
import com.liushukov.testTask.entity.Doctor;
import com.liushukov.testTask.entity.Patient;
import com.liushukov.testTask.entity.Visit;
import com.liushukov.testTask.exceptions.CustomException;
import com.liushukov.testTask.exceptions.Messages;
import com.liushukov.testTask.mapper.VisitMapper;
import com.liushukov.testTask.repository.VisitRepository;
import com.liushukov.testTask.service.VisitService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class VisitServiceImpl implements VisitService {
    private final VisitRepository visitRepository;
    private final VisitMapper visitMapper;

    public VisitServiceImpl(VisitRepository visitRepository, VisitMapper visitMapper) {
        this.visitRepository = visitRepository;
        this.visitMapper = visitMapper;
    }

    public Visit createVisit(VisitCreateDto visitCreateDto, Doctor doctor, Patient patient) throws CustomException {
        LocalDateTime startDateTime = convertToLocalDateTime(visitCreateDto.start(), doctor.getTimezone());
        LocalDateTime endDateTime = convertToLocalDateTime(visitCreateDto.end(), doctor.getTimezone());

        if (endDateTime.isBefore(startDateTime)) {
            throw new CustomException(Messages.INVALID_DATETIME_ORDER.getDescription(), HttpStatus.BAD_REQUEST);
        }

        if (isVisitOverlapping(doctor, startDateTime, endDateTime)) {
            throw new CustomException(Messages.DOCTOR_DOES_NOT_AVAILABLE.getDescription(), HttpStatus.CONFLICT);
        }

        Visit visit = visitMapper.toEntity(visitCreateDto.start(), visitCreateDto.end(), doctor, patient);
        return visitRepository.save(visit);
    }

    private LocalDateTime convertToLocalDateTime(String dateTimeString, String timezone) throws CustomException {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString, formatter);
            ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of(timezone));
            return zonedDateTime.toLocalDateTime();
        } catch (Exception e) {
            throw new CustomException(Messages.INVALID_DATETIME_FORMAT.getDescription() + e, HttpStatus.BAD_REQUEST);
        }
    }

    private boolean isVisitOverlapping(Doctor doctor, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<Visit> existingVisits = visitRepository.findOverlappingVisits(doctor, endDateTime, startDateTime);
        return !existingVisits.isEmpty();
    }
}
