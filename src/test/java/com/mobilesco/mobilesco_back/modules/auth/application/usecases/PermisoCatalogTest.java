package com.mobilesco.mobilesco_back.modules.auth.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class PermisoCatalogTest {

    @Test
    void todosLosCodigosSonUnicosYLasAccionesTienenVistaValida() {
        Map<String, PermisoCatalog.Definition> porCodigo = PermisoCatalog.DEFINITIONS.stream()
                .collect(Collectors.toMap(PermisoCatalog.Definition::code, Function.identity()));

        assertEquals(PermisoCatalog.DEFINITIONS.size(), porCodigo.size());
        PermisoCatalog.DEFINITIONS.stream()
                .filter(definition -> "ACTION".equals(definition.tipo()))
                .forEach(definition -> {
                    assertNotNull(definition.vistaRequerida(), definition.code());
                    assertTrue(porCodigo.containsKey(definition.vistaRequerida()), definition.code());
                    assertEquals("VIEW", porCodigo.get(definition.vistaRequerida()).tipo(), definition.code());
                });
    }

    @Test
    void rolesDeAccesoCompletoCompartenUnaSolaPolitica() {
        Set<String> todos = PermisoCatalog.ALL_CODES;
        assertEquals(todos, PermisoCatalog.DEFAULT_ROLE_PERMISSIONS.get("ADMIN"));
        assertEquals(todos, PermisoCatalog.DEFAULT_ROLE_PERMISSIONS.get("DIRECTOR_GENERAL"));
        assertEquals(todos, PermisoCatalog.DEFAULT_ROLE_PERMISSIONS.get("SUBDIRECCION_ADMINISTRATIVA"));
        assertEquals(
                Set.of("ADMIN", "DIRECTOR_GENERAL", "SUBDIRECCION_ADMINISTRATIVA"),
                PermisoCatalog.FULL_ACCESS_ROLES);
        assertFalse(PermisoCatalog.DEFAULT_ROLE_PERMISSIONS.containsKey("SUPER_ADMIN"));
    }

    @Test
    void obtieneLaVistaRequeridaSinFallarParaPermisosDeVistaODesconocidos() {
        assertEquals("VIEW_USERS", PermisoCatalog.requiredView("ACTION_USERS_CREATE"));
        assertNull(PermisoCatalog.requiredView("VIEW_USERS"));
        assertNull(PermisoCatalog.requiredView("PERMISO_ANTIGUO_NO_CATALOGADO"));
    }
}
