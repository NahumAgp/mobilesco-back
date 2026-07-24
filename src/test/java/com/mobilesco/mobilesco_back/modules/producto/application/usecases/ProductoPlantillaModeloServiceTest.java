package com.mobilesco.mobilesco_back.modules.producto.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.modelo.domain.models.ModeloModel;
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
    private ProductoPlantillaModeloService service;

    @BeforeEach
    void setUp() {
        ProductoRepository productoRepository = mock(ProductoRepository.class);
        productoInsumoRepository = mock(ProductoInsumoRepository.class);
        productoOperacionRepository = mock(ProductoOperacionRepository.class);
        service = new ProductoPlantillaModeloService(
                productoRepository,
                productoInsumoRepository,
                productoOperacionRepository);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void heredaInsumosYOperacionesComoPendientesDeCantidad() {
        InsumoModel insumo = new InsumoModel();
        insumo.setId(11L);
        OperacionModel operacion = OperacionModel.builder().id(21L).build();

        ModeloModel modelo = new ModeloModel();
        modelo.setId(5L);
        modelo.setInsumos(Set.of(insumo));
        modelo.setOperaciones(List.of(operacion));

        ProductoModel producto = ProductoModel.builder().id(7L).modelo(modelo).build();
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
        assertNull(productoInsumo.getCantidad());
        assertEquals(21L, productoOperacion.getOperacion().getId());
        assertNull(productoOperacion.getCantidad());
        assertEquals(1, productoOperacion.getOrden());
    }
}
