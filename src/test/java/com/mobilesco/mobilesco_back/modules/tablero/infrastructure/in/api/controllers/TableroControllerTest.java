package com.mobilesco.mobilesco_back.modules.tablero.infrastructure.in.api.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

class TableroControllerTest {
    @Test
    void protegeElResumenConPermisoDeTablero() {
        PreAuthorize permiso = TableroController.class.getAnnotation(PreAuthorize.class);

        assertNotNull(permiso);
        assertEquals("hasAuthority('VIEW_DASHBOARD')", permiso.value());
    }
}
