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

import com.mobilesco.mobilesco_back.dto.LineaProducto.LineaProductoCreateDTO;
import com.mobilesco.mobilesco_back.dto.LineaProducto.LineaProductoResponseDTO;
import com.mobilesco.mobilesco_back.dto.LineaProducto.LineaProductoUpdateDTO;
import com.mobilesco.mobilesco_back.services.LineaProductoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("lineas-Producto") // /api/v1/lineas
@RequiredArgsConstructor
public class LineaProductoController {

    private final LineaProductoService lineaProductoService;

    @PostMapping
    public LineaProductoResponseDTO crear(@Valid @RequestBody LineaProductoCreateDTO dto) {
        return lineaProductoService.crear(dto);
    }

    @PutMapping("/{id}")
    public LineaProductoResponseDTO actualizar(
            @PathVariable Long id,
            @Valid @RequestBody LineaProductoUpdateDTO dto) {
        return lineaProductoService.actualizar(id, dto);
    }

    @GetMapping("/{id}")
    public LineaProductoResponseDTO obtener(@PathVariable Long id) {
        return lineaProductoService.obtenerPorId(id);
    }

    @GetMapping
    public List<LineaProductoResponseDTO> listar() {
        return lineaProductoService.listar();
    }

    @GetMapping("/activos")
    public List<LineaProductoResponseDTO> listarActivos() {
        return lineaProductoService.listarActivos();
    }

    @GetMapping("/buscar")
    public List<LineaProductoResponseDTO> buscar(@RequestParam String nombre) {
        return lineaProductoService.buscar(nombre);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        lineaProductoService.eliminar(id);
    }
}