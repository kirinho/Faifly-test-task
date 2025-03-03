package com.liushukov.testTask.exceptions;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {
    private final static Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception exception, WebRequest webRequest) {

        if (exception instanceof MethodArgumentNotValidException) {
            ErrorDetails errorDetails = new ErrorDetails(
                    Instant.now(),
                    exception.getMessage(),
                    webRequest.getDescription(false)
            );
            logger.error("MethodArgumentNotValidException: {}", errorDetails);
            return ResponseEntity.status(HttpStatusCode.valueOf(400)).body(errorDetails);
        }

        if (exception instanceof CustomException) {
            ErrorDetails errorDetails = new ErrorDetails(
                    Instant.now(),
                    exception.getMessage(),
                    webRequest.getDescription(false)
            );
            logger.error("CustomException: {}", errorDetails);
            return ResponseEntity.status(((CustomException) exception).getHttpStatus()).body(errorDetails);
        }
        ErrorDetails errorDetails = new ErrorDetails(
                Instant.now(),
                "Internal server error",
                webRequest.getDescription(false)
        );
        logger.error("InternalServerError: {}", exception);
        return ResponseEntity.status(HttpStatusCode.valueOf(500))
                .body(errorDetails);
    }
}
