package com.mobilesco.mobilesco_back.modules.insumo.domain.enums;

public enum TipoInsumo {
    HERRAJES("Herrajes"),
    PLASTICOS("Plásticos"),
    CARPINTERIA("Carpintería"),
    PINTURA("Pintura"),
    TAPICERIA("Tapicería");
    
    private final String nombre;
    
    TipoInsumo(String nombre) {
        this.nombre = nombre;
    }
    
    public String getNombre() {
        return nombre;
    }
}