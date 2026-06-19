package com.mobilesco.mobilesco_back.modules.color.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.BadRequestException;

class ColorCodeGeneratorTest {

    @Test
    void generaCodigoDeDosLetrasParaColorSimple() {
        assertEquals("AZ", ColorCodeGenerator.generate("Azul", List.of()));
        assertEquals("VE", ColorCodeGenerator.generate("Verde", List.of()));
        assertEquals("NE", ColorCodeGenerator.generate("Negro", List.of()));
    }

    @Test
    void generaCodigoBaseMasInicialDeLaVariante() {
        assertEquals("AZM", ColorCodeGenerator.generate("Azul marino", List.of()));
        assertEquals("AZC", ColorCodeGenerator.generate("Azul cielo", List.of()));
        assertEquals("VEO", ColorCodeGenerator.generate("Verde olivo", List.of()));
    }

    @Test
    void agregaLetrasDeLaVarianteCuandoExisteColision() {
        assertEquals("AZCL", ColorCodeGenerator.generate("Azul claro", List.of("AZC")));
        assertEquals("AZCE", ColorCodeGenerator.generate("Azul celeste", List.of("AZC", "AZCL")));
    }

    @Test
    void normalizaAcentosEspaciosYCaracteresEspeciales() {
        assertEquals("AZE", ColorCodeGenerator.generate(" \u00C1zul-el\u00E9ctrico!! ", List.of()));
    }

    @Test
    void rechazaNombresSinLetras() {
        assertThrows(BadRequestException.class, () -> ColorCodeGenerator.generate("#123456", List.of()));
    }
}
