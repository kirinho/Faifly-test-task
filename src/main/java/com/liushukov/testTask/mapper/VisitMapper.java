package com.liushukov.testTask.mapper;

import com.liushukov.testTask.entity.Doctor;
import com.liushukov.testTask.entity.Patient;
import com.liushukov.testTask.entity.Visit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VisitMapper {
    @Mapping(target = "startDateTime", source = "startDateTime")
    @Mapping(target = "endDateTime", source = "endDateTime")
    @Mapping(target = "doctor", source = "doctor")
    @Mapping(target = "patient", source = "patient")
    Visit toEntity(String startDateTime, String endDateTime, Doctor doctor, Patient patient);
}
