package com.mobilesco.mobilesco_back.modules.producto.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mobilesco.mobilesco_back.modules.color.domain.models.ColorModel;
import com.mobilesco.mobilesco_back.modules.color.infrastructure.out.persistence.repositories.ColorRepository;
import com.mobilesco.mobilesco_back.modules.costoindirecto.application.usecases.CostoIndirectoService;
import com.mobilesco.mobilesco_back.modules.familia.domain.models.FamiliaModel;
import com.mobilesco.mobilesco_back.modules.imagen.application.usecases.ImagenService;
import com.mobilesco.mobilesco_back.modules.material.infrastructure.out.persistence.repositories.MaterialRepository;
import com.mobilesco.mobilesco_back.modules.modelo.domain.models.ModeloModel;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.out.persistence.repositories.ModeloRepository;
import com.mobilesco.mobilesco_back.modules.nivel.domain.models.NivelModel;
import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.out.persistence.repositories.NivelRepository;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoModel;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.CatalogoFacetasDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ModeloCatalogoPublicoDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoInsumoRepository;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoOperacionRepository;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoRepository;
import com.mobilesco.mobilesco_back.modules.subfamilia.domain.models.SubfamiliaModel;

@ExtendWith(MockitoExtension.class)
class ProductoServiceCatalogoPublicoTest {

    @Mock ProductoRepository productoRepository;
    @Mock ModeloRepository modeloRepository;
    @Mock NivelRepository nivelRepository;
    @Mock ColorRepository colorRepository;
    @Mock MaterialRepository materialRepository;
    @Mock ProductoInsumoRepository productoInsumoRepository;
    @Mock ProductoOperacionRepository productoOperacionRepository;
    @Mock ImagenService imagenService;
    @Mock CostoIndirectoService costoIndirectoService;
    @Mock ProductoPlantillaModeloService productoPlantillaModeloService;

    ProductoService service;

    FamiliaModel familiaRoperos;
    FamiliaModel familiaSalas;
    SubfamiliaModel subfamiliaTresPuertas;
    SubfamiliaModel subfamiliaDosPuertas;
    ColorModel negro;
    ColorModel blanco;
    NivelModel grande;
    NivelModel chico;
    ModeloModel ropero;
    ModeloModel roperoDos;
    ModeloModel sala;

    @BeforeEach
    void setUp() {
        service = new ProductoService(productoRepository, modeloRepository, nivelRepository,
                colorRepository, materialRepository, productoInsumoRepository,
                productoOperacionRepository, imagenService, costoIndirectoService,
                productoPlantillaModeloService);

        familiaRoperos = familia(1L, "Roperos");
        familiaSalas = familia(2L, "Salas");
        subfamiliaTresPuertas = subfamilia(7L, "Roperos 3 puertas");
        subfamiliaDosPuertas = subfamilia(8L, "Roperos 2 puertas");
        negro = color(1L, "Negro", "#000000");
        blanco = color(2L, "Blanco", "#FFFFFF");
        grande = nivel(2L, "Grande");
        chico = nivel(3L, "Chico");

        ropero = modelo(10L, "Ropero Monarca", familiaRoperos, subfamiliaTresPuertas);
        roperoDos = modelo(11L, "Ropero Colonial", familiaRoperos, subfamiliaDosPuertas);
        sala = modelo(12L, "Sala Praga", familiaSalas, null);
    }

    @Test
    void listaSinFiltrosDevuelveTodosLosModelosActivos() {
        when(modeloRepository.findByActivo(true)).thenReturn(List.of(ropero, roperoDos, sala));
        when(productoRepository.findByActivoTrue()).thenReturn(List.of());

        List<ModeloCatalogoPublicoDTO> resultado = service.listarCatalogoPublico(null, null, null, null);

        assertEquals(3, resultado.size());
    }

    @Test
    void filtraPorFamiliaYSubfamilia() {
        when(modeloRepository.findByActivo(true)).thenReturn(List.of(ropero, roperoDos, sala));
        when(productoRepository.findByActivoTrue()).thenReturn(List.of());

        List<ModeloCatalogoPublicoDTO> porFamilia = service.listarCatalogoPublico(1L, null, null, null);
        List<ModeloCatalogoPublicoDTO> porSubfamilia = service.listarCatalogoPublico(1L, 7L, null, null);

        assertEquals(2, porFamilia.size());
        assertEquals(1, porSubfamilia.size());
        assertEquals(10L, porSubfamilia.get(0).getModeloId());
    }

    @Test
    void filtroColorYTamanoExigeLaMismaVariante() {
        // ropero: variante negra-chica y blanca-grande; NO tiene negra-grande
        when(modeloRepository.findByActivo(true)).thenReturn(List.of(ropero));
        when(productoRepository.findByActivoTrue()).thenReturn(List.of(
                variante(100L, ropero, negro, chico),
                variante(101L, ropero, blanco, grande)));

        List<ModeloCatalogoPublicoDTO> negroChico = service.listarCatalogoPublico(null, null, 1L, 3L);
        List<ModeloCatalogoPublicoDTO> negroGrande = service.listarCatalogoPublico(null, null, 1L, 2L);

        assertEquals(1, negroChico.size());
        assertTrue(negroGrande.isEmpty());
    }

    @Test
    void modeloSinVariantesActivasNoEntraCuandoHayFiltroDeVariante() {
        when(modeloRepository.findByActivo(true)).thenReturn(List.of(ropero));
        when(productoRepository.findByActivoTrue()).thenReturn(List.of());

        assertTrue(service.listarCatalogoPublico(null, null, 1L, null).isEmpty());
        assertEquals(1, service.listarCatalogoPublico(null, null, null, null).size());
    }

    @Test
    void facetasCuentanModelosNoVariantes() {
        // ropero tiene DOS variantes negras: Negro debe contar 1 (un mueble), no 2
        when(modeloRepository.findByActivo(true)).thenReturn(List.of(ropero, roperoDos));
        when(productoRepository.findByActivoTrue()).thenReturn(List.of(
                variante(100L, ropero, negro, chico),
                variante(101L, ropero, negro, grande),
                variante(102L, roperoDos, blanco, grande)));

        CatalogoFacetasDTO facetas = service.obtenerFacetasPublicas(null, null);

        CatalogoFacetasDTO.ColorFacetaDTO facetaNegro = facetas.getColores().stream()
                .filter(c -> c.getId().equals(1L)).findFirst().orElseThrow();
        CatalogoFacetasDTO.TamanoFacetaDTO facetaGrande = facetas.getTamanos().stream()
                .filter(t -> t.getId().equals(2L)).findFirst().orElseThrow();

        assertEquals(1L, facetaNegro.getConteo());
        assertEquals("#000000", facetaNegro.getHex());
        assertEquals(2L, facetaGrande.getConteo());
    }

    @Test
    void facetasDeSubfamiliaRespetanSoloFamilia() {
        when(modeloRepository.findByActivo(true)).thenReturn(List.of(ropero, roperoDos, sala));
        when(productoRepository.findByActivoTrue()).thenReturn(List.of(
                variante(100L, ropero, negro, chico),
                variante(101L, roperoDos, blanco, grande)));

        // Con subfamilia 7 seleccionada, la faceta de subfamilias sigue listando las hermanas de la familia 1
        CatalogoFacetasDTO facetas = service.obtenerFacetasPublicas(1L, 7L);

        assertEquals(2, facetas.getSubfamilias().size());
        // pero colores/tamanos si se acotan a la subfamilia 7 (solo el ropero Monarca)
        assertEquals(1, facetas.getColores().size());
        assertEquals(1L, facetas.getColores().get(0).getId());
    }

    private FamiliaModel familia(Long id, String nombre) {
        FamiliaModel f = new FamiliaModel();
        f.setId(id);
        f.setNombre(nombre);
        return f;
    }

    private SubfamiliaModel subfamilia(Long id, String nombre) {
        SubfamiliaModel s = new SubfamiliaModel();
        s.setId(id);
        s.setNombre(nombre);
        return s;
    }

    private ColorModel color(Long id, String nombre, String hex) {
        ColorModel c = new ColorModel();
        c.setId(id);
        c.setNombre(nombre);
        c.setHex(hex);
        return c;
    }

    private NivelModel nivel(Long id, String nombre) {
        NivelModel n = new NivelModel();
        n.setId(id);
        n.setNombre(nombre);
        return n;
    }

    private ModeloModel modelo(Long id, String nombre, FamiliaModel familia, SubfamiliaModel subfamilia) {
        ModeloModel m = new ModeloModel();
        m.setId(id);
        m.setNombre(nombre);
        m.setActivo(true);
        m.setFamilia(familia);
        m.setSubfamilia(subfamilia);
        return m;
    }

    private ProductoModel variante(Long id, ModeloModel modelo, ColorModel color, NivelModel nivel) {
        return ProductoModel.builder()
                .id(id)
                .sku("SKU-" + id)
                .modelo(modelo)
                .color(color)
                .nivel(nivel)
                .activo(true)
                .build();
    }
}
