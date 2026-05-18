/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/material/infrastructure/out/adapters/JpaMaterialPersistenceAdapter.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: JpaMaterialPersistenceAdapter
 * CONTEXTO: Adaptador de infraestructura que implementa puertos y delega en repositorios JPA.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.material.infrastructure.out.adapters;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.mobilesco.mobilesco_back.modules.material.domain.models.MaterialModel;
import com.mobilesco.mobilesco_back.modules.material.infrastructure.out.persistence.repositories.MaterialRepository;
import com.mobilesco.mobilesco_back.modules.material.application.ports.MaterialPersistencePort;

@Component
public class JpaMaterialPersistenceAdapter implements MaterialPersistencePort {

    private final MaterialRepository materialRepository;

    public JpaMaterialPersistenceAdapter(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    @Override
    public MaterialModel save(MaterialModel material) {
        return materialRepository.save(material);
    }

    @Override
    public List<MaterialModel> findAll() {
        return materialRepository.findAll();
    }

    @Override
    public List<MaterialModel> findAll(Sort sort) {
        return materialRepository.findAll(sort);
    }

    @Override
    public Page<MaterialModel> findAll(Pageable pageable) {
        return materialRepository.findAll(pageable);
    }

    @Override
    public List<MaterialModel> findByActivo(Boolean activo) {
        return materialRepository.findByActivo(activo);
    }

    @Override
    public Optional<MaterialModel> findById(Long id) {
        return materialRepository.findById(id);
    }

    @Override
    public Optional<MaterialModel> findByCodigo(String codigo) {
        return materialRepository.findByCodigo(codigo);
    }

    @Override
    public Optional<MaterialModel> findByNombre(String nombre) {
        return materialRepository.findByNombre(nombre);
    }

    @Override
    public boolean existsById(Long id) {
        return materialRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        materialRepository.deleteById(id);
    }
}





