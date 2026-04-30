// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/controller/ImagenController.java
// ============================================
package com.mobilesco.mobilesco_back.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.dto.imagen.ImagenCreateDTO;
import com.mobilesco.mobilesco_back.dto.imagen.ImagenResponseDTO;
import com.mobilesco.mobilesco_back.dto.imagen.ImagenUpdateDTO;
import com.mobilesco.mobilesco_back.services.ImagenService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(ApiPaths.IMAGENES)
public class ImagenController {

    private final ImagenService imagenService;

    public ImagenController(ImagenService imagenService) {
        this.imagenService = imagenService;
    }

    // ========== CREATE ==========
    
    @PostMapping
    public ResponseEntity<ImagenResponseDTO> crear(@Valid @RequestBody ImagenCreateDTO dto) {
        ImagenResponseDTO creado = imagenService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImagenResponseDTO> subirYCrear(
            @RequestParam Long productoId,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(required = false) Boolean esPrincipal,
            @RequestParam(required = false) Integer orden,
            @RequestParam(required = false) String altTexto
    ) {
        ImagenResponseDTO creado = imagenService.crearDesdeArchivo(productoId, archivo, esPrincipal, orden, altTexto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // ========== READ ==========
    
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<ImagenResponseDTO>> obtenerPorProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(imagenService.obtenerPorProducto(productoId));
    }
    
    @GetMapping("/producto/{productoId}/principal")
    public ResponseEntity<ImagenResponseDTO> obtenerPrincipal(@PathVariable Long productoId) {
        ImagenResponseDTO imagen = imagenService.obtenerPrincipalPorProducto(productoId);
        return imagen != null ? ResponseEntity.ok(imagen) : ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ImagenResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(imagenService.obtenerPorId(id));
    }

    // ========== UPDATE ==========
    
    @PutMapping("/{id}")
    public ResponseEntity<ImagenResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ImagenUpdateDTO dto) {
        return ResponseEntity.ok(imagenService.actualizar(id, dto));
    }

    // ========== DELETE ==========
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        imagenService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/producto/{productoId}")
    public ResponseEntity<Void> eliminarTodasPorProducto(@PathVariable Long productoId) {
        imagenService.eliminarTodasPorProducto(productoId);
        return ResponseEntity.noContent().build();
    }
}
