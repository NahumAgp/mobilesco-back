package com.mobilesco.mobilesco_back.modules.linea.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mobilesco.mobilesco_back.modules.familia.infrastructure.out.persistence.repositories.FamiliaRepository;
import com.mobilesco.mobilesco_back.modules.linea.domain.models.LineaModel;
import com.mobilesco.mobilesco_back.modules.linea.infrastructure.in.api.dtos.LineaCreateDTO;
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
}
