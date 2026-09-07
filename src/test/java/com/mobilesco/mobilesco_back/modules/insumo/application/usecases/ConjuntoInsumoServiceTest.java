package com.mobilesco.mobilesco_back.modules.insumo.application.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import com.mobilesco.mobilesco_back.modules.insumo.domain.models.*;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.in.api.dtos.ConjuntoInsumoDTO;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.out.persistence.repositories.InsumoRepository;
import com.mobilesco.mobilesco_back.modules.unidadmedida.domain.models.UnidadMedidaModel;
import com.mobilesco.mobilesco_back.modules.unidadmedida.infrastructure.out.persistence.repositories.UnidadMedidaRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;

class ConjuntoInsumoServiceTest {
    private final InsumoRepository insumos = mock(InsumoRepository.class);
    private final UnidadMedidaRepository unidades = mock(UnidadMedidaRepository.class);
    private final ConjuntoInsumoService servicio = new ConjuntoInsumoService(insumos, unidades);

    private void preparar() {
        var unidad = new UnidadMedidaModel(); unidad.setId(1L); unidad.setEstado(true); unidad.setSimbolo("pz");
        when(unidades.findById(1L)).thenReturn(Optional.of(unidad));
        when(insumos.findById(2L)).thenReturn(Optional.of(InsumoModel.builder().id(2L).nombre("Tubo").unidadMedida(unidad).costoCotizacion(5.0).build()));
        when(insumos.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
    }
    private ConjuntoInsumoDTO dto(Long version, double cantidad) {
        return new ConjuntoInsumoDTO(null, "Travesanos", "", 1L, null, null, version,
                List.of(new ConjuntoInsumoDTO.Componente(2L, null, null, cantidad, null)));
    }
    @Test void creaDespieceReferenciadoYCalculaCosto() {
        preparar();
        var creado = servicio.guardar(null, dto(null, 2));
        assertEquals(10.0, creado.costoCotizacion());
        assertEquals(2L, creado.componentes().getFirst().insumoId());
    }
    @Test void rechazaCantidadesNoFinitas() {
        preparar();
        assertThrows(ValidationException.class, () -> servicio.guardar(null, dto(null, Double.POSITIVE_INFINITY)));
        verify(insumos, never()).saveAndFlush(any());
    }
    @Test void rechazaEdicionObsoleta() {
        when(insumos.findByIdForUpdate(3L)).thenReturn(Optional.of(InsumoModel.builder().conjunto(true).version(2L).build()));
        assertThrows(ValidationException.class, () -> servicio.guardar(3L, dto(1L, 2)));
        verify(insumos, never()).saveAndFlush(any());
    }
    @Test void noPermiteConjuntosAnidados() {
        preparar();
        when(insumos.findById(2L)).thenReturn(Optional.of(InsumoModel.builder().id(2L).conjunto(true).build()));
        assertThrows(ValidationException.class, () -> servicio.guardar(null, dto(null, 2)));
    }
}
