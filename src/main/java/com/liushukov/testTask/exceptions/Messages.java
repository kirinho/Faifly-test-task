package com.liushukov.testTask.exceptions;

public enum Messages {
    INVALID_DATETIME_ORDER("StartDateTime is invalid due to it is after the endDateTime"),
    INVALID_DATETIME_FORMAT("Exception during converting datetime: "),
    DOCTOR_DOES_NOT_AVAILABLE("Doctor doesn't available at this time");

    private final String description;

    Messages(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
