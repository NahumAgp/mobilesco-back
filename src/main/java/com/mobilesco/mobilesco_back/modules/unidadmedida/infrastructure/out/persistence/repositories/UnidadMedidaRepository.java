package com.mobilesco.mobilesco_back.modules.unidadmedida.infrastructure.out.persistence.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobilesco.mobilesco_back.modules.unidadmedida.domain.models.UnidadMedidaModel;

public interface UnidadMedidaRepository extends JpaRepository<UnidadMedidaModel, Long> {

    List<UnidadMedidaModel> findByEstado(Boolean estado);
   
}
