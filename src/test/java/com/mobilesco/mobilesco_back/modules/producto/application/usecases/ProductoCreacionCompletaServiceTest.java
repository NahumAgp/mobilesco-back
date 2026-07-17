package com.mobilesco.mobilesco_back.modules.producto.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.modules.color.application.usecases.ColorUseCase;
import com.mobilesco.mobilesco_back.modules.color.infrastructure.in.api.dtos.ColorResponseDTO;
import com.mobilesco.mobilesco_back.modules.familia.application.usecases.FamiliaUseCase;
import com.mobilesco.mobilesco_back.modules.familia.infrastructure.in.api.dtos.FamiliaResponseDTO;
import com.mobilesco.mobilesco_back.modules.linea.application.usecases.LineaService;
import com.mobilesco.mobilesco_back.modules.linea.infrastructure.in.api.dtos.LineaResponseDTO;
import com.mobilesco.mobilesco_back.modules.material.application.usecases.MaterialUseCase;
import com.mobilesco.mobilesco_back.modules.material.infrastructure.in.api.dtos.MaterialResponseDTO;
import com.mobilesco.mobilesco_back.modules.modelo.application.usecases.ModeloService;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloCategoriaDTO;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloResponseDTO;
import com.mobilesco.mobilesco_back.modules.nivel.application.usecases.NivelService;
import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.in.api.dtos.NivelResponseDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoCreateDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoCreacionCompletaDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoCreacionCompletaDTO.CategoriaBorradorDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoCreacionCompletaDTO.ColorBorradorDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoCreacionCompletaDTO.FamiliaBorradorDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoCreacionCompletaDTO.LineaBorradorDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoCreacionCompletaDTO.MaterialBorradorDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoCreacionCompletaDTO.ModeloBorradorDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoCreacionCompletaDTO.VarianteBorradorDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoResponseDTO;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.modules.subfamilia.application.usecases.SubfamiliaService;

class ProductoCreacionCompletaServiceTest {

    private LineaService lineaService;
    private FamiliaUseCase familiaService;
    private SubfamiliaService subfamiliaService;
    private ModeloService modeloService;
    private NivelService nivelService;
    private MaterialUseCase materialService;
    private ColorUseCase colorService;
    private ProductoService productoService;
    private ProductoCreacionCompletaService service;

    @BeforeEach
    void setUp() {
        lineaService = mock(LineaService.class);
        familiaService = mock(FamiliaUseCase.class);
        subfamiliaService = mock(SubfamiliaService.class);
        modeloService = mock(ModeloService.class);
        nivelService = mock(NivelService.class);
        materialService = mock(MaterialUseCase.class);
        colorService = mock(ColorUseCase.class);
        productoService = mock(ProductoService.class);
        service = new ProductoCreacionCompletaService(
                lineaService,
                familiaService,
                subfamiliaService,
                modeloService,
                nivelService,
                materialService,
                colorService,
                productoService);
    }

    @Test
    void creacionCompletaEstaProtegidaPorTransaccion() throws Exception {
        Method method = ProductoCreacionCompletaService.class.getMethod("crear", ProductoCreacionCompletaDTO.class);
        assertTrue(method.isAnnotationPresent(Transactional.class));
    }

    @Test
    void devuelveClientRefsDeTodosLosProductos() {
        ProductoCreacionCompletaDTO request = requestConDosVariantes();
        when(productoService.crear(any())).thenReturn(producto(10L), producto(11L));
        ModeloResponseDTO modelo = new ModeloResponseDTO();
        modelo.setId(1L);
        when(modeloService.obtenerPorId(1L)).thenReturn(modelo);

        var response = service.crear(request);

        assertEquals(List.of("var-1", "var-2"), response.getProductos().stream().map(item -> item.getClientRef()).toList());
        assertEquals(1L, response.getModelo().getId());
    }

    @Test
    void propagaFalloDeUnaVarianteParaForzarRollbackTotal() {
        ProductoCreacionCompletaDTO request = requestConDosVariantes();
        when(productoService.crear(any()))
                .thenReturn(producto(10L))
                .thenThrow(new ValidationException("SKU duplicado"));

        assertThrows(ValidationException.class, () -> service.crear(request));
        verify(productoService, times(2)).crear(any());
    }

    @Test
    void creaCadenaCompletaResolviendoReferenciasTemporales() {
        ProductoCreacionCompletaDTO request = new ProductoCreacionCompletaDTO();
        request.setLineas(List.of(linea("linea-ref")));
        request.setFamilias(List.of(familia("familia-ref", "linea-ref")));
        request.setModelo(modelo("modelo-ref", "familia-ref", categoria("categoria-ref", null)));
        request.setMateriales(List.of(material("material-ref")));
        request.setColores(List.of(color("color-ref")));
        request.setVariantes(List.of(varianteConRefs()));

        when(lineaService.crear(any())).thenReturn(lineaRespuesta(10L));
        when(familiaService.crear(any())).thenReturn(familiaRespuesta(20L));
        when(modeloService.crear(any())).thenReturn(modeloRespuesta(30L, categoriaRespuesta(40L, "Categoria nueva")));
        when(materialService.crear(any())).thenReturn(materialRespuesta(50L));
        when(colorService.crear(any())).thenReturn(colorRespuesta(60L));
        when(productoService.crear(any())).thenReturn(producto(70L));

        var response = service.crear(request);

        ArgumentCaptor<ProductoCreateDTO> productoCaptor = ArgumentCaptor.forClass(ProductoCreateDTO.class);
        verify(productoService).crear(productoCaptor.capture());
        ProductoCreateDTO payload = productoCaptor.getValue();
        assertEquals(30L, payload.getModeloId());
        assertEquals(40L, payload.getNivelId());
        assertEquals(50L, payload.getMaterialId());
        assertEquals(60L, payload.getColorId());
        assertEquals("var-completa", response.getProductos().get(0).getClientRef());
    }

    @Test
    void combinaModeloExistenteConCatalogosNuevos() {
        ProductoCreacionCompletaDTO request = new ProductoCreacionCompletaDTO();
        request.setCategorias(List.of(categoria("categoria-ref", 1L)));
        request.setMateriales(List.of(material("material-ref")));
        request.setColores(List.of(color("color-ref")));
        VarianteBorradorDTO variante = variante("var-mixta");
        variante.setCategoriaId(null);
        variante.setCategoriaRef("categoria-ref");
        variante.setMaterialId(null);
        variante.setMaterialRef("material-ref");
        variante.setColorId(null);
        variante.setColorRef("color-ref");
        request.setVariantes(List.of(variante));

        when(nivelService.crear(any())).thenReturn(nivelRespuesta(40L));
        when(materialService.crear(any())).thenReturn(materialRespuesta(50L));
        when(colorService.crear(any())).thenReturn(colorRespuesta(60L));
        when(productoService.crear(any())).thenReturn(producto(70L));
        when(modeloService.obtenerPorId(1L)).thenReturn(modeloRespuesta(1L));

        service.crear(request);

        verify(lineaService, never()).crear(any());
        verify(familiaService, never()).crear(any());
        verify(modeloService, never()).crear(any());
        verify(nivelService).crear(any());
        verify(productoService).crear(any());
    }

    private ProductoCreacionCompletaDTO requestConDosVariantes() {
        ProductoCreacionCompletaDTO request = new ProductoCreacionCompletaDTO();
        request.setVariantes(List.of(variante("var-1"), variante("var-2")));
        return request;
    }

    private VarianteBorradorDTO variante(String ref) {
        VarianteBorradorDTO variante = new VarianteBorradorDTO();
        variante.setClientRef(ref);
        variante.setNombre("Producto " + ref);
        variante.setModeloId(1L);
        variante.setCategoriaId(2L);
        variante.setMaterialId(3L);
        variante.setColorId(4L);
        return variante;
    }

    private VarianteBorradorDTO varianteConRefs() {
        VarianteBorradorDTO variante = new VarianteBorradorDTO();
        variante.setClientRef("var-completa");
        variante.setNombre("Producto completo");
        variante.setModeloRef("modelo-ref");
        variante.setCategoriaRef("categoria-ref");
        variante.setMaterialRef("material-ref");
        variante.setColorRef("color-ref");
        return variante;
    }

    private LineaBorradorDTO linea(String ref) {
        LineaBorradorDTO linea = new LineaBorradorDTO();
        linea.setRef(ref);
        linea.setNombre("Linea nueva");
        return linea;
    }

    private FamiliaBorradorDTO familia(String ref, String lineaRef) {
        FamiliaBorradorDTO familia = new FamiliaBorradorDTO();
        familia.setRef(ref);
        familia.setNombre("Familia nueva");
        familia.setLineaRef(lineaRef);
        return familia;
    }

    private ModeloBorradorDTO modelo(String ref, String familiaRef, CategoriaBorradorDTO categoria) {
        ModeloBorradorDTO modelo = new ModeloBorradorDTO();
        modelo.setRef(ref);
        modelo.setNombre("Modelo nuevo");
        modelo.setFamiliaRef(familiaRef);
        modelo.setCategorias(List.of(categoria));
        return modelo;
    }

    private CategoriaBorradorDTO categoria(String ref, Long modeloId) {
        CategoriaBorradorDTO categoria = new CategoriaBorradorDTO();
        categoria.setRef(ref);
        categoria.setNombre("Categoria nueva");
        categoria.setModeloId(modeloId);
        return categoria;
    }

    private MaterialBorradorDTO material(String ref) {
        MaterialBorradorDTO material = new MaterialBorradorDTO();
        material.setRef(ref);
        material.setNombre("Material nuevo");
        return material;
    }

    private ColorBorradorDTO color(String ref) {
        ColorBorradorDTO color = new ColorBorradorDTO();
        color.setRef(ref);
        color.setNombre("Color nuevo");
        color.setHex("#112233");
        return color;
    }

    private LineaResponseDTO lineaRespuesta(Long id) {
        LineaResponseDTO response = new LineaResponseDTO();
        response.setId(id);
        return response;
    }

    private FamiliaResponseDTO familiaRespuesta(Long id) {
        FamiliaResponseDTO response = new FamiliaResponseDTO();
        response.setId(id);
        return response;
    }

    private ModeloResponseDTO modeloRespuesta(Long id, ModeloCategoriaDTO... categorias) {
        ModeloResponseDTO response = new ModeloResponseDTO();
        response.setId(id);
        response.setCategorias(List.of(categorias));
        return response;
    }

    private ModeloCategoriaDTO categoriaRespuesta(Long id, String nombre) {
        ModeloCategoriaDTO response = new ModeloCategoriaDTO();
        response.setId(id);
        response.setNombre(nombre);
        return response;
    }

    private NivelResponseDTO nivelRespuesta(Long id) {
        NivelResponseDTO response = new NivelResponseDTO();
        response.setId(id);
        return response;
    }

    private MaterialResponseDTO materialRespuesta(Long id) {
        MaterialResponseDTO response = new MaterialResponseDTO();
        response.setId(id);
        return response;
    }

    private ColorResponseDTO colorRespuesta(Long id) {
        ColorResponseDTO response = new ColorResponseDTO();
        response.setId(id);
        return response;
    }

    private ProductoResponseDTO producto(Long id) {
        ProductoResponseDTO producto = new ProductoResponseDTO();
        producto.setId(id);
        return producto;
    }
}
