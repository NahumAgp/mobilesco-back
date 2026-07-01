package com.mobilesco.mobilesco_back.modules.insumo.infrastructure.in.api.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.in.api.dtos.InsumoCostoCotizacionUpdateDTO;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.in.api.dtos.InsumoCreateDTO;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.in.api.dtos.InsumoEstadoUpdateDTO;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.in.api.dtos.InsumoUpdateDTO;

class InsumoControllerTest {

    private static final String PERMISO_VER_INVENTARIO = "hasAuthority('VIEW_INVENTORY')";
    private static final String PERMISO_GESTION_COSTOS_INSUMOS = "hasAuthority('ACTION_INSUMOS_COSTS')";
    private static final String ROLES_GESTION_INSUMOS = "hasAnyRole('ADMIN','SUPER_ADMIN','DIRECTOR_GENERAL','JEFE_ALMACEN','ALMACEN','SUBDIRECCION_ADMINISTRATIVA')";

    @Test
    void endpointsDeGestionDeInsumosEstanProtegidos() throws Exception {
        assertPreAuthorize("crear", ROLES_GESTION_INSUMOS, InsumoCreateDTO.class);
        assertPreAuthorize("actualizar", ROLES_GESTION_INSUMOS, Long.class, InsumoUpdateDTO.class);
        assertPreAuthorize("actualizarEstado", ROLES_GESTION_INSUMOS, Long.class, InsumoEstadoUpdateDTO.class);
        assertPreAuthorize("ajustarStock", ROLES_GESTION_INSUMOS, Long.class, Double.class, String.class, String.class);
        assertPreAuthorize("eliminar", ROLES_GESTION_INSUMOS, Long.class);
    }

    @Test
    void endpointsDeLecturaDeInsumosRequierenPermisoDeInventario() throws Exception {
        assertPreAuthorize("obtenerPorId", PERMISO_VER_INVENTARIO, Long.class);
        assertPreAuthorize("listar", PERMISO_VER_INVENTARIO, Integer.class, Integer.class, String.class, String.class, String.class, Boolean.class, Boolean.class);
        assertPreAuthorize("exportarExcel", PERMISO_VER_INVENTARIO, Boolean.class, Boolean.class, String.class, String.class, String.class);
        assertPreAuthorize("listarActivos", PERMISO_VER_INVENTARIO);
        assertPreAuthorize("getTiposInsumo", PERMISO_VER_INVENTARIO);
        assertPreAuthorize("buscar", PERMISO_VER_INVENTARIO, String.class, String.class, Boolean.class);
        assertPreAuthorize("listarPorUnidadMedida", PERMISO_VER_INVENTARIO, Long.class);
        assertPreAuthorize("listarStockBajo", PERMISO_VER_INVENTARIO);
    }

    @Test
    void endpointsDeCostosDeInsumosRequierenPermisoDeCostos() throws Exception {
        assertPreAuthorize("listarCostos", PERMISO_GESTION_COSTOS_INSUMOS, Integer.class, Integer.class, String.class, String.class, String.class);
        assertPreAuthorize("actualizarCostoCotizacion", "hasAnyRole('ADMIN','SUPER_ADMIN','SUBDIRECCION_ADMINISTRATIVA')", Long.class, InsumoCostoCotizacionUpdateDTO.class);
    }

    private void assertPreAuthorize(String methodName, String expectedValue, Class<?>... parameterTypes) throws Exception {
        Method method = InsumoController.class.getMethod(methodName, parameterTypes);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize);
        assertEquals(expectedValue, preAuthorize.value());
    }
}
