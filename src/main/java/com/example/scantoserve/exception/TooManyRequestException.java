package com.example.scantoserve.exception;

public class TooManyRequestException extends RuntimeException {
    public TooManyRequestException(String message) {
        super(message);
    }
}
