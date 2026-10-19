package com.mobilesco.mobilesco_back.modules.costoindirecto.application.usecases;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import com.mobilesco.mobilesco_back.modules.costoindirecto.domain.enums.BaseDistribucion;
import com.mobilesco.mobilesco_back.modules.costoindirecto.infrastructure.in.api.dtos.CostoIndirectoCreateDTO;
import com.mobilesco.mobilesco_back.modules.costoindirecto.infrastructure.out.persistence.repositories.CifConfiguracionRepository;
import com.mobilesco.mobilesco_back.modules.costoindirecto.infrastructure.out.persistence.repositories.CostoIndirectoRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;

class CostoIndirectoServiceTest {
    @Test
    void rechazaBaseQueNoParticipaEnElProrrateo() {
        CostoIndirectoService service = new CostoIndirectoService(
                mock(CostoIndirectoRepository.class), mock(CifConfiguracionRepository.class));
        CostoIndirectoCreateDTO dto = new CostoIndirectoCreateDTO();
        dto.setBaseDistribucion(BaseDistribucion.PESO);

        ValidationException error = assertThrows(ValidationException.class, () -> service.crear(dto));
        assertTrue(error.getMessage().contains("minutos"));
    }
}
