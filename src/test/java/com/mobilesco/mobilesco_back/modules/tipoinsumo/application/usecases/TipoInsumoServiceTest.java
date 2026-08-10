package com.mobilesco.mobilesco_back.modules.tipoinsumo.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mobilesco.mobilesco_back.modules.proveedor.infrastructure.out.persistence.repositories.ProveedorRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.modules.tipoinsumo.domain.models.TipoInsumoModel;
import com.mobilesco.mobilesco_back.modules.tipoinsumo.infrastructure.in.api.dtos.TipoInsumoResponseDTO;
import com.mobilesco.mobilesco_back.modules.tipoinsumo.infrastructure.in.api.dtos.TipoInsumoUpdateDTO;
import com.mobilesco.mobilesco_back.modules.tipoinsumo.infrastructure.out.persistence.repositories.TipoInsumoRepository;

@ExtendWith(MockitoExtension.class)
class TipoInsumoServiceTest {

    @Mock TipoInsumoRepository tipoInsumoRepository;
    @Mock ProveedorRepository proveedorRepository;

    private TipoInsumoService service;
    private TipoInsumoModel herrajes;

    @BeforeEach
    void setUp() {
        service = new TipoInsumoService(tipoInsumoRepository, proveedorRepository);
        herrajes = tipo(1L, "HERRAJES", "Herrajes");
    }

    @Test
    void actualizaCodigoManualYConservaReferenciasDeProveedores() {
        TipoInsumoUpdateDTO dto = actualizacion("h", "Herrajes");
        when(tipoInsumoRepository.findById(1L)).thenReturn(Optional.of(herrajes));
        when(tipoInsumoRepository.findByNombreNormalizado("herrajes")).thenReturn(Optional.of(herrajes));
        when(tipoInsumoRepository.findByCodigoIgnoreCase("H")).thenReturn(Optional.empty());
        when(tipoInsumoRepository.save(herrajes)).thenReturn(herrajes);

        TipoInsumoResponseDTO resultado = service.actualizar(1L, dto);

        assertEquals("H", resultado.getCodigo());
        verify(proveedorRepository).actualizarCodigoTipoInsumo("HERRAJES", "H");
        verify(tipoInsumoRepository).save(herrajes);
    }

    @Test
    void rechazaCodigoQueYaPerteneceAOtroTipo() {
        TipoInsumoModel pintura = tipo(2L, "P", "Pintura");
        TipoInsumoUpdateDTO dto = actualizacion("P", "Herrajes");
        when(tipoInsumoRepository.findById(1L)).thenReturn(Optional.of(herrajes));
        when(tipoInsumoRepository.findByNombreNormalizado("herrajes")).thenReturn(Optional.of(herrajes));
        when(tipoInsumoRepository.findByCodigoIgnoreCase("P")).thenReturn(Optional.of(pintura));

        assertThrows(BadRequestException.class, () -> service.actualizar(1L, dto));

        verify(proveedorRepository, never()).actualizarCodigoTipoInsumo("HERRAJES", "P");
        verify(tipoInsumoRepository, never()).save(herrajes);
    }

    @Test
    void rechazaCodigoManualMayorATresCaracteres() {
        TipoInsumoUpdateDTO dto = actualizacion("HERR", "Herrajes");
        when(tipoInsumoRepository.findById(1L)).thenReturn(Optional.of(herrajes));

        assertThrows(BadRequestException.class, () -> service.actualizar(1L, dto));

        verify(tipoInsumoRepository, never()).save(herrajes);
    }

    private TipoInsumoUpdateDTO actualizacion(String codigo, String nombre) {
        TipoInsumoUpdateDTO dto = new TipoInsumoUpdateDTO();
        dto.setCodigo(codigo);
        dto.setNombre(nombre);
        return dto;
    }

    private TipoInsumoModel tipo(Long id, String codigo, String nombre) {
        TipoInsumoModel tipo = new TipoInsumoModel();
        tipo.setId(id);
        tipo.setCodigo(codigo);
        tipo.setNombre(nombre);
        tipo.setActivo(true);
        return tipo;
    }
}
