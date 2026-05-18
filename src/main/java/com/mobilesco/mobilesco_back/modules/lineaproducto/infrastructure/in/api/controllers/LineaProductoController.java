/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/lineaproducto/infrastructure/in/api/controllers/LineaProductoController.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: LineaProductoController
 * CONTEXTO: Adaptador de entrada HTTP: expone endpoints REST del modulo y delega en contratos de aplicacion.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.lineaproducto.infrastructure.in.api.controllers;

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

import com.mobilesco.mobilesco_back.modules.lineaproducto.infrastructure.in.api.dtos.LineaProductoCreateDTO;
import com.mobilesco.mobilesco_back.modules.lineaproducto.infrastructure.in.api.dtos.LineaProductoResponseDTO;
import com.mobilesco.mobilesco_back.modules.lineaproducto.infrastructure.in.api.dtos.LineaProductoUpdateDTO;
import com.mobilesco.mobilesco_back.modules.lineaproducto.application.usecases.LineaProductoUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("lineas-Producto") // /api/v1/lineas
@RequiredArgsConstructor
public class LineaProductoController {

    private final LineaProductoUseCase lineaProductoService;

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





