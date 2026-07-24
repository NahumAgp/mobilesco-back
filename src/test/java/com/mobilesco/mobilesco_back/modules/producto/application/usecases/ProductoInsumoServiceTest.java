package com.mobilesco.mobilesco_back.modules.producto.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.out.persistence.repositories.InsumoRepository;
import com.mobilesco.mobilesco_back.modules.modelo.domain.models.ModeloModel;
import com.mobilesco.mobilesco_back.modules.nivel.domain.models.NivelModel;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoInsumoModel;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoModel;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoInsumoCreateDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoInsumoRepository;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class ProductoInsumoServiceTest {
    @Mock ProductoInsumoRepository productoInsumoRepository;
    @Mock ProductoRepository productoRepository;
    @Mock InsumoRepository insumoRepository;
    ProductoInsumoService service;

    @BeforeEach
    void setUp() {
        service = new ProductoInsumoService(productoInsumoRepository, productoRepository, insumoRepository);
    }

    @Test
    void aplicaCantidadesAMismoModeloYNivelSinImportarColor() {
        ModeloModel modelo = new ModeloModel();
        modelo.setId(10L);
        NivelModel nivel = new NivelModel();
        nivel.setId(20L);
        nivel.setNombre("Primaria");
        ProductoModel origen = ProductoModel.builder()
                .id(1L).sku("SKU-AM").modelo(modelo).nivel(nivel).build();
        ProductoModel variante = ProductoModel.builder()
                .id(2L).sku("SKU-AZ").modelo(modelo).nivel(nivel).build();
        InsumoModel insumo = InsumoModel.builder().id(30L).nombre("Tubo").build();
        ProductoInsumoModel insumoOrigen = ProductoInsumoModel.builder()
                .id(40L).producto(origen).insumo(insumo).cantidad(null).desperdicioPorcentaje(0.0).build();
        ProductoInsumoModel insumoVariante = ProductoInsumoModel.builder()
                .id(41L).producto(variante).insumo(insumo).cantidad(null).desperdicioPorcentaje(0.0).build();
        ProductoInsumoCreateDTO cantidad = new ProductoInsumoCreateDTO();
        cantidad.setInsumoId(30L);
        cantidad.setCantidad(2.75);
        cantidad.setDesperdicioPorcentaje(5.0);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(origen));
        when(productoRepository.findByModeloIdAndNivelId(10L, 20L)).thenReturn(List.of(origen, variante));
        when(productoInsumoRepository.findByProductoId(1L)).thenReturn(List.of(insumoOrigen));
        when(productoInsumoRepository.findByProductoId(2L)).thenReturn(List.of(insumoVariante));

        var respuesta = service.aplicarCantidadesMismoNivel(1L, List.of(cantidad));

        assertEquals(2, respuesta.getProductosActualizados());
        assertEquals(2.75, insumoOrigen.getCantidad());
        assertEquals(2.75, insumoVariante.getCantidad());
        assertEquals(5.0, insumoVariante.getDesperdicioPorcentaje());
        verify(productoInsumoRepository, atLeastOnce()).saveAll(anyList());
    }
}
