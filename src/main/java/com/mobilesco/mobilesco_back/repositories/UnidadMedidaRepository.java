package com.mobilesco.mobilesco_back.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.models.UnidadMedidaModel;

@Repository
public interface UnidadMedidaRepository extends JpaRepository<UnidadMedidaModel, Long> {

    List<UnidadMedidaModel> findByEstado(Boolean estado);
   
}
