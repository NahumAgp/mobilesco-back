package com.mobilesco.mobilesco_back.modules.configuracion.infrastructure.in.api.controllers;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.modules.configuracion.domain.models.ConfiguracionSistemaModel;
import com.mobilesco.mobilesco_back.modules.configuracion.infrastructure.in.api.dtos.ConfiguracionSistemaDTO;
import com.mobilesco.mobilesco_back.modules.configuracion.infrastructure.in.api.dtos.IdentidadPublicaDTO;
import com.mobilesco.mobilesco_back.modules.configuracion.infrastructure.out.persistence.repositories.ConfiguracionSistemaRepository;
import com.mobilesco.mobilesco_back.modules.imagen.application.usecases.AlmacenamientoImagenesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.CONFIGURACION_SISTEMA)
@RequiredArgsConstructor
public class ConfiguracionSistemaController {
    private static final long CONFIG_ID = 1L;
    private final ConfiguracionSistemaRepository repository;
    private final AlmacenamientoImagenesService almacenamientoImagenes;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_USERS')")
    public ConfiguracionSistemaDTO obtener() {
        return map(repository.findById(CONFIG_ID).orElseThrow());
    }

    @GetMapping("/publica")
    public IdentidadPublicaDTO obtenerIdentidadPublica() {
        ConfiguracionSistemaModel model = repository.findById(CONFIG_ID).orElseThrow();
        return new IdentidadPublicaDTO(model.getNombreEmpresa(), model.getColorPrimario(), model.getLogoSistemaUrl(), model.getLogoDocumentosUrl(), model.getLogoCatalogoUrl());
    }

    @PutMapping
    @PreAuthorize("hasAuthority('VIEW_USERS')")
    public ConfiguracionSistemaDTO actualizar(@Valid @RequestBody ConfiguracionSistemaDTO dto) {
        ConfiguracionSistemaModel model = repository.findById(CONFIG_ID).orElseThrow();
        model.setNombreEmpresa(dto.getNombreEmpresa().trim());
        model.setRazonSocial(dto.getRazonSocial());
        model.setRfc(dto.getRfc());
        model.setCorreo(dto.getCorreo());
        model.setTelefono(dto.getTelefono());
        model.setDireccion(dto.getDireccion());
        model.setSitioWeb(dto.getSitioWeb());
        model.setMoneda(dto.getMoneda());
        model.setColorPrimario(dto.getColorPrimario().toUpperCase());
        model.setCoberturaAbcA(dto.getCoberturaAbcA());
        model.setCoberturaAbcB(dto.getCoberturaAbcB());
        model.setCoberturaAbcC(dto.getCoberturaAbcC());
        return map(repository.save(model));
    }

    @PostMapping("/logo/{variante}")
    @PreAuthorize("hasAuthority('VIEW_USERS')")
    public ConfiguracionSistemaDTO subirLogo(
            @PathVariable String variante,
            @RequestParam("archivo") MultipartFile archivo) {
        try {
            String url = almacenamientoImagenes.guardarLogoEmpresa(variante, archivo);
            ConfiguracionSistemaModel model = repository.findById(CONFIG_ID).orElseThrow();
            switch (variante) {
                case "sistema" -> model.setLogoSistemaUrl(url);
                case "documentos" -> model.setLogoDocumentosUrl(url);
                case "catalogo" -> model.setLogoCatalogoUrl(url);
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Variante no válida.");
            }
            return map(repository.save(model));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo guardar el logo.", e);
        }
    }

    private ConfiguracionSistemaDTO map(ConfiguracionSistemaModel model) {
        ConfiguracionSistemaDTO dto = new ConfiguracionSistemaDTO();
        dto.setNombreEmpresa(model.getNombreEmpresa());
        dto.setRazonSocial(model.getRazonSocial());
        dto.setRfc(model.getRfc());
        dto.setCorreo(model.getCorreo());
        dto.setTelefono(model.getTelefono());
        dto.setDireccion(model.getDireccion());
        dto.setSitioWeb(model.getSitioWeb());
        dto.setMoneda(model.getMoneda());
        dto.setColorPrimario(model.getColorPrimario());
        dto.setLogoSistemaUrl(model.getLogoSistemaUrl());
        dto.setLogoDocumentosUrl(model.getLogoDocumentosUrl());
        dto.setLogoCatalogoUrl(model.getLogoCatalogoUrl());
        dto.setCoberturaAbcA(model.getCoberturaAbcA());
        dto.setCoberturaAbcB(model.getCoberturaAbcB());
        dto.setCoberturaAbcC(model.getCoberturaAbcC());
        return dto;
    }
}
