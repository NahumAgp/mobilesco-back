package com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class ModeloInsumoDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void aceptaConjuntoNullComoFalse() throws Exception {
        ModeloInsumoDTO dto = objectMapper.readValue("""
                {
                  "id": 11,
                  "cantidad": 2,
                  "conjunto": null
                }
                """, ModeloInsumoDTO.class);

        assertFalse(dto.getConjunto());
    }
}
