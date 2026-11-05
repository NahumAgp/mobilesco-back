package com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.modules.modelo.application.usecases.CostoInicialModeloService;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.CostoInicialModeloDTO;

@RestController
@RequestMapping(ApiPaths.MODELOS)
public class CostoInicialModeloController {

    private final CostoInicialModeloService costos;

    public CostoInicialModeloController(CostoInicialModeloService costos) {
        this.costos = costos;
    }

    @GetMapping("/{modeloId}/costos-2026")
    public List<CostoInicialModeloDTO> obtenerPorModelo(@PathVariable Long modeloId) {
        return costos.obtenerPorModelo(modeloId);
    }
}
