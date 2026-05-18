/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/familia/infrastructure/out/adapters/JpaFamiliaPersistenceAdapter.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: JpaFamiliaPersistenceAdapter
 * CONTEXTO: Adaptador de infraestructura que implementa puertos y delega en repositorios JPA.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.familia.infrastructure.out.adapters;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.mobilesco.mobilesco_back.modules.familia.application.ports.FamiliaPersistencePort;
import com.mobilesco.mobilesco_back.modules.familia.domain.models.FamiliaModel;
import com.mobilesco.mobilesco_back.modules.familia.infrastructure.out.persistence.repositories.FamiliaRepository;

@Component
public class JpaFamiliaPersistenceAdapter implements FamiliaPersistencePort {

    private final FamiliaRepository familiaRepository;

    public JpaFamiliaPersistenceAdapter(FamiliaRepository familiaRepository) {
        this.familiaRepository = familiaRepository;
    }

    @Override
    public Optional<FamiliaModel> findById(Long id) {
        return familiaRepository.findById(id);
    }

    @Override
    public List<FamiliaModel> findAll() {
        return familiaRepository.findAll();
    }

    @Override
    public List<FamiliaModel> findAll(Sort sort) {
        return familiaRepository.findAll(sort);
    }

    @Override
    public Page<FamiliaModel> findAll(Pageable pageable) {
        return familiaRepository.findAll(pageable);
    }

    @Override
    public List<FamiliaModel> findByActivo(Boolean activo) {
        return familiaRepository.findByActivo(activo);
    }

    @Override
    public List<FamiliaModel> findByLineaId(Long lineaId) {
        return familiaRepository.findByLineaId(lineaId);
    }

    @Override
    public List<FamiliaModel> findByLineaIdAndActivo(Long lineaId, Boolean activo) {
        return familiaRepository.findByLineaIdAndActivo(lineaId, activo);
    }

    @Override
    public boolean existsById(Long id) {
        return familiaRepository.existsById(id);
    }

    @Override
    public boolean existsByCodigo(String codigo) {
        return familiaRepository.existsByCodigo(codigo);
    }

    @Override
    public boolean existsByNombre(String nombre) {
        return familiaRepository.existsByNombre(nombre);
    }

    @Override
    public FamiliaModel save(FamiliaModel familia) {
        return familiaRepository.save(familia);
    }

    @Override
    public void deleteById(Long id) {
        familiaRepository.deleteById(id);
    }
}





