package com.mobilesco.mobilesco_back.modules.imagen.infrastructure.in.api.controllers;

import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.modules.imagen.application.usecases.AccesoArchivosService;

@RestController
@RequestMapping("/uploads/empleados")
public class ArchivoPrivadoController {

    private final AccesoArchivosService accesoArchivosService;

    public ArchivoPrivadoController(AccesoArchivosService accesoArchivosService) {
        this.accesoArchivosService = accesoArchivosService;
    }

    @GetMapping("/{empleadoId}/perfil/{nombreArchivo:.+}")
    public ResponseEntity<org.springframework.core.io.Resource> obtenerFotoEmpleado(
            @PathVariable Long empleadoId,
            @PathVariable String nombreArchivo,
            Authentication authentication
    ) {
        var archivo = accesoArchivosService.cargarFotoEmpleado(
                empleadoId,
                nombreArchivo,
                authentication
        );

        return ResponseEntity.ok()
                .contentType(archivo.mediaType())
                .cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(nombreArchivo).build().toString())
                .body(archivo.recurso());
    }
}
