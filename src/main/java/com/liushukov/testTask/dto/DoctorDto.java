package com.liushukov.testTask.dto;

import java.io.Serializable;

public record DoctorDto(String firstName, String lastName, int totalPatients) implements Serializable {
}
