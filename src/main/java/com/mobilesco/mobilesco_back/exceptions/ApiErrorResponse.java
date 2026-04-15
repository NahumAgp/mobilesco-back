package com.mobilesco.mobilesco_back.exceptions;

import java.util.Map;

public class ApiErrorResponse {

    private final boolean success;
    private final String message;
    private final Map<String, String> errors;

    public ApiErrorResponse(boolean success, String message, Map<String, String> errors) {
        this.success = success;
        this.message = message;
        this.errors = errors;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
