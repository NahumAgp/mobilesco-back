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

import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mobilesco.mobilesco_back.modules.categoria.domain.models.CategoriaModel;
import com.mobilesco.mobilesco_back.modules.categoria.infrastructure.out.persistence.repositories.CategoriaRepository;
import com.mobilesco.mobilesco_back.modules.familia.domain.models.FamiliaModel;
import com.mobilesco.mobilesco_back.modules.familia.infrastructure.out.persistence.repositories.FamiliaRepository;
import com.mobilesco.mobilesco_back.modules.imagen.application.usecases.AlmacenamientoImagenesService;
import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.out.persistence.repositories.InsumoRepository;
import com.mobilesco.mobilesco_back.modules.material.domain.models.MaterialModel;
import com.mobilesco.mobilesco_back.modules.material.infrastructure.out.persistence.repositories.MaterialRepository;
import com.mobilesco.mobilesco_back.modules.modelo.domain.models.ModeloModel;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloCategoriaDTO;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloCreateDTO;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloInsumoDTO;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloOperacionDTO;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloResponseDTO;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.out.persistence.repositories.ModeloRepository;
import com.mobilesco.mobilesco_back.modules.nivel.domain.models.NivelModel;
import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.out.persistence.repositories.NivelInsumoRepository;
import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.out.persistence.repositories.NivelOperacionRepository;
import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.out.persistence.repositories.NivelRepository;
import com.mobilesco.mobilesco_back.modules.operacion.domain.models.OperacionModel;
import com.mobilesco.mobilesco_back.modules.operacion.infrastructure.out.persistence.repositories.OperacionRepository;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoInsumoModel;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoModel;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoOperacionModel;
import com.mobilesco.mobilesco_back.modules.producto.application.usecases.ProductoPlantillaModeloService;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoInsumoRepository;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoOperacionRepository;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.modules.subfamilia.domain.models.SubfamiliaModel;
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
    private InsumoRepository insumoRepository;
    private OperacionRepository operacionRepository;
    private NivelInsumoRepository nivelInsumoRepository;
    private NivelOperacionRepository nivelOperacionRepository;
    private ProductoInsumoRepository productoInsumoRepository;
    private ProductoOperacionRepository productoOperacionRepository;
    private ProductoPlantillaModeloService productoPlantillaModeloService;
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
        insumoRepository = mock(InsumoRepository.class);
        operacionRepository = mock(OperacionRepository.class);
        nivelInsumoRepository = mock(NivelInsumoRepository.class);
        nivelOperacionRepository = mock(NivelOperacionRepository.class);
        productoInsumoRepository = mock(ProductoInsumoRepository.class);
        productoOperacionRepository = mock(ProductoOperacionRepository.class);
        productoPlantillaModeloService = mock(ProductoPlantillaModeloService.class);
        modeloService = new ModeloService(
                modeloRepository,
                familiaRepository,
                materialRepository,
                subfamiliaRepository,
                productoRepository,
                nivelRepository,
                categoriaRepository,
                almacenamientoImagenesService,
                insumoRepository,
                operacionRepository,
                nivelInsumoRepository,
                nivelOperacionRepository,
                productoInsumoRepository,
                productoOperacionRepository,
                productoPlantillaModeloService);
    }

    @Test
    void permiteRepetirNombreDeModeloEnFamiliasDistintas() {
        FamiliaModel familia = familia(20L, "Sillas");
        CategoriaModel categoria = categoria(30L, "Escolar");
        when(familiaRepository.findById(20L)).thenReturn(Optional.of(familia));
        when(familiaRepository.existsById(20L)).thenReturn(true);
        when(modeloRepository.existsByFamiliaIdAndSubfamiliaIsNullAndNombreIgnoreCase(20L, "ISO")).thenReturn(false);
        when(modeloRepository.findByFamiliaIdAndSubfamiliaIsNull(20L)).thenReturn(List.of());
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
        verify(modeloRepository).existsByFamiliaIdAndSubfamiliaIsNullAndNombreIgnoreCase(20L, "ISO");
        verify(modeloRepository, never()).findAll();
    }

    @Test
    void validaElNombreDeModeloDentroDeLaMismaFamilia() {
        FamiliaModel familia = familia(20L, "Sillas");
        when(familiaRepository.findById(20L)).thenReturn(Optional.of(familia));
        when(modeloRepository.existsByFamiliaIdAndSubfamiliaIsNullAndNombreIgnoreCase(20L, "ISO")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> modeloService.crear(modelo("ISO", 20L)));

        verify(modeloRepository).existsByFamiliaIdAndSubfamiliaIsNullAndNombreIgnoreCase(20L, "ISO");
        verify(modeloRepository, never()).save(any(ModeloModel.class));
    }

    @Test
    void permiteRepetirNombreDeModeloEnSubfamiliasDistintas() {
        FamiliaModel familia = familia(20L, "Pupitre");
        SubfamiliaModel subfamilia = new SubfamiliaModel();
        subfamilia.setId(21L);
        subfamilia.setFamilia(familia);
        CategoriaModel categoria = categoria(30L, "Escolar");
        ModeloCreateDTO dto = modelo("Mesabanco", 20L);
        dto.setSubfamiliaId(21L);

        when(familiaRepository.findById(20L)).thenReturn(Optional.of(familia));
        when(familiaRepository.existsById(20L)).thenReturn(true);
        when(subfamiliaRepository.findById(21L)).thenReturn(Optional.of(subfamilia));
        when(modeloRepository.findBySubfamiliaId(21L)).thenReturn(List.of());
        when(modeloRepository.save(any(ModeloModel.class))).thenAnswer(invocation -> {
            ModeloModel modelo = invocation.getArgument(0);
            modelo.setId(40L);
            return modelo;
        });
        when(nivelRepository.findByModeloIdOrderByCodigoAsc(40L)).thenReturn(List.of());
        when(categoriaRepository.findByNombreIgnoreCase("Escolar")).thenReturn(Optional.of(categoria));
        when(nivelRepository.save(any(NivelModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ModeloResponseDTO resultado = modeloService.crear(dto);

        assertEquals(21L, resultado.getSubfamiliaId());
        verify(modeloRepository).existsBySubfamiliaIdAndNombreIgnoreCase(21L, "Mesabanco");
    }

    @Test
    void muestraMaterialesUsadosPorVariantesAunqueElModeloNoLosTengaAsociados() {
        ModeloModel modelo = new ModeloModel();
        modelo.setId(40L);
        modelo.setCodigo("ME");
        modelo.setNombre("Mesa Binaria");
        modelo.setActivo(true);

        MaterialModel formica = material(7L, "F", "Formica");
        MaterialModel triplay = material(9L, "T", "Triplay");

        when(modeloRepository.findById(40L)).thenReturn(Optional.of(modelo));
        when(nivelRepository.findByModeloIdOrderByCodigoAsc(40L)).thenReturn(List.of());
        when(productoRepository.findMaterialesByModeloId(40L)).thenReturn(List.of(formica, triplay));

        ModeloResponseDTO resultado = modeloService.obtenerPorId(40L);

        assertEquals(List.of(7L, 9L), resultado.getMateriales().stream()
                .map(material -> material.getId())
                .toList());
    }

    @Test
    void sincronizarInsumosVariantesReemplazaInsumosExistentesDeLaVariante() {
        ModeloModel modelo = modeloPersistido(40L);
        NivelModel nivel = nivel(70L, modelo);
        ProductoModel producto = producto(90L, modelo, nivel);
        InsumoModel plantilla = insumo(11L, "Tubo");
        ProductoInsumoModel existente = productoInsumo(1L, producto, plantilla, 9.0, 0.0, "Manual");
        ProductoInsumoModel sobrante = productoInsumo(2L, producto, insumo(12L, "Sobrante"), 3.0, 0.0, "Manual");

        when(modeloRepository.findById(40L)).thenReturn(Optional.of(modelo));
        when(nivelRepository.findById(70L)).thenReturn(Optional.of(nivel));
        when(insumoRepository.findById(11L)).thenReturn(Optional.of(plantilla));
        when(productoRepository.findByModeloIdAndNivelId(40L, 70L)).thenReturn(List.of(producto));
        when(productoInsumoRepository.findByProductoId(90L)).thenReturn(List.of(existente, sobrante));

        modeloService.sincronizarInsumosVariantes(40L, 70L, null, List.of(
                ModeloInsumoDTO.builder()
                        .id(11L)
                        .cantidad(2.0)
                        .desperdicioPorcentaje(5.0)
                        .build()));

        ArgumentCaptor<List<ProductoInsumoModel>> eliminados = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<ProductoInsumoModel>> guardados = ArgumentCaptor.forClass(List.class);
        verify(productoInsumoRepository).deleteAll(eliminados.capture());
        verify(productoInsumoRepository).saveAll(guardados.capture());

        assertEquals(List.of(sobrante), eliminados.getValue());
        assertEquals(1, guardados.getValue().size());
        ProductoInsumoModel actualizado = guardados.getValue().get(0);
        assertEquals(2.0, actualizado.getCantidad());
        assertEquals(5.0, actualizado.getDesperdicioPorcentaje());
        assertEquals("Heredado de la categoria del modelo", actualizado.getObservaciones());
    }

    @Test
    void sincronizarOperacionesVariantesReemplazaOperacionesExistentesDeLaVariante() {
        ModeloModel modelo = modeloPersistido(40L);
        NivelModel nivel = nivel(70L, modelo);
        ProductoModel producto = producto(90L, modelo, nivel);
        OperacionModel corte = operacion(31L, "Corte", 0.5, 1.0);
        OperacionModel soldadura = operacion(32L, "Soldadura", 0.25, 2.0);
        ProductoOperacionModel existente = productoOperacion(1L, producto, corte, 8, 9, "Manual");
        ProductoOperacionModel sobrante = productoOperacion(2L, producto, soldadura, 3, 2, "Manual");

        when(modeloRepository.findById(40L)).thenReturn(Optional.of(modelo));
        when(nivelRepository.findById(70L)).thenReturn(Optional.of(nivel));
        when(operacionRepository.findById(31L)).thenReturn(Optional.of(corte));
        when(productoRepository.findByModeloIdAndNivelId(40L, 70L)).thenReturn(List.of(producto));
        when(productoOperacionRepository.findByProductoIdOrderByOrdenAsc(90L)).thenReturn(List.of(existente, sobrante));

        modeloService.sincronizarOperacionesVariantes(40L, 70L, List.of(
                ModeloOperacionDTO.builder()
                        .id(31L)
                        .cantidad(2)
                        .build()));

        ArgumentCaptor<List<ProductoOperacionModel>> eliminadas = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<ProductoOperacionModel>> guardadas = ArgumentCaptor.forClass(List.class);
        verify(productoOperacionRepository).deleteAll(eliminadas.capture());
        verify(productoOperacionRepository).saveAll(guardadas.capture());

        assertEquals(List.of(sobrante), eliminadas.getValue());
        assertEquals(1, guardadas.getValue().size());
        ProductoOperacionModel actualizada = guardadas.getValue().get(0);
        assertEquals(2, actualizada.getCantidad());
        assertEquals(1, actualizada.getOrden());
        assertEquals("Heredado de la categoria del modelo", actualizada.getObservaciones());
        assertEquals(Boolean.TRUE, actualizada.getActivo());
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

    private MaterialModel material(Long id, String codigo, String nombre) {
        MaterialModel material = new MaterialModel();
        material.setId(id);
        material.setCodigo(codigo);
        material.setNombre(nombre);
        material.setActivo(true);
        return material;
    }

    private ModeloModel modeloPersistido(Long id) {
        ModeloModel modelo = new ModeloModel();
        modelo.setId(id);
        modelo.setCodigo("MOD");
        modelo.setNombre("Modelo");
        modelo.setActivo(true);
        return modelo;
    }

    private NivelModel nivel(Long id, ModeloModel modelo) {
        NivelModel nivel = new NivelModel();
        nivel.setId(id);
        nivel.setNombre("Primaria");
        nivel.setModelo(modelo);
        return nivel;
    }

    private ProductoModel producto(Long id, ModeloModel modelo, NivelModel nivel) {
        ProductoModel producto = new ProductoModel();
        producto.setId(id);
        producto.setSku("SKU-" + id);
        producto.setNombre("Producto " + id);
        producto.setModelo(modelo);
        producto.setNivel(nivel);
        return producto;
    }

    private InsumoModel insumo(Long id, String nombre) {
        InsumoModel insumo = new InsumoModel();
        insumo.setId(id);
        insumo.setCodigo("INS-" + id);
        insumo.setNombre(nombre);
        return insumo;
    }

    private ProductoInsumoModel productoInsumo(
            Long id,
            ProductoModel producto,
            InsumoModel insumo,
            Double cantidad,
            Double desperdicio,
            String observaciones) {
        ProductoInsumoModel item = new ProductoInsumoModel();
        item.setId(id);
        item.setProducto(producto);
        item.setInsumo(insumo);
        item.setCantidad(cantidad);
        item.setDesperdicioPorcentaje(desperdicio);
        item.setObservaciones(observaciones);
        return item;
    }

    private OperacionModel operacion(Long id, String nombre, Double tiempo, Double costoMinuto) {
        OperacionModel operacion = new OperacionModel();
        operacion.setId(id);
        operacion.setCodigo("OP-" + id);
        operacion.setNombre(nombre);
        operacion.setTiempoOperacion(tiempo);
        operacion.setCostoMinuto(costoMinuto);
        operacion.setActivo(true);
        return operacion;
    }

    private ProductoOperacionModel productoOperacion(
            Long id,
            ProductoModel producto,
            OperacionModel operacion,
            Integer cantidad,
            Integer orden,
            String observaciones) {
        ProductoOperacionModel item = new ProductoOperacionModel();
        item.setId(id);
        item.setProducto(producto);
        item.setOperacion(operacion);
        item.setCantidad(cantidad);
        item.setOrden(orden);
        item.setObservaciones(observaciones);
        item.setActivo(true);
        return item;
    }
}
