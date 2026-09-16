package com.mobilesco.mobilesco_back.modules.abastecimiento.infrastructure.in.api.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import com.mobilesco.mobilesco_back.modules.abastecimiento.infrastructure.in.api.dtos.CrearComprasBorradorRequestDTO;

class AbastecimientoControllerTest {

    @Test
    void lecturaDeSugerenciasRequierePermisoDeAbastecimientoAsistido() throws Exception {
        assertPreAuthorize(
                "obtenerSugerencias",
                "hasAuthority('VIEW_ASSISTED_PROCUREMENT')");
    }

    @Test
    void crearBorradoresRequiereAccionDeAbastecimientoAsistido() throws Exception {
        assertPreAuthorize(
                "crearComprasBorrador",
                "hasAuthority('VIEW_ASSISTED_PROCUREMENT') and hasAuthority('ACTION_ASSISTED_PROCUREMENT_DRAFTS')",
                CrearComprasBorradorRequestDTO.class);
    }

    private void assertPreAuthorize(
            String methodName,
            String expectedValue,
            Class<?>... parameterTypes) throws Exception {
        Method method = AbastecimientoController.class.getMethod(methodName, parameterTypes);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
        assertNotNull(preAuthorize);
        assertEquals(expectedValue, preAuthorize.value());
    }
}
