package com.liushukov.testTask.service;

import com.liushukov.testTask.dto.VisitCreateDto;
import com.liushukov.testTask.entity.Doctor;
import com.liushukov.testTask.entity.Patient;
import com.liushukov.testTask.entity.Visit;
import com.liushukov.testTask.exceptions.CustomException;

public interface VisitService {
    Visit createVisit(VisitCreateDto visitCreateDto, Doctor doctor, Patient patient) throws CustomException;
}
