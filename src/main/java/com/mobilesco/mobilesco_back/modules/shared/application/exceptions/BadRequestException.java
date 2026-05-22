package com.mobilesco.mobilesco_back.modules.shared.application.exceptions;

public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
