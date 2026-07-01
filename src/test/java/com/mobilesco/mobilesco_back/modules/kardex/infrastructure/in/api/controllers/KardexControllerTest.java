package com.mobilesco.mobilesco_back.modules.kardex.infrastructure.in.api.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

class KardexControllerTest {

    private static final String PERMISO_VER_KARDEX = "hasAuthority('VIEW_KARDEX')";

    @Test
    void todosLosEndpointsDeKardexRequierenPermiso() throws Exception {
        assertPreAuthorize("getHistorialPorInsumo", Long.class);
        assertPreAuthorize("getMovimientosPorPeriodo", LocalDateTime.class, LocalDateTime.class);
        assertPreAuthorize("getMovimientosPorCompra", Long.class);
        assertPreAuthorize("getCostoPromedio", Long.class);
        assertPreAuthorize("getConsumoEnPeriodo", Long.class, LocalDateTime.class, LocalDateTime.class);
    }

    private void assertPreAuthorize(String methodName, Class<?>... parameterTypes) throws Exception {
        Method method = KardexController.class.getMethod(methodName, parameterTypes);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize);
        assertEquals(PERMISO_VER_KARDEX, preAuthorize.value());
    }
}
