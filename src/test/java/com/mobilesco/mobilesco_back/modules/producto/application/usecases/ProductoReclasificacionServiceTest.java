package com.mobilesco.mobilesco_back.modules.producto.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mobilesco.mobilesco_back.modules.color.domain.models.ColorModel;
import com.mobilesco.mobilesco_back.modules.color.infrastructure.out.persistence.repositories.ColorRepository;
import com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.out.persistence.repositories.CotizacionRepository;
import com.mobilesco.mobilesco_back.modules.familia.domain.models.FamiliaModel;
import com.mobilesco.mobilesco_back.modules.familia.infrastructure.out.persistence.repositories.FamiliaRepository;
import com.mobilesco.mobilesco_back.modules.linea.domain.models.LineaModel;
import com.mobilesco.mobilesco_back.modules.linea.infrastructure.out.persistence.repositories.LineaRepository;
import com.mobilesco.mobilesco_back.modules.material.domain.models.MaterialModel;
import com.mobilesco.mobilesco_back.modules.material.infrastructure.out.persistence.repositories.MaterialRepository;
import com.mobilesco.mobilesco_back.modules.modelo.domain.models.ModeloModel;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.out.persistence.repositories.ModeloRepository;
import com.mobilesco.mobilesco_back.modules.nivel.domain.models.NivelModel;
import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.out.persistence.repositories.NivelRepository;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoModel;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoReclasificacionRequestDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoReclasificacionResponseDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.modules.subfamilia.domain.models.SubfamiliaModel;
import com.mobilesco.mobilesco_back.modules.subfamilia.infrastructure.out.persistence.repositories.SubfamiliaRepository;

@ExtendWith(MockitoExtension.class)
class ProductoReclasificacionServiceTest {

    @Mock ProductoRepository productoRepository;
    @Mock ModeloRepository modeloRepository;
    @Mock LineaRepository lineaRepository;
    @Mock FamiliaRepository familiaRepository;
    @Mock SubfamiliaRepository subfamiliaRepository;
    @Mock NivelRepository nivelRepository;
    @Mock MaterialRepository materialRepository;
    @Mock ColorRepository colorRepository;
    @Mock CotizacionRepository cotizacionRepository;

    private ProductoReclasificacionService service;
    private ProductoReclasificacionRequestDTO request;
    private ModeloModel modelo;
    private FamiliaModel familiaActual;
    private FamiliaModel familiaDestino;
    private SubfamiliaModel subfamiliaDestino;
    private ProductoModel producto;
    private NivelModel nivel;
    private MaterialModel material;
    private ColorModel color;

    @BeforeEach
    void setUp() {
        service = new ProductoReclasificacionService(
                productoRepository, modeloRepository, lineaRepository,
                familiaRepository, subfamiliaRepository, nivelRepository,
                materialRepository, colorRepository, cotizacionRepository);

        LineaModel linea = new LineaModel();
        linea.setId(1L);
        linea.setCodigo("E");
        linea.setNombre("Escolar");

        familiaActual = new FamiliaModel();
        familiaActual.setId(2L);
        familiaActual.setCodigo("MB");
        familiaActual.setNombre("Mesabancos");
        familiaActual.setLinea(linea);

        familiaDestino = new FamiliaModel();
        familiaDestino.setId(3L);
        familiaDestino.setCodigo("PU");
        familiaDestino.setNombre("Pupitre");
        familiaDestino.setLinea(linea);

        subfamiliaDestino = new SubfamiliaModel();
        subfamiliaDestino.setId(4L);
        subfamiliaDestino.setCodigo("ME");
        subfamiliaDestino.setNombre("Mesabanco");
        subfamiliaDestino.setFamilia(familiaDestino);

        modelo = new ModeloModel();
        modelo.setId(5L);
        modelo.setCodigo("M");
        modelo.setNombre("Mesabanco Concha");
        modelo.setFamilia(familiaActual);

        nivel = new NivelModel();
        nivel.setId(6L);
        nivel.setCodigo("01");
        nivel.setModelo(modelo);

        material = new MaterialModel();
        material.setId(7L);
        material.setCodigo("P");

        color = new ColorModel();
        color.setId(8L);
        color.setCodigo("AM");

        producto = ProductoModel.builder()
                .id(9L)
                .sku("EMBM-01-P-AM")
                .nombre("Mesabanco amarillo")
                .modelo(modelo)
                .nivel(nivel)
                .material(material)
                .color(color)
                .build();

        request = new ProductoReclasificacionRequestDTO();
        request.setLineaId(1L);
        request.setFamiliaId(3L);
        request.setSubfamiliaId(4L);
        request.setModeloId(5L);
        request.setNivelId(6L);
        request.setMaterialId(7L);
        request.setColorId(8L);

        when(productoRepository.findById(9L)).thenReturn(Optional.of(producto));
        when(modeloRepository.findById(5L)).thenReturn(Optional.of(modelo));
        when(nivelRepository.findById(6L)).thenReturn(Optional.of(nivel));
        when(materialRepository.findById(7L)).thenReturn(Optional.of(material));
        lenient().when(colorRepository.findById(8L)).thenReturn(Optional.of(color));
        when(lineaRepository.findById(1L)).thenReturn(Optional.of(linea));
        lenient().when(familiaRepository.findById(3L)).thenReturn(Optional.of(familiaDestino));
        lenient().when(subfamiliaRepository.findById(4L)).thenReturn(Optional.of(subfamiliaDestino));
    }

    @Test
    void previsualizaSoloRutaYConservaModeloCategoriaMaterialYColor() {
        prepararVariantes();
        when(cotizacionRepository.existsByProductoIdsInCotizaciones(List.of(9L))).thenReturn(false);

        ProductoReclasificacionResponseDTO response = service.previsualizar(9L, request);

        assertTrue(response.isPermitido());
        assertEquals("EPUMEM-01-P-AM", response.getCambiosSku().get(0).getSkuNuevo());
        assertSame(familiaActual, modelo.getFamilia());
        assertEquals(6L, producto.getNivel().getId());
        assertEquals(7L, producto.getMaterial().getId());
        assertEquals(8L, producto.getColor().getId());
    }

    @Test
    void aplicaRutaAlMismoModeloYRecalculaSusVariantes() {
        prepararVariantes();
        when(cotizacionRepository.existsByProductoIdsInCotizaciones(List.of(9L))).thenReturn(false);

        ProductoReclasificacionResponseDTO response = service.aplicar(9L, request);

        assertTrue(response.isPermitido());
        assertSame(modelo, producto.getModelo());
        assertSame(familiaDestino, modelo.getFamilia());
        assertSame(subfamiliaDestino, modelo.getSubfamilia());
        assertEquals("EPUMEM-01-P-AM", producto.getSku());
        verify(modeloRepository).save(modelo);
        verify(productoRepository).saveAll(List.of(producto));
    }

    @Test
    void cambiaAtributosDelProductoYRecalculaSuSkuSinCambiarExistencias() {
        ColorModel colorAzul = new ColorModel();
        colorAzul.setId(10L);
        colorAzul.setCodigo("AZ");

        request.setFamiliaId(2L);
        request.setSubfamiliaId(null);
        request.setColorId(10L);

        when(familiaRepository.findById(2L)).thenReturn(Optional.of(familiaActual));
        when(colorRepository.findById(10L)).thenReturn(Optional.of(colorAzul));
        when(productoRepository.findByModeloId(5L)).thenReturn(List.of(producto));
        when(productoRepository.findBySkuIgnoreCase("EMBM-01-P-AZ")).thenReturn(Optional.empty());
        when(cotizacionRepository.existsByProductoIdsInCotizaciones(List.of(9L))).thenReturn(false);

        ProductoReclasificacionResponseDTO response = service.aplicar(9L, request);

        assertTrue(response.isPermitido());
        assertSame(colorAzul, producto.getColor());
        assertEquals("EMBM-01-P-AZ", producto.getSku());
        assertEquals(1, response.getVariantesAfectadas());
        verify(productoRepository).saveAll(List.of(producto));
    }

    @Test
    void bloqueaCuandoAlgunaVarianteTieneHistorialComercial() {
        prepararVariantes();
        when(cotizacionRepository.existsByProductoIdsInCotizaciones(List.of(9L))).thenReturn(true);

        ProductoReclasificacionResponseDTO response = service.previsualizar(9L, request);

        assertFalse(response.isPermitido());
        assertTrue(response.getMotivoBloqueo().contains("cotizaciones"));
    }

    @Test
    void rechazaSubfamiliaQueNoPerteneceALaFamilia() {
        FamiliaModel otraFamilia = new FamiliaModel();
        otraFamilia.setId(99L);
        subfamiliaDestino.setFamilia(otraFamilia);

        assertThrows(ValidationException.class, () -> service.previsualizar(9L, request));
    }

    private void prepararVariantes() {
        when(productoRepository.findByModeloId(5L)).thenReturn(List.of(producto));
        when(productoRepository.findBySkuIgnoreCase("EPUMEM-01-P-AM")).thenReturn(Optional.empty());
    }
}
