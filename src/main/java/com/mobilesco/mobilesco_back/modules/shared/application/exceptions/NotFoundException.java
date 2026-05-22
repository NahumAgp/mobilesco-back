package com.mobilesco.mobilesco_back.modules.shared.application.exceptions;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
