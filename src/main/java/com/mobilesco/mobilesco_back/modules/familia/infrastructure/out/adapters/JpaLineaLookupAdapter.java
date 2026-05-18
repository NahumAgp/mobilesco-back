/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/familia/infrastructure/out/adapters/JpaLineaLookupAdapter.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: JpaLineaLookupAdapter
 * CONTEXTO: Adaptador de infraestructura que implementa puertos y delega en repositorios JPA.
 * NOTAS: Mantener desacoplamiento por interfaces; evitar dependencias directas a implementaciones concretas.
 */
package com.mobilesco.mobilesco_back.modules.familia.infrastructure.out.adapters;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.mobilesco.mobilesco_back.modules.linea.domain.models.LineaModel;
import com.mobilesco.mobilesco_back.modules.linea.infrastructure.out.persistence.repositories.LineaRepository;
import com.mobilesco.mobilesco_back.modules.familia.application.ports.LineaLookupPort;

@Component
public class JpaLineaLookupAdapter implements LineaLookupPort {

    private final LineaRepository lineaRepository;

    public JpaLineaLookupAdapter(LineaRepository lineaRepository) {
        this.lineaRepository = lineaRepository;
    }

    @Override
    public Optional<LineaModel> findById(Long id) {
        return lineaRepository.findById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return lineaRepository.existsById(id);
    }
}





