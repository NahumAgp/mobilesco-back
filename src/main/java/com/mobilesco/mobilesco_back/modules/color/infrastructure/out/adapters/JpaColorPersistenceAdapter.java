/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/color/infrastructure/out/adapters/JpaColorPersistenceAdapter.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: JpaColorPersistenceAdapter
 * CONTEXTO: Adaptador de infraestructura que implementa puertos y delega en repositorios JPA.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.color.infrastructure.out.adapters;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.mobilesco.mobilesco_back.modules.color.domain.models.ColorModel;
import com.mobilesco.mobilesco_back.modules.color.infrastructure.out.persistence.repositories.ColorRepository;
import com.mobilesco.mobilesco_back.modules.color.application.ports.ColorPersistencePort;

@Component
public class JpaColorPersistenceAdapter implements ColorPersistencePort {

    private final ColorRepository colorRepository;

    public JpaColorPersistenceAdapter(ColorRepository colorRepository) {
        this.colorRepository = colorRepository;
    }

    @Override
    public ColorModel save(ColorModel color) {
        return colorRepository.save(color);
    }

    @Override
    public List<ColorModel> findAll() {
        return colorRepository.findAll();
    }

    @Override
    public List<ColorModel> findByActivo(Boolean activo) {
        return colorRepository.findByActivo(activo);
    }

    @Override
    public Optional<ColorModel> findById(Long id) {
        return colorRepository.findById(id);
    }

    @Override
    public Optional<ColorModel> findByNombre(String nombre) {
        return colorRepository.findByNombre(nombre);
    }

    @Override
    public boolean existsByCodigo(String codigo) {
        return colorRepository.existsByCodigo(codigo);
    }

    @Override
    public boolean existsByNombre(String nombre) {
        return colorRepository.existsByNombre(nombre);
    }

    @Override
    public boolean existsById(Long id) {
        return colorRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        colorRepository.deleteById(id);
    }
}





