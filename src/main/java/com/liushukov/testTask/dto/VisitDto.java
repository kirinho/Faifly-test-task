package com.liushukov.testTask.dto;

import java.io.Serializable;

public record VisitDto(String start, String end, DoctorDto doctor) implements Serializable {
}
