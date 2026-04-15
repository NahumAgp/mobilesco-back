package com.mobilesco.mobilesco_back.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.dto.unidadMedida.UnidadMedidaCreateDTO;
import com.mobilesco.mobilesco_back.dto.unidadMedida.UnidadMedidaResponseDTO;
import com.mobilesco.mobilesco_back.dto.unidadMedida.UnidadMedidaUpdateDTO;
import com.mobilesco.mobilesco_back.services.UnidadMedidaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;


@Tag(name = "UnidadMedida", description = "CRUD y gestión de unidades de medida")
@RestController
@RequestMapping(ApiPaths.UNIDADES_MEDIDA)
public class UnidadMedidaController {

    
    @Autowired
    private UnidadMedidaService unidadMedidaService;
    //OBTENER todas las unidades de medida
    //GET /unidadMedida
    @GetMapping
    @Operation(
        summary= "Listar Unidades de Medida",
        description = "Devuelve Unidades de Medida"
    )
        public List<?> listar() {
            return unidadMedidaService.obtenerTodos();
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
        return unidadMedidaService.actualizar(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //Eliminar un regiustro de la bd
    @Operation(summary = "Eliminar Unidad de Medida", description = "Elimina una Unidad de Medida por su ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        boolean ok = unidadMedidaService.eliminar(id);
        return ok ? ResponseEntity.noContent().build() 
                  : ResponseEntity.notFound().build();
        
    }
}