package com.liushukov.testTask.dto;

import jakarta.validation.constraints.NotNull;

public record VisitCreateDto(
        @NotNull(message = "start is mandatory")
        String start,
        @NotNull(message = "end is mandatory")
        String end,
        @NotNull(message = "patientId is mandatory")
        Integer patientId,
        @NotNull(message = "doctorId is mandatory")
        Integer doctorId
) {
}
