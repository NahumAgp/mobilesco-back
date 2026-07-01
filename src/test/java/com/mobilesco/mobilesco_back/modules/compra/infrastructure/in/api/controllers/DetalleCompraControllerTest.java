package com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.dtos.DetalleCompraCreateDTO;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.dtos.DetalleCompraUpdateDTO;

class DetalleCompraControllerTest {

    private static final String PERMISO_VER_COMPRAS = "hasAuthority('VIEW_PURCHASES')";
    private static final String ROLES_GESTION_COMPRAS = "hasAnyRole('ADMIN','SUPER_ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA','JEFE_ALMACEN')";

    @Test
    void endpointsDeGestionDeDetalleCompraEstanProtegidos() throws Exception {
        assertPreAuthorize("crear", ROLES_GESTION_COMPRAS, Long.class, DetalleCompraCreateDTO.class);
        assertPreAuthorize("actualizar", ROLES_GESTION_COMPRAS, Long.class, DetalleCompraUpdateDTO.class);
        assertPreAuthorize("recibirParcial", ROLES_GESTION_COMPRAS, Long.class, Double.class, String.class, String.class);
        assertPreAuthorize("eliminar", ROLES_GESTION_COMPRAS, Long.class);
    }

    @Test
    void endpointsDeLecturaDeDetalleCompraRequierenPermiso() throws Exception {
        assertPreAuthorize("obtenerPorId", PERMISO_VER_COMPRAS, Long.class);
        assertPreAuthorize("listarPorCompra", PERMISO_VER_COMPRAS, Long.class);
        assertPreAuthorize("listarPorInsumo", PERMISO_VER_COMPRAS, Long.class);
    }

    private void assertPreAuthorize(String methodName, String expectedValue, Class<?>... parameterTypes) throws Exception {
        Method method = DetalleCompraController.class.getMethod(methodName, parameterTypes);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize);
        assertEquals(expectedValue, preAuthorize.value());
    }
}
