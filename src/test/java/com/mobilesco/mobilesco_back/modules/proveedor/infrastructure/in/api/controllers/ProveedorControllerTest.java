package com.mobilesco.mobilesco_back.modules.proveedor.infrastructure.in.api.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;

import com.mobilesco.mobilesco_back.modules.proveedor.infrastructure.in.api.dtos.ProveedorCalificacionUpdateDTO;

class ProveedorControllerTest {

    @Test
    void eliminarProveedorRequierePermisoEspecifico() throws Exception {
        Method method = ProveedorController.class.getMethod("eliminar", Long.class);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize);
        assertEquals("hasAuthority('VIEW_SUPPLIERS') and hasAuthority('ACTION_SUPPLIERS_DELETE')", preAuthorize.value());
        assertTrue(preAuthorize.value().contains("ACTION_SUPPLIERS_DELETE"));
    }

    @Test
    void actualizarCalificacionExponePatchConPermisoDeEdicion() throws Exception {
        Method method = ProveedorController.class.getMethod(
                "actualizarCalificacion",
                Long.class,
                ProveedorCalificacionUpdateDTO.class);

        PatchMapping patchMapping = method.getAnnotation(PatchMapping.class);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(patchMapping);
        assertEquals("/{id}/calificacion", patchMapping.value()[0]);
        assertNotNull(preAuthorize);
        assertEquals(
                "hasAuthority('VIEW_SUPPLIERS') and hasAuthority('ACTION_SUPPLIERS_EDIT')",
                preAuthorize.value());
    }
}
