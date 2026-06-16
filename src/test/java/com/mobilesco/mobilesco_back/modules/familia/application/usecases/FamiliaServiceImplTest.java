package com.mobilesco.mobilesco_back.modules.familia.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        FamiliaModel existente = new FamiliaModel();
        existente.setId(1L);
        existente.setCodigo("ESC");
        existente.setNombre("Escolar");

        when(familiaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(familiaRepository.existsByNombreIgnoreCaseAndIdNot("ESCOLAR", 1L)).thenReturn(false);
        when(familiaRepository.save(any(FamiliaModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FamiliaUpdateDTO dto = new FamiliaUpdateDTO();
        dto.setCodigo("ESC");
        dto.setNombre("ESCOLAR");

        FamiliaResponseDTO resultado = familiaService.actualizar(1L, dto);

        assertEquals("ESCOLAR", resultado.getNombre());
        verify(familiaRepository).existsByNombreIgnoreCaseAndIdNot("ESCOLAR", 1L);
        verify(familiaRepository, never()).existsByNombre("ESCOLAR");
    }

    @Test
    void validaElNombreSinDistinguirMayusculasAlCrear() {
        when(familiaRepository.existsByNombreIgnoreCase("ESCOLAR")).thenReturn(true);

        FamiliaCreateDTO dto = new FamiliaCreateDTO();
        dto.setNombre("ESCOLAR");
        dto.setLineaId(10L);
        dto.setDescripcion("Descripcion");

        assertThrows(BadRequestException.class, () -> familiaService.crear(dto));

        verify(familiaRepository).existsByNombreIgnoreCase("ESCOLAR");
        verify(familiaRepository, never()).save(any(FamiliaModel.class));
    }
}
