package com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

class CompraControllerTest {

    @Test
    void eliminarCompraSoloPermiteDireccionGeneralYDevAdmin() throws Exception {
        Method method = CompraController.class.getMethod("eliminar", Long.class);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize);
        assertEquals("hasAnyRole('ADMIN','SUPER_ADMIN','DIRECTOR_GENERAL')", preAuthorize.value());
        assertFalse(preAuthorize.value().contains("SUBDIRECCION_ADMINISTRATIVA"));
        assertFalse(preAuthorize.value().contains("JEFE_ALMACEN"));
    }
}
