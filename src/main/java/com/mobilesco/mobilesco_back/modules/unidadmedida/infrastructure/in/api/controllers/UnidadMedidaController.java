package com.mobilesco.mobilesco_back.modules.unidadmedida.infrastructure.in.api.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import com.mobilesco.mobilesco_back.modules.unidadmedida.infrastructure.in.api.dtos.UnidadMedidaCreateDTO;
import com.mobilesco.mobilesco_back.modules.unidadmedida.infrastructure.in.api.dtos.UnidadMedidaResponseDTO;
import com.mobilesco.mobilesco_back.modules.unidadmedida.infrastructure.in.api.dtos.UnidadMedidaUpdateDTO;
import com.mobilesco.mobilesco_back.modules.unidadmedida.application.usecases.UnidadMedidaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;


@Tag(name = "UnidadMedida", description = "CRUD y gestión de unidades de medida")
@RestController
@RequestMapping(ApiPaths.UNIDADES_MEDIDA)
public class UnidadMedidaController {

    private final UnidadMedidaService unidadMedidaService;

    public UnidadMedidaController(UnidadMedidaService unidadMedidaService) {
        this.unidadMedidaService = unidadMedidaService;
    }

    //OBTENER todas las unidades de medida
    //GET /unidadMedida
    @GetMapping
    @Operation(
        summary= "Listar Unidades de Medida",
        description = "Devuelve Unidades de Medida"
    )
        public ResponseEntity<?> listar(
                @RequestParam(required = false) Integer page,
                @RequestParam(required = false) Integer size,
                @RequestParam(required = false) String sortBy,
                @RequestParam(required = false, defaultValue = "asc") String direction,
                @RequestParam(required = false) Boolean estado,
                @RequestParam(required = false) String busqueda) {
            if (page != null) {
                return ResponseEntity.ok(unidadMedidaService.obtenerPaginado(page, size, sortBy, direction, estado, busqueda));
            }

            return ResponseEntity.ok(unidadMedidaService.obtenerTodos());
    }

    @GetMapping("/reporte/excel")
    @Operation(summary = "Exportar unidades de medida a Excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) Boolean estado,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String direction) {
        byte[] excel = unidadMedidaService.generarReporteExcel(estado, busqueda, sortBy, direction);
        String filename = "unidades-medida.xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    //CREAR una nueva unidad de medida
    //Post /unidadMedida
    @Operation(summary = "Crear Unidad de Medida", description = "Crea una nueva Unidad de Medida")
    @PostMapping
    public UnidadMedidaResponseDTO crear(@Valid @RequestBody UnidadMedidaCreateDTO dto) {
        return unidadMedidaService.crear(dto);
    }

    //Obtener Unidad de medidad por id
    @GetMapping("/{id}")
    @Operation (summary = "Obtener Unidad Medida por ID")
    public ResponseEntity<UnidadMedidaResponseDTO> obtenerPorId(@PathVariable Long id) {

        UnidadMedidaResponseDTO unidadaMedida = unidadMedidaService.obtenerPorId(id);

        return ResponseEntity.ok(unidadaMedida);
    }
    
    

    //ACTUALIZAR una unidad de medida existente
    //PUT /unidadMedida/{id}
    @Operation(summary = "Actualizar Unidad de Medida", description = "Actualiza una Unidad de Medida existente")
    @PutMapping("/{id}")
    public ResponseEntity<UnidadMedidaResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody UnidadMedidaUpdateDTO dto) {
        return ResponseEntity.ok(unidadMedidaService.actualizar(id, dto));
    }

    //Eliminar un regiustro de la bd
    @Operation(summary = "Eliminar Unidad de Medida", description = "Elimina una Unidad de Medida por su ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        unidadMedidaService.eliminar(id);
        return ResponseEntity.noContent().build();
        
    }
}
