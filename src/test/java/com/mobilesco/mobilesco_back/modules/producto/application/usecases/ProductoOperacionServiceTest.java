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

import com.mobilesco.mobilesco_back.modules.modelo.domain.models.ModeloModel;
import com.mobilesco.mobilesco_back.modules.nivel.domain.models.NivelModel;
import com.mobilesco.mobilesco_back.modules.operacion.domain.models.OperacionModel;
import com.mobilesco.mobilesco_back.modules.operacion.infrastructure.out.persistence.repositories.OperacionRepository;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoModel;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoOperacionModel;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoOperacionCreateDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoOperacionRepository;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class ProductoOperacionServiceTest {
    @Mock ProductoOperacionRepository productoOperacionRepository;
    @Mock ProductoRepository productoRepository;
    @Mock OperacionRepository operacionRepository;
    ProductoOperacionService service;

    @BeforeEach
    void setUp() {
        service = new ProductoOperacionService(productoOperacionRepository, productoRepository, operacionRepository);
    }

    @Test
    void aplicaCantidadesDeOperacionAlMismoModeloYNivel() {
        ModeloModel modelo = new ModeloModel();
        modelo.setId(10L);
        NivelModel nivel = new NivelModel();
        nivel.setId(20L);
        nivel.setNombre("Primaria");
        ProductoModel origen = ProductoModel.builder()
                .id(1L).sku("SKU-AM").modelo(modelo).nivel(nivel).build();
        ProductoModel variante = ProductoModel.builder()
                .id(2L).sku("SKU-AZ").modelo(modelo).nivel(nivel).build();
        OperacionModel operacion = OperacionModel.builder()
                .id(30L).codigo("CORTE").nombre("Corte").tiempoOperacion(4.0).costoMinuto(2.5).build();
        ProductoOperacionModel operacionOrigen = ProductoOperacionModel.builder()
                .id(40L).producto(origen).operacion(operacion).cantidad(null).orden(1).activo(true).build();
        ProductoOperacionModel operacionVariante = ProductoOperacionModel.builder()
                .id(41L).producto(variante).operacion(operacion).cantidad(null).orden(1).activo(true).build();
        ProductoOperacionCreateDTO cantidad = ProductoOperacionCreateDTO.builder()
                .operacionId(30L).cantidad(3).orden(1).build();

        when(productoRepository.findById(1L)).thenReturn(Optional.of(origen));
        when(productoRepository.findByModeloIdAndNivelId(10L, 20L)).thenReturn(List.of(origen, variante));
        when(productoOperacionRepository.findByProductoIdOrderByOrdenAsc(1L))
                .thenReturn(List.of(operacionOrigen));
        when(productoOperacionRepository.findByProductoIdOrderByOrdenAsc(2L))
                .thenReturn(List.of(operacionVariante));

        var respuesta = service.aplicarCantidadesMismoNivel(1L, List.of(cantidad));

        assertEquals(2, respuesta.getProductosActualizados());
        assertEquals(3, operacionOrigen.getCantidad());
        assertEquals(3, operacionVariante.getCantidad());
        assertEquals(12.0, operacionVariante.getTiempoTotal());
        assertEquals(30.0, operacionVariante.getImporteActividad());
        verify(productoOperacionRepository, atLeastOnce()).saveAll(anyList());
    }
}
