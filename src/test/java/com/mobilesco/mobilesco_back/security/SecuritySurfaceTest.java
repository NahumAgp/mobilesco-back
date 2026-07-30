package com.mobilesco.mobilesco_back.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
class SecuritySurfaceTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void catalogoDeImagenesEsPublico() throws Exception {
        mockMvc.perform(get("/uploads/modelos/1/inexistente.jpg"))
                .andExpect(status().isNotFound());
    }

    @Test
    void fotoDeEmpleadoRequiereAutenticacion() throws Exception {
        mockMvc.perform(get("/uploads/empleados/1/perfil/inexistente.jpg"))
                .andExpect(status().isForbidden());
    }

    @Test
    void empleadoNoPuedeConsultarFotoAjena() throws Exception {
        mockMvc.perform(get("/uploads/empleados/1/perfil/inexistente.jpg")
                        .with(user("empleado@mobilesco.test").roles("EMPLEADO")))
                .andExpect(status().isForbidden());
    }

    @Test
    void rolAdministradorSuperaFiltroDeFotoPrivada() throws Exception {
        mockMvc.perform(get("/uploads/empleados/1/perfil/inexistente.jpg")
                        .with(user("admin@mobilesco.test").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void openApiNoEsPublicoFueraDelEntornoAutorizado() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/v3/api-docs")
                        .with(user("admin@mobilesco.test").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }
}
