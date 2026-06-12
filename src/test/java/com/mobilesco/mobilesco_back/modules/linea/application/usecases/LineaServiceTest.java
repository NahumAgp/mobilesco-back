package com.mobilesco.mobilesco_back.modules.linea.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mobilesco.mobilesco_back.modules.familia.infrastructure.out.persistence.repositories.FamiliaRepository;
import com.mobilesco.mobilesco_back.modules.linea.domain.models.LineaModel;
import com.mobilesco.mobilesco_back.modules.linea.infrastructure.in.api.dtos.LineaCreateDTO;
import com.mobilesco.mobilesco_back.modules.linea.infrastructure.in.api.dtos.LineaResponseDTO;
import com.mobilesco.mobilesco_back.modules.linea.infrastructure.in.api.dtos.LineaUpdateDTO;
import com.mobilesco.mobilesco_back.modules.linea.infrastructure.out.persistence.repositories.LineaRepository;

class LineaServiceTest {

    private LineaRepository lineaRepository;
    private LineaService lineaService;

    @BeforeEach
    void setUp() {
        lineaRepository = mock(LineaRepository.class);
        FamiliaRepository familiaRepository = mock(FamiliaRepository.class);
        lineaService = new LineaService(lineaRepository, familiaRepository);
    }

    @Test
    void sugiereElCodigoMasCortoDisponible() {
        when(lineaRepository.findAllCodigos()).thenReturn(List.of("E", "ES"));

        assertEquals("ESC", lineaService.sugerirCodigo("Escolar"));
    }

    @Test
    void cambiaElTercerCaracterCuandoLosCodigosNaturalesEstanOcupados() {
        when(lineaRepository.findAllCodigos()).thenReturn(List.of("E", "ES", "ESC"));

        assertEquals("ESA", lineaService.sugerirCodigo("Escolar"));
    }

    @Test
    void generaElCodigoEnElServidorAlCrear() {
        when(lineaRepository.findAllCodigos()).thenReturn(List.of());
        when(lineaRepository.save(any(LineaModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LineaCreateDTO dto = new LineaCreateDTO();
        dto.setCodigo("CODIGO-IGNORADO");
        dto.setNombre("Áreas especiales");

        lineaService.crear(dto);

        ArgumentCaptor<LineaModel> captor = ArgumentCaptor.forClass(LineaModel.class);
        verify(lineaRepository).save(captor.capture());
        assertEquals("A", captor.getValue().getCodigo());
    }

    @Test
    void permiteCambiarSoloMayusculasEnElNombreSinMarcarDuplicado() {
        LineaModel existente = new LineaModel();
        existente.setId(1L);
        existente.setCodigo("ESC");
        existente.setNombre("Escolar");

        when(lineaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(lineaRepository.existsByNombreIgnoreCaseAndIdNot("ESCOLAR", 1L)).thenReturn(false);

        LineaUpdateDTO dto = new LineaUpdateDTO();
        dto.setCodigo("ESC");
        dto.setNombre("ESCOLAR");

        when(lineaRepository.save(any(LineaModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LineaResponseDTO resultado = lineaService.actualizar(1L, dto);

        assertEquals("ESCOLAR", resultado.getNombre());
        verify(lineaRepository).existsByNombreIgnoreCaseAndIdNot("ESCOLAR", 1L);
        verify(lineaRepository, never()).existsByNombre("ESCOLAR");
    }
}
