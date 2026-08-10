package com.mobilesco.mobilesco_back.modules.proveedor.infrastructure.in.api.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

class ProveedorControllerTest {

    @Test
    void eliminarProveedorRequierePermisoEspecifico() throws Exception {
        Method method = ProveedorController.class.getMethod("eliminar", Long.class);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize);
        assertEquals("hasAuthority('VIEW_SUPPLIERS') and hasAuthority('ACTION_SUPPLIERS_DELETE')", preAuthorize.value());
        assertTrue(preAuthorize.value().contains("ACTION_SUPPLIERS_DELETE"));
    }
}
