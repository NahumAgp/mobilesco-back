package com.mobilesco.mobilesco_back.modules.producto.application.usecases;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.modules.modelo.domain.models.ModeloModel;
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

    @Transactional
    public void aplicarAProducto(ProductoModel producto) {
        if (producto == null || producto.getId() == null || producto.getModelo() == null) {
            return;
        }

        ModeloModel modelo = producto.getModelo();
        List<ProductoInsumoModel> insumosNuevos = new ArrayList<>();
        modelo.getInsumos().forEach(insumo -> {
            if (!productoInsumoRepository.existsByProductoIdAndInsumoId(producto.getId(), insumo.getId())) {
                insumosNuevos.add(ProductoInsumoModel.builder()
                        .producto(producto)
                        .insumo(insumo)
                        .cantidad(null)
                        .desperdicioPorcentaje(0.0)
                        .observaciones("Heredado de la plantilla del modelo")
                        .build());
            }
        });
        productoInsumoRepository.saveAll(insumosNuevos);

        List<ProductoOperacionModel> operacionesNuevas = new ArrayList<>();
        for (int index = 0; index < modelo.getOperaciones().size(); index++) {
            var operacion = modelo.getOperaciones().get(index);
            if (!productoOperacionRepository.existsByProductoIdAndOperacionId(producto.getId(), operacion.getId())) {
                operacionesNuevas.add(ProductoOperacionModel.builder()
                        .producto(producto)
                        .operacion(operacion)
                        .cantidad(null)
                        .orden(index + 1)
                        .observaciones("Heredado de la plantilla del modelo")
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
}
