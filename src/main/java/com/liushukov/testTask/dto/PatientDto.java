package com.liushukov.testTask.dto;

import java.io.Serializable;
import java.util.List;

public record PatientDto(String firstName, String lastName, List<VisitDto> lastVisits) implements Serializable {
}
