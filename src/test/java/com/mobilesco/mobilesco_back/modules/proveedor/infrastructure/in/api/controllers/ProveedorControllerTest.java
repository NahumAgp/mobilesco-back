package com.mobilesco.mobilesco_back.modules.proveedor.infrastructure.in.api.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

class ProveedorControllerTest {

    @Test
    void eliminarProveedorIncluyeSuperAdmin() throws Exception {
        Method method = ProveedorController.class.getMethod("eliminar", Long.class);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize);
        assertEquals("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DIRECTOR_GENERAL', 'SUBDIRECCION_ADMINISTRATIVA')", preAuthorize.value());
        assertTrue(preAuthorize.value().contains("SUPER_ADMIN"));
    }
}
