/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/familia/infrastructure/out/adapters/JpaModeloFamiliaValidationAdapter.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: JpaModeloFamiliaValidationAdapter
 * CONTEXTO: Adaptador de infraestructura que implementa puertos y delega en repositorios JPA.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.familia.infrastructure.out.adapters;

import org.springframework.stereotype.Component;

import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.out.persistence.repositories.ModeloRepository;
import com.mobilesco.mobilesco_back.modules.familia.application.ports.ModeloFamiliaValidationPort;

@Component
public class JpaModeloFamiliaValidationAdapter implements ModeloFamiliaValidationPort {

    private final ModeloRepository modeloRepository;

    public JpaModeloFamiliaValidationAdapter(ModeloRepository modeloRepository) {
        this.modeloRepository = modeloRepository;
    }

    @Override
    public boolean existsByFamiliaId(Long familiaId) {
        return modeloRepository.existsByFamiliaId(familiaId);
    }
}





