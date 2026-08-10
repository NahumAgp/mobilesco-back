package com.mobilesco.mobilesco_back.modules.producto.application.usecases;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.modules.familia.domain.models.FamiliaModel;
import com.mobilesco.mobilesco_back.modules.modelo.domain.models.ModeloModel;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoModel;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoSkuValidacionResponseDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoSkuValidacionResponseDTO.DetalleSkuDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductoSkuValidacionService {

    private static final String CORRECTO = "CORRECTO";
    private static final String DESCUADRADO = "DESCUADRADO";
    private static final String NO_VALIDABLE = "NO_VALIDABLE";
    private static final String CONFLICTO = "CONFLICTO";

    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public ProductoSkuValidacionResponseDTO validar() {
        return evaluar(productoRepository.findAllByOrderByIdAsc(), 0).respuesta();
    }

    @Transactional
    public ProductoSkuValidacionResponseDTO corregir() {
        List<ProductoModel> productos = productoRepository.findAllForSkuUpdate();
        Evaluacion evaluacion = evaluar(productos, 0);
        Map<Long, String> skuEsperadoPorId = new LinkedHashMap<>();

        evaluacion.estados().forEach((productoId, estado) -> {
            if (DESCUADRADO.equals(estado)) {
                skuEsperadoPorId.put(productoId, evaluacion.skusEsperados().get(productoId));
            }
        });

        if (skuEsperadoPorId.isEmpty()) {
            return evaluacion.respuesta();
        }

        List<ProductoModel> aCorregir = productos.stream()
                .filter(producto -> skuEsperadoPorId.containsKey(producto.getId()))
                .toList();

        // Libera primero los SKUs anteriores para soportar intercambios sin violar la restriccion unica.
        aCorregir.forEach(producto -> producto.setSku("TMP" + UUID.randomUUID().toString().replace("-", "")));
        productoRepository.saveAllAndFlush(aCorregir);

        aCorregir.forEach(producto -> producto.setSku(skuEsperadoPorId.get(producto.getId())));
        productoRepository.saveAllAndFlush(aCorregir);

        return evaluar(productos, aCorregir.size()).respuesta();
    }

    private Evaluacion evaluar(List<ProductoModel> productos, int actualizados) {
        Map<Long, ResultadoSku> resultados = new LinkedHashMap<>();
        Map<String, List<Long>> idsPorSkuEsperado = new HashMap<>();

        for (ProductoModel producto : productos) {
            ResultadoSku resultado = calcularSku(producto);
            resultados.put(producto.getId(), resultado);
            if (resultado.sku() != null) {
                idsPorSkuEsperado
                        .computeIfAbsent(resultado.sku().toLowerCase(Locale.ROOT), ignored -> new ArrayList<>())
                        .add(producto.getId());
            }
        }

        int correctos = 0;
        int corregibles = 0;
        int bloqueados = 0;
        List<DetalleSkuDTO> detalles = new ArrayList<>();
        Map<Long, String> estados = new LinkedHashMap<>();
        Map<Long, String> skusEsperados = new LinkedHashMap<>();

        for (ProductoModel producto : productos) {
            ResultadoSku resultado = resultados.get(producto.getId());
            String estado;
            String motivo = resultado.motivo();

            if (resultado.sku() == null) {
                estado = NO_VALIDABLE;
                bloqueados++;
            } else if (idsPorSkuEsperado.get(resultado.sku().toLowerCase(Locale.ROOT)).size() > 1) {
                estado = CONFLICTO;
                motivo = "La clasificación actual genera el mismo SKU para más de un producto.";
                bloqueados++;
            } else if (Objects.equals(producto.getSku(), resultado.sku())) {
                estado = CORRECTO;
                correctos++;
            } else {
                estado = DESCUADRADO;
                motivo = "El SKU no coincide con la clasificación vigente.";
                corregibles++;
            }

            estados.put(producto.getId(), estado);
            skusEsperados.put(producto.getId(), resultado.sku());
            if (!CORRECTO.equals(estado)) {
                detalles.add(DetalleSkuDTO.builder()
                        .productoId(producto.getId())
                        .productoNombre(producto.getNombre())
                        .skuActual(producto.getSku())
                        .skuEsperado(resultado.sku())
                        .estado(estado)
                        .motivo(motivo)
                        .build());
            }
        }

        ProductoSkuValidacionResponseDTO respuesta = ProductoSkuValidacionResponseDTO.builder()
                .total(productos.size())
                .correctos(correctos)
                .inconsistentes(corregibles + bloqueados)
                .corregibles(corregibles)
                .bloqueados(bloqueados)
                .actualizados(actualizados)
                .detalles(detalles)
                .build();
        return new Evaluacion(respuesta, estados, skusEsperados);
    }

    private ResultadoSku calcularSku(ProductoModel producto) {
        ModeloModel modelo = producto.getModelo();
        if (modelo == null) return error("El producto no tiene modelo.");
        FamiliaModel familia = modelo.getFamilia();
        if (familia == null) return error("El modelo no tiene familia.");
        if (familia.getLinea() == null) return error("La familia no tiene línea.");
        if (modelo.getSubfamilia() != null
                && (modelo.getSubfamilia().getFamilia() == null
                    || !Objects.equals(modelo.getSubfamilia().getFamilia().getId(), familia.getId()))) {
            return error("La subfamilia del modelo no pertenece a su familia.");
        }
        if (producto.getNivel() == null) return error("El producto no tiene categoría.");
        if (producto.getNivel().getModelo() == null
                || !Objects.equals(producto.getNivel().getModelo().getId(), modelo.getId())) {
            return error("La categoría no pertenece al modelo del producto.");
        }
        if (producto.getMaterial() == null) return error("El producto no tiene material.");
        if (producto.getColor() == null) return error("El producto no tiene color.");

        try {
            String subfamilia = modelo.getSubfamilia() == null
                    ? ""
                    : codigo(modelo.getSubfamilia().getCodigo(), "subfamilia");
            String sku = codigo(familia.getLinea().getCodigo(), "línea")
                    + codigo(familia.getCodigo(), "familia")
                    + subfamilia
                    + codigo(modelo.getCodigo(), "modelo")
                    + "-" + codigo(producto.getNivel().getCodigo(), "categoría")
                    + "-" + codigo(producto.getMaterial().getCodigo(), "material")
                    + "-" + codigo(producto.getColor().getCodigo(), "color");
            if (sku.length() > 50) return error("El SKU calculado supera los 50 caracteres.");
            return new ResultadoSku(sku, null);
        } catch (IllegalArgumentException ex) {
            return error(ex.getMessage());
        }
    }

    private String codigo(String valor, String catalogo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Falta el código de " + catalogo + ".");
        }
        String normalizado = valor.trim().toUpperCase(Locale.ROOT);
        if (!normalizado.matches("[A-Z0-9]+")) {
            throw new IllegalArgumentException("El código de " + catalogo + " contiene caracteres no permitidos.");
        }
        return normalizado;
    }

    private ResultadoSku error(String motivo) {
        return new ResultadoSku(null, motivo);
    }

    private record ResultadoSku(String sku, String motivo) {}

    private record Evaluacion(
            ProductoSkuValidacionResponseDTO respuesta,
            Map<Long, String> estados,
            Map<Long, String> skusEsperados) {}
}
