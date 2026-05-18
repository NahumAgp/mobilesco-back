/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/lineaproducto/infrastructure/out/adapters/JpaLineaProductoPersistenceAdapter.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: JpaLineaProductoPersistenceAdapter
 * CONTEXTO: Adaptador de infraestructura que implementa puertos y delega en repositorios JPA.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.lineaproducto.infrastructure.out.adapters;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.mobilesco.mobilesco_back.modules.lineaproducto.domain.models.LineaProductoModel;
import com.mobilesco.mobilesco_back.modules.lineaproducto.infrastructure.out.persistence.repositories.LineaProductoRepository;
import com.mobilesco.mobilesco_back.modules.lineaproducto.application.ports.LineaProductoPersistencePort;

@Component
public class JpaLineaProductoPersistenceAdapter implements LineaProductoPersistencePort {

    private final LineaProductoRepository lineaProductoRepository;

    public JpaLineaProductoPersistenceAdapter(LineaProductoRepository lineaProductoRepository) {
        this.lineaProductoRepository = lineaProductoRepository;
    }

    @Override
    public LineaProductoModel save(LineaProductoModel lineaProducto) {
        return lineaProductoRepository.save(lineaProducto);
    }

    @Override
    public Optional<LineaProductoModel> findById(Long id) {
        return lineaProductoRepository.findById(id);
    }

    @Override
    public List<LineaProductoModel> findAll() {
        return lineaProductoRepository.findAll();
    }

    @Override
    public List<LineaProductoModel> findByActivoTrue() {
        return lineaProductoRepository.findByActivoTrue();
    }

    @Override
    public List<LineaProductoModel> buscarPorNombre(String nombre) {
        return lineaProductoRepository.buscarPorNombre(nombre);
    }

    @Override
    public boolean existsByNombreIgnoreCase(String nombre) {
        return lineaProductoRepository.existsByNombreIgnoreCase(nombre);
    }
}





