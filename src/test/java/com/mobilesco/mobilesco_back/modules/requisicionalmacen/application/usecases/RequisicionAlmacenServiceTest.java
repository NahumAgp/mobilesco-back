package com.mobilesco.mobilesco_back.modules.requisicionalmacen.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mobilesco.mobilesco_back.modules.auth.domain.models.UsuarioModel;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.UsuarioRepository;
import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.out.persistence.repositories.InsumoRepository;
import com.mobilesco.mobilesco_back.modules.requisicionalmacen.domain.models.EstadoRequisicionAlmacen;
import com.mobilesco.mobilesco_back.modules.requisicionalmacen.domain.models.RequisicionAlmacenModel;
import com.mobilesco.mobilesco_back.modules.requisicionalmacen.infrastructure.in.api.dtos.RequisicionCreateDTO;
import com.mobilesco.mobilesco_back.modules.requisicionalmacen.infrastructure.in.api.dtos.RequisicionPartidaRequestDTO;
import com.mobilesco.mobilesco_back.modules.requisicionalmacen.infrastructure.in.api.dtos.RequisicionResponseDTO;
import com.mobilesco.mobilesco_back.modules.requisicionalmacen.infrastructure.out.persistence.repositories.RequisicionAlmacenRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.modules.unidadmedida.domain.models.UnidadMedidaModel;

@ExtendWith(MockitoExtension.class)
class RequisicionAlmacenServiceTest {

    @Mock
    private RequisicionAlmacenRepository requisicionRepository;
    @Mock
    private InsumoRepository insumoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private UsuarioModel usuario;

    private RequisicionAlmacenService service;

    @BeforeEach
    void setUp() {
        service = new RequisicionAlmacenService(requisicionRepository, insumoRepository, usuarioRepository);
    }

    @Test
    void sugerenciasCalculanFaltantePeroNoCreanRequisicion() {
        UnidadMedidaModel unidad = new UnidadMedidaModel();
        unidad.setSimbolo("kg");
        InsumoModel insumo = InsumoModel.builder()
                .id(5L)
                .codigo("INS-005")
                .nombre("Acero")
                .stockActual(3.0)
                .stockMinimo(10.0)
                .activo(true)
                .unidadMedida(unidad)
                .build();
        InsumoModel justoEnMinimo = InsumoModel.builder()
                .id(6L)
                .codigo("INS-006")
                .nombre("Tornillo")
                .stockActual(10.0)
                .stockMinimo(10.0)
                .activo(true)
                .unidadMedida(unidad)
                .build();
        when(insumoRepository.findWithStockBajo()).thenReturn(List.of(insumo, justoEnMinimo));

        var sugerencias = service.sugerencias();

        assertEquals(1, sugerencias.size());
        assertEquals(7.0, sugerencias.get(0).getFaltanteMinimo());
        assertEquals(7.0, sugerencias.get(0).getCantidadSugerida());
        assertFalse(requisicionRepository.count() > 0);
    }

    @Test
    void crearGuardaSnapshotsYEnviaASubdireccion() {
        when(usuario.getId()).thenReturn(9L);
        when(usuario.getEmail()).thenReturn("almacen@mobilesco.mx");
        when(usuarioRepository.findOneByEmail("almacen@mobilesco.mx")).thenReturn(Optional.of(usuario));

        InsumoModel insumo = InsumoModel.builder()
                .id(2L)
                .codigo("INS-002")
                .nombre("Pintura")
                .stockActual(1.0)
                .stockMinimo(5.0)
                .activo(true)
                .build();
        when(insumoRepository.findById(2L)).thenReturn(Optional.of(insumo));
        when(requisicionRepository.save(any(RequisicionAlmacenModel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RequisicionResponseDTO resultado = service.crear(
                request(partida(2L, 8.0)),
                "almacen@mobilesco.mx");

        assertEquals(EstadoRequisicionAlmacen.ENVIADA, resultado.getEstado());
        assertEquals("Subdirección Administrativa", resultado.getDestinatario());
        assertEquals(1.0, resultado.getPartidas().get(0).getStockActualSnapshot());
        assertEquals(5.0, resultado.getPartidas().get(0).getStockMinimoSnapshot());
    }

    @Test
    void crearImpideInsumosDuplicados() {
        when(usuario.getEmail()).thenReturn("almacen@mobilesco.mx");
        when(usuarioRepository.findOneByEmail("almacen@mobilesco.mx")).thenReturn(Optional.of(usuario));
        InsumoModel insumo = InsumoModel.builder()
                .id(2L).codigo("INS-002").nombre("Pintura").stockActual(1.0).activo(true).build();
        when(insumoRepository.findById(2L)).thenReturn(Optional.of(insumo));

        assertThrows(
                ValidationException.class,
                () -> service.crear(
                        request(partida(2L, 2.0), partida(2L, 3.0)),
                        "almacen@mobilesco.mx"));
    }

    private RequisicionCreateDTO request(RequisicionPartidaRequestDTO... partidas) {
        RequisicionCreateDTO dto = new RequisicionCreateDTO();
        dto.setPartidas(List.of(partidas));
        return dto;
    }

    private RequisicionPartidaRequestDTO partida(Long insumoId, Double cantidad) {
        RequisicionPartidaRequestDTO dto = new RequisicionPartidaRequestDTO();
        dto.setInsumoId(insumoId);
        dto.setCantidadSolicitada(cantidad);
        return dto;
    }
}
