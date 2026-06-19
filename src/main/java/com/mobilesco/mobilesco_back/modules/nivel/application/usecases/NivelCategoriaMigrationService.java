package com.mobilesco.mobilesco_back.modules.nivel.application.usecases;

import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.modules.categoria.domain.models.CategoriaModel;
import com.mobilesco.mobilesco_back.modules.categoria.infrastructure.out.persistence.repositories.CategoriaRepository;
import com.mobilesco.mobilesco_back.modules.nivel.domain.models.NivelModel;
import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.out.persistence.repositories.NivelRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NivelCategoriaMigrationService {

    private final NivelRepository nivelRepository;
    private final CategoriaRepository categoriaRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void sincronizarCategoriasGlobales() {
        List<NivelModel> niveles = nivelRepository.findAll();
        if (niveles.isEmpty()) {
            return;
        }

        int actualizados = 0;
        for (NivelModel nivel : niveles) {
            String nombre = nivel.getNombre() == null ? "" : nivel.getNombre().trim();
            if (nombre.isBlank()) {
                continue;
            }

            CategoriaModel categoria = nivel.getCategoria();
            if (categoria == null) {
                categoria = categoriaRepository.findByNombreIgnoreCase(nombre)
                        .orElseGet(() -> {
                            CategoriaModel nueva = new CategoriaModel();
                            nueva.setNombre(nombre);
                            nueva.setDescripcion(nivel.getDescripcion());
                            nueva.setActivo(nivel.getActivo() == null ? true : nivel.getActivo());
                            return categoriaRepository.save(nueva);
                        });
                nivel.setCategoria(categoria);
            }

            nivel.setNombre(categoria.getNombre());
            nivel.setDescripcion(categoria.getDescripcion());
            nivel.setActivo(categoria.getActivo());
            nivelRepository.save(nivel);
            actualizados++;
        }

        if (actualizados > 0) {
            log.info("Migracion de categorias globales completada: {} niveles sincronizados", actualizados);
        }
    }
}
