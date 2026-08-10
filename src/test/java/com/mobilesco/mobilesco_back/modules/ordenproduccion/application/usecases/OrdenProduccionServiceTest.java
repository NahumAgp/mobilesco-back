package com.mobilesco.mobilesco_back.modules.ordenproduccion.application.usecases;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mobilesco.mobilesco_back.modules.centrotrabajo.domain.models.CentroTrabajoModel;
import com.mobilesco.mobilesco_back.modules.cotizacion.domain.models.*;
import com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.out.persistence.repositories.CotizacionRepository;
import com.mobilesco.mobilesco_back.modules.insumo.application.usecases.StockMinimoNotificacionService;
import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.out.persistence.repositories.InsumoRepository;
import com.mobilesco.mobilesco_back.modules.kardex.application.usecases.KardexService;
import com.mobilesco.mobilesco_back.modules.operacion.domain.models.OperacionModel;
import com.mobilesco.mobilesco_back.modules.ordenproduccion.domain.models.*;
import com.mobilesco.mobilesco_back.modules.ordenproduccion.infrastructure.in.api.dtos.OrdenProduccionAccionesDTO.Conversion;
import com.mobilesco.mobilesco_back.modules.ordenproduccion.infrastructure.out.persistence.repositories.*;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.*;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.*;
import com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.out.persistence.repositories.*;
import com.mobilesco.mobilesco_back.modules.cliente.infrastructure.out.persistence.repositories.ClienteRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.modules.unidadmedida.domain.models.UnidadMedidaModel;

@ExtendWith(MockitoExtension.class)
class OrdenProduccionServiceTest {
    @Mock OrdenProduccionRepository ordenRepository; @Mock OrdenProduccionOperacionRepository ordenOperacionRepository;
    @Mock OrdenProduccionAvanceRepository avanceRepository; @Mock ProductoRepository productoRepository;
    @Mock ProductoInsumoRepository productoInsumoRepository; @Mock ProductoOperacionRepository productoOperacionRepository;
    @Mock ClienteRepository clienteRepository; @Mock CotizacionRepository cotizacionRepository; @Mock InsumoRepository insumoRepository;
    @Mock SalidaInsumoRepository salidaRepository; @Mock DetalleSalidaInsumoRepository detalleSalidaRepository;
    @Mock KardexService kardexService; @Mock StockMinimoNotificacionService stockMinimoNotificacionService;
    @InjectMocks OrdenProduccionService service;

    @BeforeEach void savesReturnEntity(){ lenient().when(ordenRepository.save(any())).thenAnswer(i->i.getArgument(0)); }

    @Test void rechazaCotizacionQueNoEstaAceptada(){
        var cot=CotizacionModel.builder().id(7L).estado(EstadoCotizacion.ENVIADA).build();
        when(cotizacionRepository.findById(7L)).thenReturn(Optional.of(cot));
        assertThatThrownBy(()->service.convertirCotizacion(7L,new Conversion(),"user@test.mx"))
            .isInstanceOf(ValidationException.class).hasMessageContaining("aceptada");
        verify(ordenRepository,never()).save(any());
    }

    @Test void evitaConvertirDosVecesLaMismaCotizacion(){
        var cot=CotizacionModel.builder().id(7L).estado(EstadoCotizacion.ACEPTADA).build();
        when(cotizacionRepository.findById(7L)).thenReturn(Optional.of(cot)); when(ordenRepository.existsByCotizacionId(7L)).thenReturn(true);
        assertThatThrownBy(()->service.convertirCotizacion(7L,new Conversion(),"user@test.mx"))
            .isInstanceOf(ValidationException.class).hasMessageContaining("ya tiene");
    }

    @Test void liberarCongelaYConsolidaBomConDesperdicio(){
        var unidad=new UnidadMedidaModel(); unidad.setSimbolo("m");
        var insumo=InsumoModel.builder().id(20L).codigo("TUBO").nombre("Tubo").unidadMedida(unidad).stockActual(10d).stockApartado(0d).build();
        var producto=ProductoModel.builder().id(10L).sku("P-01").nombre("Producto").activo(true).build();
        var centro=CentroTrabajoModel.builder().id(30L).nombre("Corte").build();
        var operacion=OperacionModel.builder().id(40L).codigo("COR").nombre("Cortar").tiempoOperacion(5d).centroTrabajo(centro).build();
        var bom=ProductoInsumoModel.builder().producto(producto).insumo(insumo).cantidad(10d).desperdicioPorcentaje(10d).build();
        var ruta=ProductoOperacionModel.builder().producto(producto).operacion(operacion).cantidad(2).orden(1).activo(true).build();
        var orden=OrdenProduccionModel.builder().id(1L).folio("OP-2026-00001").origen(OrigenOrdenProduccion.MANUAL)
            .estado(EstadoOrdenProduccion.BORRADOR).creadoPor("u").actualizadoPor("u").build();
        orden.getDetalles().add(OrdenProduccionDetalleModel.builder().id(2L).orden(orden).producto(producto).skuSnapshot("P-01")
            .nombreSnapshot("Producto").cantidadPlaneada(new BigDecimal("2.000")).cantidadTerminada(BigDecimal.ZERO).build());
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(productoInsumoRepository.findByProductoId(10L)).thenReturn(List.of(bom));
        when(productoOperacionRepository.findByProductoIdOrderByOrdenAsc(10L)).thenReturn(List.of(ruta));
        when(insumoRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(insumo));

        var response=service.liberar(1L,"supervisor@test.mx");

        assertThat(response.getEstado()).isEqualTo("LIBERADA");
        assertThat(response.getInsumos()).singleElement().satisfies(i->assertThat(i.getRequerido()).isEqualByComparingTo("22.0000"));
        assertThat(response.getInsumos()).singleElement().satisfies(i->{
            assertThat(i.getApartado()).isEqualByComparingTo("10.0000");
            assertThat(i.getPorApartar()).isEqualByComparingTo("12.0000");
            assertThat(i.isFaltante()).isTrue();
        });
        assertThat(insumo.getStockActual()).isEqualTo(10d);
        assertThat(insumo.getStockApartado()).isEqualTo(10d);
        assertThat(response.getOperaciones()).singleElement().satisfies(o->{assertThat(o.getRepeticionesPlaneadas()).isEqualTo(4);assertThat(o.getTiempoPlaneado()).isEqualByComparingTo("20.000");});
    }
}
