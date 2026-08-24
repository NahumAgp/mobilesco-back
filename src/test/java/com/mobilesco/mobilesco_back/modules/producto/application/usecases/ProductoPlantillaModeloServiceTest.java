package com.mobilesco.mobilesco_back.modules.producto.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.nivel.domain.models.NivelInsumoModel;
import com.mobilesco.mobilesco_back.modules.nivel.domain.models.NivelModel;
import com.mobilesco.mobilesco_back.modules.nivel.domain.models.NivelOperacionModel;
import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.out.persistence.repositories.NivelInsumoRepository;
import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.out.persistence.repositories.NivelOperacionRepository;
import com.mobilesco.mobilesco_back.modules.operacion.domain.models.OperacionModel;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoInsumoModel;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoModel;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoOperacionModel;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoInsumoRepository;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoOperacionRepository;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoRepository;

class ProductoPlantillaModeloServiceTest {

    private ProductoInsumoRepository productoInsumoRepository;
    private ProductoOperacionRepository productoOperacionRepository;
    private NivelInsumoRepository nivelInsumoRepository;
    private NivelOperacionRepository nivelOperacionRepository;
    private ProductoPlantillaModeloService service;

    @BeforeEach
    void setUp() {
        ProductoRepository productoRepository = mock(ProductoRepository.class);
        productoInsumoRepository = mock(ProductoInsumoRepository.class);
        productoOperacionRepository = mock(ProductoOperacionRepository.class);
        nivelInsumoRepository = mock(NivelInsumoRepository.class);
        nivelOperacionRepository = mock(NivelOperacionRepository.class);
        service = new ProductoPlantillaModeloService(
                productoRepository,
                productoInsumoRepository,
                productoOperacionRepository,
                nivelInsumoRepository,
                nivelOperacionRepository);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void heredaInsumosYOperacionesDeLaCategoriaDelProducto() {
        InsumoModel insumo = new InsumoModel();
        insumo.setId(11L);
        OperacionModel operacion = OperacionModel.builder().id(21L).build();

        NivelModel nivel = new NivelModel();
        nivel.setId(5L);

        ProductoModel producto = ProductoModel.builder().id(7L).nivel(nivel).build();
        when(nivelInsumoRepository.findByNivelIdOrderByInsumoNombreAsc(5L)).thenReturn(List.of(
                NivelInsumoModel.builder().nivel(nivel).insumo(insumo).cantidad(2.5).build()));
        when(nivelOperacionRepository.findByNivelIdOrderByOrdenAsc(5L)).thenReturn(List.of(
                NivelOperacionModel.builder().nivel(nivel).operacion(operacion).cantidad(3).orden(2).build()));
        when(productoInsumoRepository.existsByProductoIdAndInsumoId(7L, 11L)).thenReturn(false);
        when(productoOperacionRepository.existsByProductoIdAndOperacionId(7L, 21L)).thenReturn(false);
        when(productoInsumoRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(productoOperacionRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.aplicarAProducto(producto);

        ArgumentCaptor<Iterable> insumosCaptor = ArgumentCaptor.forClass(Iterable.class);
        ArgumentCaptor<Iterable> operacionesCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(productoInsumoRepository).saveAll(insumosCaptor.capture());
        verify(productoOperacionRepository).saveAll(operacionesCaptor.capture());

        ProductoInsumoModel productoInsumo = ((List<ProductoInsumoModel>) insumosCaptor.getValue()).get(0);
        ProductoOperacionModel productoOperacion = ((List<ProductoOperacionModel>) operacionesCaptor.getValue()).get(0);

        assertEquals(11L, productoInsumo.getInsumo().getId());
        assertEquals(2.5, productoInsumo.getCantidad());
        assertEquals(21L, productoOperacion.getOperacion().getId());
        assertEquals(3, productoOperacion.getCantidad());
        assertEquals(2, productoOperacion.getOrden());
    }
}
