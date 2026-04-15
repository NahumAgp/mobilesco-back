package com.mobilesco.mobilesco_back.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.dto.TipoProducto.TipoProductoCreateDTO;
import com.mobilesco.mobilesco_back.dto.TipoProducto.TipoProductoResponseDTO;
import com.mobilesco.mobilesco_back.dto.TipoProducto.TipoProductoUpdateDTO;
import com.mobilesco.mobilesco_back.services.TipoProductoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.TIPO_PRODUCTO)
@RequiredArgsConstructor
public class TipoProductoController {

    private final TipoProductoService tipoProductoService;

    @PostMapping
    public TipoProductoResponseDTO crear(@Valid @RequestBody TipoProductoCreateDTO dto) {
        return tipoProductoService.crear(dto);
    }

    @PutMapping("/{id}")
    public TipoProductoResponseDTO actualizar(
            @PathVariable Long id,
            @Valid @RequestBody TipoProductoUpdateDTO dto) {
        return tipoProductoService.actualizar(id, dto);
    }

    @GetMapping("/{id}")
    public TipoProductoResponseDTO obtener(@PathVariable Long id) {
        return tipoProductoService.obtenerPorId(id);
    }

    @GetMapping
    public List<TipoProductoResponseDTO> listar() {
        return tipoProductoService.listar();
    }

    @GetMapping("/activos")
    public List<TipoProductoResponseDTO> listarActivos() {
        return tipoProductoService.listarActivos();
    }

    @GetMapping("/familia/{familiaId}")
    public List<TipoProductoResponseDTO> listarPorFamilia(@PathVariable Long familiaId) {
        return tipoProductoService.listarPorFamilia(familiaId);
    }

    @GetMapping("/buscar")
    public List<TipoProductoResponseDTO> buscar(@RequestParam String nombre) {
        return tipoProductoService.buscar(nombre);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        tipoProductoService.eliminar(id);
    }
}