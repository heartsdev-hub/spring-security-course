package com.course.api.exception;

public class DataAccessException extends RuntimeException{
    public DataAccessException(String message) {
        super(message);
    }
}
