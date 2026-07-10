package com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.dtos.CompraCreateDTO;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.dtos.CompraUpdateDTO;

class CompraControllerTest {

    private static final String PERMISO_VER_COMPRAS = "hasAuthority('VIEW_PURCHASES')";
    private static final String ROLES_GESTION_COMPRAS = "hasAnyRole('ADMIN','SUPER_ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA','JEFE_ALMACEN')";

    @Test
    void endpointsDeGestionDeCompraEstanProtegidos() throws Exception {
        assertPreAuthorize("crear", ROLES_GESTION_COMPRAS, CompraCreateDTO.class);
        assertPreAuthorize("actualizar", ROLES_GESTION_COMPRAS, Long.class, CompraUpdateDTO.class);
        assertPreAuthorize("recibirCompra", ROLES_GESTION_COMPRAS, Long.class);
        assertPreAuthorize("cancelarCompra", ROLES_GESTION_COMPRAS, Long.class, String.class);
    }

    @Test
    void endpointsDeLecturaDeCompraRequierenPermiso() throws Exception {
        assertPreAuthorize("obtenerPorId", PERMISO_VER_COMPRAS, Long.class);
        assertPreAuthorize(
                "listar",
                PERMISO_VER_COMPRAS,
                Integer.class,
                Integer.class,
                String.class,
                String.class,
                String.class,
                LocalDate.class,
                LocalDate.class);
        assertPreAuthorize("listarPorProveedor", PERMISO_VER_COMPRAS, Long.class);
        assertPreAuthorize("listarPorEstado", PERMISO_VER_COMPRAS, String.class);
        assertPreAuthorize("listarPorRangoFechas", PERMISO_VER_COMPRAS, LocalDate.class, LocalDate.class);
        assertPreAuthorize("buscarPorFolio", PERMISO_VER_COMPRAS, String.class);
    }

    @Test
    void eliminarCompraSoloPermiteDireccionGeneralYDevAdmin() throws Exception {
        PreAuthorize preAuthorize = assertPreAuthorize("eliminar", "hasAnyRole('ADMIN','SUPER_ADMIN','DIRECTOR_GENERAL')", Long.class);
        assertFalse(preAuthorize.value().contains("SUBDIRECCION_ADMINISTRATIVA"));
        assertFalse(preAuthorize.value().contains("JEFE_ALMACEN"));
    }

    private PreAuthorize assertPreAuthorize(String methodName, String expectedValue, Class<?>... parameterTypes) throws Exception {
        Method method = CompraController.class.getMethod(methodName, parameterTypes);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize);
        assertEquals(expectedValue, preAuthorize.value());
        return preAuthorize;
    }
}
