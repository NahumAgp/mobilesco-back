package com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.in.api.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.in.api.dtos.SalidaInsumoCreateDTO;

class SalidaInsumoControllerTest {

    private static final String PERMISO_VER_INVENTARIO = "hasAuthority('VIEW_INVENTORY_OUTPUTS')";
    private static final String ROLES_GESTION_INVENTARIO = "hasAuthority('VIEW_INVENTORY_OUTPUTS') and hasAuthority('ACTION_INVENTORY_OUTPUTS_CREATE')";

    @Test
    void crearSalidaDeInsumoRequiereRolDeGestion() throws Exception {
        assertPreAuthorize("crear", ROLES_GESTION_INVENTARIO, SalidaInsumoCreateDTO.class);
    }

    @Test
    void endpointsDeLecturaDeSalidasRequierenPermisoDeInventario() throws Exception {
        assertPreAuthorize(
                "listar",
                PERMISO_VER_INVENTARIO,
                String.class,
                String.class,
                String.class,
                LocalDate.class,
                LocalDate.class,
                Integer.class,
                Integer.class);
        assertPreAuthorize("obtenerPorId", PERMISO_VER_INVENTARIO, Long.class);
    }

    private void assertPreAuthorize(String methodName, String expectedValue, Class<?>... parameterTypes) throws Exception {
        Method method = SalidaInsumoController.class.getMethod(methodName, parameterTypes);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize);
        assertEquals(expectedValue, preAuthorize.value());
    }
}
