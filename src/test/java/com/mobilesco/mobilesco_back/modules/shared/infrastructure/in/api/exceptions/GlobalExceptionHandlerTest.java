package com.mobilesco.mobilesco_back.modules.shared.infrastructure.in.api.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.HttpMethod;

import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;

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

    @Test
    void optimisticLockDevuelveConflictConMensajeClaro() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<Map<String, Object>> response =
                handler.handleOptimisticLock(new ObjectOptimisticLockingFailureException(InsumoModel.class, 1L));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(HttpStatus.CONFLICT.value(), response.getBody().get("status"));
        assertEquals("Conflict", response.getBody().get("error"));
        assertEquals("El registro fue modificado por otro usuario. Vuelve a intentarlo.", response.getBody().get("message"));
        assertEquals(false, response.getBody().get("success"));
    }

    @Test
    void archivoInvalidoDevuelveBadRequest() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<Map<String, Object>> response =
                handler.handleIllegalArgument(new IllegalArgumentException("archivo invalido"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("archivo invalido", response.getBody().get("message"));
    }

    @Test
    void recursoInexistenteDevuelveNotFound() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<Map<String, Object>> response =
                handler.handleNoResource(new NoResourceFoundException(
                        HttpMethod.GET,
                        "/v3/api-docs",
                        "/v3/api-docs"
                ));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Recurso no encontrado", response.getBody().get("message"));
    }
}
