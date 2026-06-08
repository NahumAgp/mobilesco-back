package com.mobilesco.mobilesco_back.modules.shared.application.codes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class CatalogCodeGeneratorTest {

    @Test
    void priorizaUnoDosYTresCaracteres() {
        assertEquals("E", CatalogCodeGenerator.generate("Escolar", List.of()));
        assertEquals("ES", CatalogCodeGenerator.generate("Escolar", List.of("E")));
        assertEquals("ESC", CatalogCodeGenerator.generate("Escolar", List.of("E", "ES")));
    }

    @Test
    void cambiaElTercerCaracterCuandoLosNaturalesEstanOcupados() {
        assertEquals("ESA", CatalogCodeGenerator.generate("Escolar", List.of("E", "ES", "ESC")));
    }

    @Test
    void normalizaAcentosEspaciosYMinusculas() {
        assertEquals("A", CatalogCodeGenerator.generate("Áreas especiales", List.of()));
    }
}
