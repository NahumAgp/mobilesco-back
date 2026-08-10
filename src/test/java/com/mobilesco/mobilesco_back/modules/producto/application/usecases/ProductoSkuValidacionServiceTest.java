package com.mobilesco.mobilesco_back.modules.producto.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mobilesco.mobilesco_back.modules.color.domain.models.ColorModel;
import com.mobilesco.mobilesco_back.modules.familia.domain.models.FamiliaModel;
import com.mobilesco.mobilesco_back.modules.linea.domain.models.LineaModel;
import com.mobilesco.mobilesco_back.modules.material.domain.models.MaterialModel;
import com.mobilesco.mobilesco_back.modules.modelo.domain.models.ModeloModel;
import com.mobilesco.mobilesco_back.modules.nivel.domain.models.NivelModel;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoModel;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoSkuValidacionResponseDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoRepository;
import com.mobilesco.mobilesco_back.modules.subfamilia.domain.models.SubfamiliaModel;

@ExtendWith(MockitoExtension.class)
class ProductoSkuValidacionServiceTest {

    @Mock ProductoRepository productoRepository;

    private ProductoSkuValidacionService service;

    @BeforeEach
    void setUp() {
        service = new ProductoSkuValidacionService(productoRepository);
    }

    @Test
    void detectaSkuDescuadradoUsandoTodaLaClasificacionActual() {
        ProductoModel producto = producto(1L, "ANTERIOR");
        when(productoRepository.findAllByOrderByIdAsc()).thenReturn(List.of(producto));

        ProductoSkuValidacionResponseDTO respuesta = service.validar();

        assertEquals(1, respuesta.getInconsistentes());
        assertEquals(1, respuesta.getCorregibles());
        assertEquals("EPUMEM-01-P-AM", respuesta.getDetalles().get(0).getSkuEsperado());
        assertEquals("DESCUADRADO", respuesta.getDetalles().get(0).getEstado());
    }

    @Test
    void bloqueaClasificacionIncompletaSinInventarCodigo() {
        ProductoModel producto = producto(1L, "ANTERIOR");
        producto.setColor(null);
        when(productoRepository.findAllByOrderByIdAsc()).thenReturn(List.of(producto));

        ProductoSkuValidacionResponseDTO respuesta = service.validar();

        assertEquals(1, respuesta.getBloqueados());
        assertEquals("NO_VALIDABLE", respuesta.getDetalles().get(0).getEstado());
        assertTrue(respuesta.getDetalles().get(0).getMotivo().contains("color"));
    }

    @Test
    void detectaColisionEntreSkusCalculados() {
        ProductoModel primero = producto(1L, "SKU-1");
        ProductoModel segundo = producto(2L, "SKU-2");
        when(productoRepository.findAllByOrderByIdAsc()).thenReturn(List.of(primero, segundo));

        ProductoSkuValidacionResponseDTO respuesta = service.validar();

        assertEquals(2, respuesta.getBloqueados());
        assertTrue(respuesta.getDetalles().stream().allMatch(item -> "CONFLICTO".equals(item.getEstado())));
    }

    @Test
    void corrigeEnDosFasesParaEvitarChoquesConLaRestriccionUnica() {
        ProductoModel producto = producto(1L, "ANTERIOR");
        when(productoRepository.findAllForSkuUpdate()).thenReturn(List.of(producto));

        ProductoSkuValidacionResponseDTO respuesta = service.corregir();

        assertEquals("EPUMEM-01-P-AM", producto.getSku());
        assertEquals(1, respuesta.getActualizados());
        assertEquals(1, respuesta.getCorrectos());
        verify(productoRepository, times(2)).saveAllAndFlush(anyList());
    }

    private ProductoModel producto(Long id, String sku) {
        LineaModel linea = new LineaModel();
        linea.setId(1L);
        linea.setCodigo("E");

        FamiliaModel familia = new FamiliaModel();
        familia.setId(2L);
        familia.setCodigo("PU");
        familia.setLinea(linea);

        SubfamiliaModel subfamilia = new SubfamiliaModel();
        subfamilia.setId(3L);
        subfamilia.setCodigo("ME");
        subfamilia.setFamilia(familia);

        ModeloModel modelo = new ModeloModel();
        modelo.setId(4L);
        modelo.setCodigo("M");
        modelo.setFamilia(familia);
        modelo.setSubfamilia(subfamilia);

        NivelModel nivel = new NivelModel();
        nivel.setId(5L);
        nivel.setCodigo("01");
        nivel.setModelo(modelo);

        MaterialModel material = new MaterialModel();
        material.setId(6L);
        material.setCodigo("P");

        ColorModel color = new ColorModel();
        color.setId(7L);
        color.setCodigo("AM");

        return ProductoModel.builder()
                .id(id)
                .sku(sku)
                .nombre("Producto " + id)
                .modelo(modelo)
                .nivel(nivel)
                .material(material)
                .color(color)
                .build();
    }
}
