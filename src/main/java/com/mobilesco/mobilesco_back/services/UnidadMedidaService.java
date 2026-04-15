package com.mobilesco.mobilesco_back.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mobilesco.mobilesco_back.dto.unidadMedida.UnidadMedidaCreateDTO;
import com.mobilesco.mobilesco_back.dto.unidadMedida.UnidadMedidaResponseDTO;
import com.mobilesco.mobilesco_back.dto.unidadMedida.UnidadMedidaUpdateDTO;
import com.mobilesco.mobilesco_back.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.models.UnidadMedidaModel;
import com.mobilesco.mobilesco_back.repositories.UnidadMedidaRepository;


@Service
public class UnidadMedidaService {

    @Autowired
    private UnidadMedidaRepository unidadMedidaRepository;

    private UnidadMedidaResponseDTO mapToResponseDTO(UnidadMedidaModel unidadMedida) {
        UnidadMedidaResponseDTO dto = new UnidadMedidaResponseDTO();
        dto.setId(unidadMedida.getId());
        dto.setNombre(unidadMedida.getNombre());
        dto.setSimbolo(unidadMedida.getSimbolo());
        dto.setTipo(unidadMedida.getTipo());
        dto.setEstado(unidadMedida.getEstado());
        dto.setFechaRegistro(unidadMedida.getFechaRegistro());
        return dto;
    }

    private List<UnidadMedidaResponseDTO> mapToResponseDTOList(List<UnidadMedidaModel> unidades) {
        return unidades.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // --------- CREATE ---------
    public UnidadMedidaResponseDTO crear(UnidadMedidaCreateDTO umM) {
        UnidadMedidaModel unidadMedida = new UnidadMedidaModel();
        unidadMedida.setNombre(umM.getNombre());
        unidadMedida.setSimbolo(umM.getSimbolo());
        unidadMedida.setTipo(umM.getTipo());
        unidadMedida.setEstado(true); //<-- regla: nueva unidad inicia activa
        UnidadMedidaModel guardado = unidadMedidaRepository.save(unidadMedida);
        return mapToResponseDTO(guardado);
    }

    // --------- READ ---------
    public List<UnidadMedidaResponseDTO> obtenerTodos() {
        return mapToResponseDTOList(unidadMedidaRepository.findAll());
    }

    public UnidadMedidaResponseDTO obtenerPorId(Long id) {

        UnidadMedidaModel UnidadMedida = unidadMedidaRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Unidad de Medida no encontrada"));

        return mapToResponseDTO(UnidadMedida);
    }

    //----------UPDATE----------
    public Optional<UnidadMedidaResponseDTO> actualizar(Long id, UnidadMedidaUpdateDTO umM) {
        return unidadMedidaRepository.findById(id).map(unidadMedida -> {
            unidadMedida.setNombre(umM.getNombre());
            unidadMedida.setSimbolo(umM.getSimbolo());
            unidadMedida.setTipo(umM.getTipo());
            UnidadMedidaModel actualizado = unidadMedidaRepository.save(unidadMedida);
            return mapToResponseDTO(actualizado);
        });
           
    }

    // --------- DELETE ---------
    public boolean eliminar(Long id) {
        if (!unidadMedidaRepository.existsById(id)) return false;
            unidadMedidaRepository.deleteById(id);
            return true;
    }   

    //------------Desactivar----------
    public boolean desactivar(Long id) {
        return unidadMedidaRepository.findById(id).map(unidadMedida -> {
            unidadMedida.setEstado(false);
            unidadMedidaRepository.save(unidadMedida);
            return true;
        }).orElse(false);   
    }

     //------------Activar----------
    public boolean activar(Long id) {
        return unidadMedidaRepository.findById(id).map(unidadMedida -> {
            unidadMedida.setEstado(true);
            unidadMedidaRepository.save(unidadMedida);
            return true;
        }).orElse(false);
    }


}
