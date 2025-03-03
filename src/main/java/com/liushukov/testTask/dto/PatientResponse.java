package com.liushukov.testTask.dto;

import java.util.List;

public record PatientResponse(List<PatientDto> data, int count) {
}
