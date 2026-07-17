package com.mobilesco.mobilesco_back.modules.modelo.application.usecases;

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

import com.mobilesco.mobilesco_back.modules.categoria.domain.models.CategoriaModel;
import com.mobilesco.mobilesco_back.modules.categoria.infrastructure.out.persistence.repositories.CategoriaRepository;
import com.mobilesco.mobilesco_back.modules.familia.domain.models.FamiliaModel;
import com.mobilesco.mobilesco_back.modules.familia.infrastructure.out.persistence.repositories.FamiliaRepository;
import com.mobilesco.mobilesco_back.modules.imagen.application.usecases.AlmacenamientoImagenesService;
import com.mobilesco.mobilesco_back.modules.material.infrastructure.out.persistence.repositories.MaterialRepository;
import com.mobilesco.mobilesco_back.modules.modelo.domain.models.ModeloModel;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloCategoriaDTO;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloCreateDTO;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloResponseDTO;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.out.persistence.repositories.ModeloRepository;
import com.mobilesco.mobilesco_back.modules.nivel.domain.models.NivelModel;
import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.out.persistence.repositories.NivelRepository;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.modules.subfamilia.infrastructure.out.persistence.repositories.SubfamiliaRepository;

class ModeloServiceTest {

    private ModeloRepository modeloRepository;
    private FamiliaRepository familiaRepository;
    private MaterialRepository materialRepository;
    private SubfamiliaRepository subfamiliaRepository;
    private ProductoRepository productoRepository;
    private NivelRepository nivelRepository;
    private CategoriaRepository categoriaRepository;
    private AlmacenamientoImagenesService almacenamientoImagenesService;
    private ModeloService modeloService;

    @BeforeEach
    void setUp() {
        modeloRepository = mock(ModeloRepository.class);
        familiaRepository = mock(FamiliaRepository.class);
        materialRepository = mock(MaterialRepository.class);
        subfamiliaRepository = mock(SubfamiliaRepository.class);
        productoRepository = mock(ProductoRepository.class);
        nivelRepository = mock(NivelRepository.class);
        categoriaRepository = mock(CategoriaRepository.class);
        almacenamientoImagenesService = mock(AlmacenamientoImagenesService.class);
        modeloService = new ModeloService(
                modeloRepository,
                familiaRepository,
                materialRepository,
                subfamiliaRepository,
                productoRepository,
                nivelRepository,
                categoriaRepository,
                almacenamientoImagenesService);
    }

    @Test
    void permiteRepetirNombreDeModeloEnFamiliasDistintas() {
        FamiliaModel familia = familia(20L, "Sillas");
        CategoriaModel categoria = categoria(30L, "Escolar");
        when(familiaRepository.findById(20L)).thenReturn(Optional.of(familia));
        when(familiaRepository.existsById(20L)).thenReturn(true);
        when(modeloRepository.existsByFamiliaIdAndNombreIgnoreCase(20L, "ISO")).thenReturn(false);
        when(modeloRepository.findByFamiliaId(20L)).thenReturn(List.of());
        when(modeloRepository.save(any(ModeloModel.class))).thenAnswer(invocation -> {
            ModeloModel modelo = invocation.getArgument(0);
            if (modelo.getId() == null) {
                modelo.setId(40L);
            }
            return modelo;
        });
        when(nivelRepository.findByModeloIdOrderByCodigoAsc(40L)).thenReturn(List.of());
        when(categoriaRepository.findByNombreIgnoreCase("Escolar")).thenReturn(Optional.of(categoria));
        when(nivelRepository.save(any(NivelModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ModeloResponseDTO resultado = modeloService.crear(modelo("ISO", 20L));

        assertEquals("ISO", resultado.getNombre());
        assertEquals(20L, resultado.getFamiliaId());
        verify(modeloRepository).existsByFamiliaIdAndNombreIgnoreCase(20L, "ISO");
        verify(modeloRepository, never()).findAll();
    }

    @Test
    void validaElNombreDeModeloDentroDeLaMismaFamilia() {
        FamiliaModel familia = familia(20L, "Sillas");
        when(familiaRepository.findById(20L)).thenReturn(Optional.of(familia));
        when(modeloRepository.existsByFamiliaIdAndNombreIgnoreCase(20L, "ISO")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> modeloService.crear(modelo("ISO", 20L)));

        verify(modeloRepository).existsByFamiliaIdAndNombreIgnoreCase(20L, "ISO");
        verify(modeloRepository, never()).save(any(ModeloModel.class));
    }

    private ModeloCreateDTO modelo(String nombre, Long familiaId) {
        ModeloCategoriaDTO categoria = new ModeloCategoriaDTO();
        categoria.setNombre("Escolar");

        ModeloCreateDTO dto = new ModeloCreateDTO();
        dto.setNombre(nombre);
        dto.setFamiliaId(familiaId);
        dto.setCategorias(List.of(categoria));
        return dto;
    }

    private FamiliaModel familia(Long id, String nombre) {
        FamiliaModel familia = new FamiliaModel();
        familia.setId(id);
        familia.setCodigo("S");
        familia.setNombre(nombre);
        return familia;
    }

    private CategoriaModel categoria(Long id, String nombre) {
        CategoriaModel categoria = new CategoriaModel();
        categoria.setId(id);
        categoria.setNombre(nombre);
        categoria.setActivo(true);
        return categoria;
    }
}
