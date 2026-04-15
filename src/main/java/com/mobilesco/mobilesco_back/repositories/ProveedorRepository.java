package com.mobilesco.mobilesco_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mobilesco.mobilesco_back.enums.TipoInsumo;
import com.mobilesco.mobilesco_back.models.ProveedorModel;

@Repository
public interface ProveedorRepository extends JpaRepository<ProveedorModel, Long> {

    List<ProveedorModel> findByActivo(Boolean activo);

    List<ProveedorModel> findByNombreContainingIgnoreCase(String nombre);


    List<ProveedorModel> findByActivoAndNombreContainingIgnoreCase(Boolean activo, String nombre);

    Optional<ProveedorModel> findByRazonSocialIgnoreCase(String razonSocial);

    List<ProveedorModel> findByTipoInsumo(TipoInsumo tipoInsumo);
     

     

    
    // (Opcional) si luego quieres activo+contacto:
    // List<ProveedorModel> findByActivoAndContactoContainingIgnoreCase(Boolean activo, String contacto);
}
