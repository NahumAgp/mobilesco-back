package com.mobilesco.mobilesco_back.modules.familia.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mobilesco.mobilesco_back.modules.familia.application.ports.FamiliaPersistencePort;
import com.mobilesco.mobilesco_back.modules.familia.application.ports.LineaLookupPort;
import com.mobilesco.mobilesco_back.modules.familia.application.ports.ModeloFamiliaValidationPort;
import com.mobilesco.mobilesco_back.modules.familia.domain.models.FamiliaModel;
import com.mobilesco.mobilesco_back.modules.familia.infrastructure.in.api.dtos.FamiliaCreateDTO;
import com.mobilesco.mobilesco_back.modules.familia.infrastructure.in.api.dtos.FamiliaResponseDTO;
import com.mobilesco.mobilesco_back.modules.familia.infrastructure.in.api.dtos.FamiliaUpdateDTO;
import com.mobilesco.mobilesco_back.modules.linea.domain.models.LineaModel;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.BadRequestException;

class FamiliaServiceImplTest {

    private FamiliaPersistencePort familiaRepository;
    private LineaLookupPort lineaRepository;
    private ModeloFamiliaValidationPort modeloRepository;
    private FamiliaServiceImpl familiaService;

    @BeforeEach
    void setUp() {
        familiaRepository = mock(FamiliaPersistencePort.class);
        lineaRepository = mock(LineaLookupPort.class);
        modeloRepository = mock(ModeloFamiliaValidationPort.class);
        familiaService = new FamiliaServiceImpl(familiaRepository, lineaRepository, modeloRepository);
    }

    @Test
    void permiteCambiarSoloMayusculasEnElNombreSinMarcarDuplicado() {
        LineaModel linea = linea(10L, "ESC", "Escolar");
        FamiliaModel existente = new FamiliaModel();
        existente.setId(1L);
        existente.setCodigo("S");
        existente.setNombre("Sillas");
        existente.setLinea(linea);

        when(familiaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(familiaRepository.existsByLineaIdAndCodigoIgnoreCaseAndIdNot(10L, "S", 1L)).thenReturn(false);
        when(familiaRepository.existsByLineaIdAndNombreIgnoreCaseAndIdNot(10L, "SILLAS", 1L)).thenReturn(false);
        when(familiaRepository.save(any(FamiliaModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FamiliaUpdateDTO dto = new FamiliaUpdateDTO();
        dto.setCodigo("S");
        dto.setNombre("SILLAS");

        FamiliaResponseDTO resultado = familiaService.actualizar(1L, dto);

        assertEquals("SILLAS", resultado.getNombre());
        verify(familiaRepository).existsByLineaIdAndNombreIgnoreCaseAndIdNot(10L, "SILLAS", 1L);
        verify(familiaRepository, never()).existsByNombre("SILLAS");
    }

    @Test
    void permiteRepetirNombreDeFamiliaEnLineasDistintas() {
        LineaModel oficina = linea(20L, "OFI", "Oficina");
        when(lineaRepository.findById(20L)).thenReturn(Optional.of(oficina));
        when(lineaRepository.existsById(20L)).thenReturn(true);
        when(familiaRepository.existsByLineaIdAndNombreIgnoreCase(20L, "Sillas")).thenReturn(false);
        when(familiaRepository.findByLineaId(20L)).thenReturn(List.of());
        when(familiaRepository.save(any(FamiliaModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FamiliaCreateDTO dto = new FamiliaCreateDTO();
        dto.setNombre("Sillas");
        dto.setLineaId(20L);
        dto.setDescripcion("Sillas de oficina");

        FamiliaResponseDTO resultado = familiaService.crear(dto);

        assertEquals("Sillas", resultado.getNombre());
        assertEquals(20L, resultado.getLineaId());
        verify(familiaRepository).existsByLineaIdAndNombreIgnoreCase(20L, "Sillas");
        verify(familiaRepository, never()).existsByNombreIgnoreCase("Sillas");
    }

    @Test
    void validaElNombreDentroDeLaMismaLineaAlCrear() {
        LineaModel escolar = linea(10L, "ESC", "Escolar");
        when(lineaRepository.findById(10L)).thenReturn(Optional.of(escolar));
        when(familiaRepository.existsByLineaIdAndNombreIgnoreCase(10L, "Sillas")).thenReturn(true);

        FamiliaCreateDTO dto = new FamiliaCreateDTO();
        dto.setNombre("Sillas");
        dto.setLineaId(10L);
        dto.setDescripcion("Descripcion");

        assertThrows(BadRequestException.class, () -> familiaService.crear(dto));

        verify(familiaRepository).existsByLineaIdAndNombreIgnoreCase(10L, "Sillas");
        verify(familiaRepository, never()).save(any(FamiliaModel.class));
    }

    private LineaModel linea(Long id, String codigo, String nombre) {
        LineaModel linea = new LineaModel();
        linea.setId(id);
        linea.setCodigo(codigo);
        linea.setNombre(nombre);
        return linea;
    }
}
