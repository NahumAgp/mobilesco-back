package com.mobilesco.mobilesco_back.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.dto.Familia.FamiliaCreateDTO;
import com.mobilesco.mobilesco_back.dto.Familia.FamiliaResponseDTO;
import com.mobilesco.mobilesco_back.dto.Familia.FamiliaUpdateDTO;
import com.mobilesco.mobilesco_back.services.FamiliaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.FAMILIAS)
@RequiredArgsConstructor
public class FamiliaController {

    private final FamiliaService familiaService;

    @PostMapping
    public FamiliaResponseDTO crear(@Valid @RequestBody FamiliaCreateDTO dto) {
        return familiaService.crear(dto);
    }

    @PutMapping("/{id}")
    public FamiliaResponseDTO actualizar(
            @PathVariable Long id,
            @Valid @RequestBody FamiliaUpdateDTO dto) {

        return familiaService.actualizar(id, dto);
    }

    @GetMapping("/{id}")
    public FamiliaResponseDTO obtener(@PathVariable Long id) {
        return familiaService.obtenerPorId(id);
    }

    @GetMapping
    public List<FamiliaResponseDTO> listar() {
        return familiaService.listar();
    }

    @GetMapping("/activas")
    public List<FamiliaResponseDTO> listarActivas() {
        return familiaService.listarActivas();
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        familiaService.eliminar(id);
    }
}