/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/material/infrastructure/out/adapters/JpaProductoValidationAdapter.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: JpaProductoValidationAdapter
 * CONTEXTO: Adaptador de infraestructura que implementa puertos y delega en repositorios JPA.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.material.infrastructure.out.adapters;

import org.springframework.stereotype.Component;

import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoRepository;
import com.mobilesco.mobilesco_back.modules.material.application.ports.ProductoValidationPort;

@Component
public class JpaProductoValidationAdapter implements ProductoValidationPort {

    private final ProductoRepository productoRepository;

    public JpaProductoValidationAdapter(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public boolean existsByMaterialId(Long materialId) {
        return productoRepository.existsByMaterialId(materialId);
    }
}





