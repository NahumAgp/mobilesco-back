package com.mobilesco.mobilesco_back.modules.shared.infrastructure.in.api.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    @Test
    void accessDeniedDevuelveForbiddenConMensajeClaro() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<Map<String, Object>> response =
                handler.handleAccessDenied(new AccessDeniedException("denied"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getBody().get("status"));
        assertEquals("Forbidden", response.getBody().get("error"));
        assertEquals("No tienes permisos para realizar esta accion.", response.getBody().get("message"));
        assertEquals(false, response.getBody().get("success"));
    }
}
