package com.mobilesco.mobilesco_back.modules.empleado.infrastructure.out.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mobilesco.mobilesco_back.modules.empleado.domain.models.EmpleadoModel;

public interface EmpleadoRepository extends JpaRepository<EmpleadoModel, Long> {

    @Override
    @EntityGraph(attributePaths = "areaTrabajo")
    List<EmpleadoModel> findAll();

    @Override
    @EntityGraph(attributePaths = "areaTrabajo")
    Optional<EmpleadoModel> findById(Long id);

    @EntityGraph(attributePaths = "areaTrabajo")
    List<EmpleadoModel> findByActivo(Boolean activo);

    @EntityGraph(attributePaths = "areaTrabajo")
    List<EmpleadoModel> findByNombreContainingIgnoreCase(String nombre);

    @EntityGraph(attributePaths = "areaTrabajo")
    List<EmpleadoModel> findByActivoAndNombreContainingIgnoreCase(Boolean activo, String nombre);

    Optional<EmpleadoModel> findByTelefono(String telefono);

    @EntityGraph(attributePaths = "areaTrabajo")
    @Query("""
            SELECT e
            FROM EmpleadoModel e
            WHERE (:activo IS NULL OR e.activo = :activo)
              AND (
                    :busqueda IS NULL
                    OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(e.apellidoPaterno) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(e.apellidoMaterno, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(COALESCE(e.telefono, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR CAST(e.id AS string) LIKE CONCAT('%', :busqueda, '%')
                    OR EXISTS (
                        SELECT u.id
                        FROM UsuarioModel u
                        WHERE u.empleado = e
                          AND LOWER(u.email) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    )
                  )
            """)
    Page<EmpleadoModel> buscarPaginado(
            @Param("activo") Boolean activo,
            @Param("busqueda") String busqueda,
            Pageable pageable);

}
