package com.mobilesco.mobilesco_back.modules.imagen.infrastructure.in.api.controllers;

import java.time.Duration;

import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.modules.imagen.application.usecases.AccesoArchivosService;

@RestController
@RequestMapping("/uploads")
public class ArchivoPublicoController {

    private final AccesoArchivosService accesoArchivosService;

    public ArchivoPublicoController(AccesoArchivosService accesoArchivosService) {
        this.accesoArchivosService = accesoArchivosService;
    }

    @GetMapping("/modelos/{modeloId}/{nombreArchivo:.+}")
    public ResponseEntity<Resource> obtenerImagenModelo(
            @PathVariable Long modeloId,
            @PathVariable String nombreArchivo
    ) {
        return responder(accesoArchivosService.cargarImagenModelo(modeloId, nombreArchivo));
    }

    @GetMapping("/productos/catalogo/{productoId}/{nombreArchivo:.+}")
    public ResponseEntity<Resource> obtenerImagenProducto(
            @PathVariable Long productoId,
            @PathVariable String nombreArchivo
    ) {
        return responder(accesoArchivosService.cargarImagenProducto(productoId, nombreArchivo));
    }

    private ResponseEntity<Resource> responder(AccesoArchivosService.ArchivoDescargable archivo) {
        return ResponseEntity.ok()
                .contentType(archivo.mediaType())
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .header("X-Content-Type-Options", "nosniff")
                .body(archivo.recurso());
    }
}
