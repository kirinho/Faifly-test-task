package com.liushukov.testTask.exceptions;

import java.time.Instant;

public record ErrorDetails(
        Instant exceptionTime,
        String description,
        String details
) {
}
