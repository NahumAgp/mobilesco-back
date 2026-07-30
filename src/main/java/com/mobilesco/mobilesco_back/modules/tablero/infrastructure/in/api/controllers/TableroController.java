package com.mobilesco.mobilesco_back.modules.tablero.infrastructure.in.api.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.modules.tablero.application.usecases.TableroService;
import com.mobilesco.mobilesco_back.modules.tablero.domain.PeriodoTablero;
import com.mobilesco.mobilesco_back.modules.tablero.infrastructure.in.api.dtos.TableroResumenDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.TABLERO)
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('VIEW_DASHBOARD')")
public class TableroController {
    private final TableroService tableroService;

    @GetMapping("/resumen")
    public TableroResumenDTO obtenerResumen(
            @RequestParam(defaultValue = "MES") PeriodoTablero periodo) {
        return tableroService.obtenerResumen(periodo);
    }
}
