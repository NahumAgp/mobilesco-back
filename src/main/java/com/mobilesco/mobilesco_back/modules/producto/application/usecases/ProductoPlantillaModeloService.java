package com.mobilesco.mobilesco_back.modules.producto.application.usecases;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.modules.modelo.domain.models.ModeloModel;
import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.out.persistence.repositories.NivelInsumoRepository;
import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.out.persistence.repositories.NivelOperacionRepository;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoInsumoModel;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoModel;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoOperacionModel;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoInsumoRepository;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoOperacionRepository;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductoPlantillaModeloService {

    private final ProductoRepository productoRepository;
    private final ProductoInsumoRepository productoInsumoRepository;
    private final ProductoOperacionRepository productoOperacionRepository;
    private final NivelInsumoRepository nivelInsumoRepository;
    private final NivelOperacionRepository nivelOperacionRepository;

    private record PlantillaInsumoKey(Long materialId, Long insumoId) {
    }

    @Transactional
    public void aplicarAProducto(ProductoModel producto) {
        if (producto == null || producto.getId() == null || producto.getNivel() == null) {
            return;
        }

        List<ProductoInsumoModel> insumosNuevos = new ArrayList<>();
        construirPlantillaParaProducto(producto).values().forEach(plantilla -> {
            var insumo = plantilla.getInsumo();
            if (!productoInsumoRepository.existsByProductoIdAndInsumoId(producto.getId(), insumo.getId())) {
                insumosNuevos.add(ProductoInsumoModel.builder()
                        .producto(producto)
                        .insumo(insumo)
                        .cantidad(plantilla.getCantidad())
                        .desperdicioPorcentaje(plantilla.getDesperdicioPorcentaje() != null ? plantilla.getDesperdicioPorcentaje() : 0.0)
                        .observaciones("Heredado de la categoria del modelo")
                        .build());
            }
        });
        productoInsumoRepository.saveAll(insumosNuevos);

        List<ProductoOperacionModel> operacionesNuevas = new ArrayList<>();
        for (var plantilla : nivelOperacionRepository.findByNivelIdOrderByOrdenAsc(producto.getNivel().getId())) {
            var operacion = plantilla.getOperacion();
            if (!productoOperacionRepository.existsByProductoIdAndOperacionId(producto.getId(), operacion.getId())) {
                operacionesNuevas.add(ProductoOperacionModel.builder()
                        .producto(producto)
                        .operacion(operacion)
                        .cantidad(plantilla.getCantidad())
                        .orden(plantilla.getOrden())
                        .observaciones("Heredado de la categoria del modelo")
                        .activo(true)
                        .build());
            }
        }
        productoOperacionRepository.saveAll(operacionesNuevas);
    }

    @Transactional
    public void propagarAdiciones(ModeloModel modelo) {
        if (modelo == null || modelo.getId() == null) {
            return;
        }
        productoRepository.findByModeloId(modelo.getId()).forEach(this::aplicarAProducto);
    }

    private Map<Long, com.mobilesco.mobilesco_back.modules.nivel.domain.models.NivelInsumoModel> construirPlantillaParaProducto(
            ProductoModel producto) {
        Map<PlantillaInsumoKey, com.mobilesco.mobilesco_back.modules.nivel.domain.models.NivelInsumoModel> plantilla = new LinkedHashMap<>();
        nivelInsumoRepository.findByNivelIdOrderByMaterialNombreAscInsumoNombreAsc(producto.getNivel().getId())
                .forEach(item -> {
                    Long materialId = item.getMaterial() != null ? item.getMaterial().getId() : null;
                    Long insumoId = item.getInsumo() != null ? item.getInsumo().getId() : null;
                    if (insumoId != null) {
                        plantilla.put(new PlantillaInsumoKey(materialId, insumoId), item);
                    }
                });

        Map<Long, com.mobilesco.mobilesco_back.modules.nivel.domain.models.NivelInsumoModel> efectiva = new LinkedHashMap<>();
        Long materialProductoId = producto.getMaterial() != null ? producto.getMaterial().getId() : null;
        plantilla.forEach((key, item) -> {
            if (key.materialId() == null) {
                efectiva.put(key.insumoId(), item);
            }
        });
        plantilla.forEach((key, item) -> {
            if (materialProductoId != null && Objects.equals(key.materialId(), materialProductoId)) {
                efectiva.put(key.insumoId(), item);
            }
        });
        return efectiva;
    }
}
