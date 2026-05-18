/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/color/infrastructure/out/adapters/JpaProductoColorValidationAdapter.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: JpaProductoColorValidationAdapter
 * CONTEXTO: Adaptador de infraestructura que implementa puertos y delega en repositorios JPA.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.color.infrastructure.out.adapters;

import org.springframework.stereotype.Component;

import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoRepository;
import com.mobilesco.mobilesco_back.modules.color.application.ports.ProductoColorValidationPort;

@Component
public class JpaProductoColorValidationAdapter implements ProductoColorValidationPort {

    private final ProductoRepository productoRepository;

    public JpaProductoColorValidationAdapter(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public boolean existsByColorId(Long colorId) {
        return productoRepository.existsByColorId(colorId);
    }
}





