package com.mobilesco.mobilesco_back.modules.ordenproduccion.infrastructure.out.persistence.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import com.mobilesco.mobilesco_back.modules.ordenproduccion.domain.models.OrdenProduccionOperacionModel;
public interface OrdenProduccionOperacionRepository extends JpaRepository<OrdenProduccionOperacionModel,Long> {}
