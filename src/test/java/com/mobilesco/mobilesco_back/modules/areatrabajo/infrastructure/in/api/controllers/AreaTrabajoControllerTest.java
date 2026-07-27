package com.mobilesco.mobilesco_back.modules.areatrabajo.infrastructure.in.api.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import com.mobilesco.mobilesco_back.modules.areatrabajo.infrastructure.in.api.dtos.AreaTrabajoCreateDTO;
import com.mobilesco.mobilesco_back.modules.areatrabajo.infrastructure.in.api.dtos.AreaTrabajoUpdateDTO;

class AreaTrabajoControllerTest {

    private static final String GESTION =
            "hasAnyRole('ADMIN','SUPER_ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA') or hasAuthority('VIEW_EMPLOYEES')";
    private static final String CONSULTA_Y_CREACION_PARA_SALIDAS =
            GESTION + " or hasRole('JEFE_ALMACEN')";

    @Test
    void jefeAlmacenPuedeConsultarYCrearAreasParaSalidas() throws Exception {
        assertPreAuthorize("listar", CONSULTA_Y_CREACION_PARA_SALIDAS,
                Boolean.class, String.class, Integer.class, Integer.class);
        assertPreAuthorize("sugerirCodigo", CONSULTA_Y_CREACION_PARA_SALIDAS, String.class);
        assertPreAuthorize("crear", CONSULTA_Y_CREACION_PARA_SALIDAS, AreaTrabajoCreateDTO.class);
    }

    @Test
    void edicionYEstadoDeAreasMantienenGestionAdministrativa() throws Exception {
        assertPreAuthorize("actualizar", GESTION, Long.class, AreaTrabajoUpdateDTO.class);
        assertPreAuthorize("activar", GESTION, Long.class);
        assertPreAuthorize("desactivar", GESTION, Long.class);
    }

    private void assertPreAuthorize(String methodName, String expected, Class<?>... parameterTypes)
            throws Exception {
        Method method = AreaTrabajoController.class.getMethod(methodName, parameterTypes);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation);
        assertEquals(expected, annotation.value());
    }
}
