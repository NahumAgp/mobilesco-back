package com.mobilesco.mobilesco_back.modules.cliente.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mobilesco.mobilesco_back.modules.cliente.domain.models.ClasificacionCliente;
import com.mobilesco.mobilesco_back.modules.cliente.domain.models.ClienteModel;
import com.mobilesco.mobilesco_back.modules.cliente.domain.models.TipoPersonaCliente;
import com.mobilesco.mobilesco_back.modules.cliente.infrastructure.in.api.dtos.ClienteRequestDTO;
import com.mobilesco.mobilesco_back.modules.cliente.infrastructure.in.api.dtos.ClienteResponseDTO;
import com.mobilesco.mobilesco_back.modules.cliente.infrastructure.out.persistence.repositories.ClienteRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.BadRequestException;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    private ClienteService clienteService;

    @BeforeEach
    void setUp() {
        clienteService = new ClienteService(clienteRepository);
    }

    @Test
    void crearNormalizaDatosYGeneraCodigoComercial() {
        ClienteRequestDTO dto = clienteValido();
        dto.setRfc("abc010101ab1");
        dto.setCorreo(" VENTAS@EJEMPLO.COM ");

        when(clienteRepository.findByRfcIgnoreCase("ABC010101AB1")).thenReturn(Optional.empty());
        when(clienteRepository.save(any(ClienteModel.class))).thenAnswer(invocation -> {
            ClienteModel guardado = invocation.getArgument(0);
            guardado.setId(10L);
            return guardado;
        });

        ClienteResponseDTO resultado = clienteService.crear(dto);

        assertNotNull(resultado.getCodigo());
        assertEquals("ABC010101AB1", resultado.getRfc());
        assertEquals("ventas@ejemplo.com", resultado.getCorreo());
        assertEquals("Cliente prioritario", resultado.getClasificacionEtiqueta());
    }

    @Test
    void crearRechazaRfcDuplicado() {
        ClienteRequestDTO dto = clienteValido();
        dto.setRfc("ABC010101AB1");
        ClienteModel existente = ClienteModel.builder().id(7L).rfc(dto.getRfc()).build();
        when(clienteRepository.findByRfcIgnoreCase(dto.getRfc())).thenReturn(Optional.of(existente));

        assertThrows(BadRequestException.class, () -> clienteService.crear(dto));
    }

    @Test
    void clasificacionesExponenCodigosEstablesYEtiquetasAmigables() {
        assertEquals(6, clienteService.listarClasificaciones().size());
        assertEquals("Prospecto", clienteService.listarClasificaciones().get(0).etiqueta());
    }

    private ClienteRequestDTO clienteValido() {
        ClienteRequestDTO dto = new ClienteRequestDTO();
        dto.setClasificacion(ClasificacionCliente.PRIORITARIO);
        dto.setTipoPersona(TipoPersonaCliente.MORAL);
        dto.setRazonSocial("Cliente de prueba SA de CV");
        return dto;
    }
}
