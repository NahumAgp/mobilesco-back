package com.mobilesco.mobilesco_back.modules.configuracion.infrastructure.out.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.mobilesco.mobilesco_back.modules.configuracion.domain.models.ConfiguracionSistemaModel;

public interface ConfiguracionSistemaRepository extends JpaRepository<ConfiguracionSistemaModel, Long> {
}
