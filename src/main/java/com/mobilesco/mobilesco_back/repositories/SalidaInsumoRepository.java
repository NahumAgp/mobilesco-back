package com.mobilesco.mobilesco_back.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.models.SalidaInsumoModel;

@Repository
public interface SalidaInsumoRepository extends JpaRepository<SalidaInsumoModel, Long> {
    List<SalidaInsumoModel> findAllByOrderByFechaSalidaDesc();
}
