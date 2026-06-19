package com.mobilesco.mobilesco_back.modules.tipoinsumo.infrastructure.out.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobilesco.mobilesco_back.modules.tipoinsumo.domain.models.TipoInsumoModel;

public interface TipoInsumoRepository extends JpaRepository<TipoInsumoModel, Long> {

    List<TipoInsumoModel> findAllByOrderByActivoDescNombreAsc();

    List<TipoInsumoModel> findByActivoTrueOrderByNombreAsc();

    Optional<TipoInsumoModel> findByCodigoIgnoreCase(String codigo);

    Optional<TipoInsumoModel> findByNombreNormalizado(String nombreNormalizado);
}
