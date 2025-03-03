package com.liushukov.testTask.dto;

import java.util.List;

public record PatientDto(String firstName, String lastName, List<VisitDto> lastVisits) {
}
