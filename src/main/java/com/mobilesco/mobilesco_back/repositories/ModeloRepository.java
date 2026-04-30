package com.mobilesco.mobilesco_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.models.ModeloModel;

@Repository
public interface ModeloRepository extends JpaRepository<ModeloModel, Long> {

    Optional<ModeloModel> findByCodigo(String codigo);

    List<ModeloModel> findByFamiliaId(Long familiaId);

    boolean existsByCodigo(String codigo);

    @Query("SELECT p FROM ModeloModel p WHERE " +
           "(:codigo IS NULL OR LOWER(p.codigo) LIKE LOWER(CONCAT('%', :codigo, '%'))) AND " +
           "(:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
           "(:familiaId IS NULL OR p.familia.id = :familiaId)")
    List<ModeloModel> buscarConFiltros(
            @Param("codigo") String codigo,
            @Param("nombre") String nombre,
            @Param("familiaId") Long familiaId
    );
}
