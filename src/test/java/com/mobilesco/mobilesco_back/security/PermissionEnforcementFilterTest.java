package com.mobilesco.mobilesco_back.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class PermissionEnforcementFilterTest {

    private final PermissionEnforcementFilter filter = new PermissionEnforcementFilter();

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void unaAccionSinVistaEsRechazada() throws Exception {
        autenticar("ACTION_INVENTORY_CREATE");
        MockHttpServletResponse response = ejecutar("POST", "/api/v1/insumos");
        assertEquals(403, response.getStatus());
    }

    @Test
    void laVistaSinAccionSoloPermiteLectura() throws Exception {
        autenticar("VIEW_INVENTORY");
        assertEquals(200, ejecutar("GET", "/api/v1/insumos").getStatus());
        assertEquals(403, ejecutar("POST", "/api/v1/insumos").getStatus());
    }

    @Test
    void vistaYAccionPermitenLaOperacion() throws Exception {
        autenticar("VIEW_INVENTORY", "ACTION_INVENTORY_CREATE");
        assertEquals(200, ejecutar("POST", "/api/v1/insumos").getStatus());
    }

    @Test
    void direccionGeneralPuedeEjecutarAccionesSinPermisosGranulares() throws Exception {
        autenticar("ROLE_DIRECTOR_GENERAL");
        assertEquals(200, ejecutar("POST", "/api/v1/insumos").getStatus());
        assertEquals(200, ejecutar("POST", "/api/v1/insumos/7/ajustar-stock").getStatus());
        assertEquals(200, ejecutar("POST", "/api/v1/insumos/conjuntos").getStatus());
    }

    @Test
    void abastecimientoAsistidoUsaPermisosPropios() throws Exception {
        autenticar("VIEW_PURCHASES", "ACTION_PURCHASES_CREATE");
        assertEquals(403, ejecutar("GET", "/api/v1/abastecimiento/sugerencias").getStatus());
        assertEquals(403, ejecutar("POST", "/api/v1/abastecimiento/compras-borrador").getStatus());

        autenticar("VIEW_ASSISTED_PROCUREMENT");
        assertEquals(200, ejecutar("GET", "/api/v1/abastecimiento/sugerencias").getStatus());
        assertEquals(403, ejecutar("POST", "/api/v1/abastecimiento/compras-borrador").getStatus());

        autenticar("VIEW_ASSISTED_PROCUREMENT", "ACTION_ASSISTED_PROCUREMENT_DRAFTS");
        assertEquals(200, ejecutar("POST", "/api/v1/abastecimiento/compras-borrador").getStatus());
    }

    @Test
    void conjuntosDeInsumosUsanPermisosPropios() throws Exception {
        autenticar("VIEW_INVENTORY", "ACTION_INVENTORY_CREATE", "ACTION_INVENTORY_EDIT");
        assertEquals(403, ejecutar("GET", "/api/v1/insumos/conjuntos").getStatus());
        assertEquals(403, ejecutar("POST", "/api/v1/insumos/conjuntos").getStatus());

        autenticar("VIEW_INPUT_SETS");
        assertEquals(200, ejecutar("GET", "/api/v1/insumos/conjuntos").getStatus());
        assertEquals(403, ejecutar("POST", "/api/v1/insumos/conjuntos").getStatus());
        assertEquals(403, ejecutar("PUT", "/api/v1/insumos/conjuntos/7").getStatus());

        autenticar("VIEW_INPUT_SETS", "ACTION_INPUT_SETS_CREATE", "ACTION_INPUT_SETS_EDIT");
        assertEquals(200, ejecutar("POST", "/api/v1/insumos/conjuntos").getStatus());
        assertEquals(200, ejecutar("PUT", "/api/v1/insumos/conjuntos/7").getStatus());
    }

    private void autenticar(String... authorities) {
        var granted = List.of(authorities).stream().map(SimpleGrantedAuthority::new).toList();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("usuario", "n/a", granted));
    }

    private MockHttpServletResponse ejecutar(String method, String path) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setRequestURI(path);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
