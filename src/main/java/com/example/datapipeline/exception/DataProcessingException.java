package com.example.datapipeline.exception;

import org.springframework.http.HttpStatus;

public class DataProcessingException extends RuntimeException {
    private HttpStatus status;
    private String message;

    public DataProcessingException(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public DataProcessingException(String message, HttpStatus status, String message1) {
        super(message);
        this.status = status;
        this.message = message1;
    }

    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
