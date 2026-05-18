/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/categoria/infrastructure/out/adapters/JpaCategoriaPersistenceAdapter.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: JpaCategoriaPersistenceAdapter
 * CONTEXTO: Adaptador de infraestructura que implementa puertos y delega en repositorios JPA.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.categoria.infrastructure.out.adapters;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.mobilesco.mobilesco_back.modules.categoria.domain.models.CategoriaModel;
import com.mobilesco.mobilesco_back.modules.categoria.infrastructure.out.persistence.repositories.CategoriaRepository;
import com.mobilesco.mobilesco_back.modules.categoria.application.ports.CategoriaPersistencePort;

@Component
public class JpaCategoriaPersistenceAdapter implements CategoriaPersistencePort {

    private final CategoriaRepository categoriaRepository;

    public JpaCategoriaPersistenceAdapter(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public CategoriaModel save(CategoriaModel categoria) {
        return categoriaRepository.save(categoria);
    }

    @Override
    public Optional<CategoriaModel> findById(Long id) {
        return categoriaRepository.findById(id);
    }

    @Override
    public List<CategoriaModel> findAll() {
        return categoriaRepository.findAll();
    }

    @Override
    public List<CategoriaModel> findAll(Sort sort) {
        return categoriaRepository.findAll(sort);
    }

    @Override
    public List<CategoriaModel> findByActivoTrue() {
        return categoriaRepository.findByActivoTrue();
    }

    @Override
    public boolean existsByNombreIgnoreCase(String nombre) {
        return categoriaRepository.existsByNombreIgnoreCase(nombre);
    }

    @Override
    public List<CategoriaModel> buscarPorNombre(String nombre) {
        return categoriaRepository.buscarPorNombre(nombre);
    }
}





