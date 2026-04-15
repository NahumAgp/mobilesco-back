package com.mobilesco.mobilesco_back.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.dto.DistribucionCosto.DetalleCostoDTO;
import com.mobilesco.mobilesco_back.dto.DistribucionCosto.DistribucionCostoResponseDTO;
import com.mobilesco.mobilesco_back.dto.DistribucionCosto.DistribucionPorProductoDTO;
import com.mobilesco.mobilesco_back.dto.DistribucionCosto.DistribucionResumenDTO;
import com.mobilesco.mobilesco_back.enums.BaseDistribucion;
import com.mobilesco.mobilesco_back.enums.TipoCostoIndirecto;
import com.mobilesco.mobilesco_back.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.models.CostoIndirectoModel;
import com.mobilesco.mobilesco_back.models.DistribucionCostoModel;
import com.mobilesco.mobilesco_back.models.ProductoModel;
import com.mobilesco.mobilesco_back.repositories.CostoIndirectoRepository;
import com.mobilesco.mobilesco_back.repositories.DistribucionCostoRepository;
import com.mobilesco.mobilesco_back.repositories.ProduccionRepository;
import com.mobilesco.mobilesco_back.repositories.ProductoOperacionRepository;
import com.mobilesco.mobilesco_back.repositories.ProductoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistribucionCostoService {

    private final DistribucionCostoRepository distribucionRepository;
    private final CostoIndirectoRepository costoIndirectoRepository;
    private final ProductoRepository productoRepository;
    private final ProductoOperacionRepository productoOperacionRepository;
    private final ProduccionRepository produccionRepository;

    /**
     * CALCULAR Y GUARDAR la distribución de costos para un mes específico
     */
    @Transactional
    public List<DistribucionCostoResponseDTO> calcularDistribucionMensual(Integer anio, Integer mes) {
        log.info("Calculando distribución de costos para {}/{}", mes, anio);

        // Validar parámetros
        if (anio == null || mes == null || mes < 1 || mes > 12) {
            throw new ValidationException("Año y mes inválidos");
        }

        // 1. Obtener todos los costos indirectos activos
        List<CostoIndirectoModel> costos = costoIndirectoRepository.findByActivoTrue();
        
        // 2. Obtener productos activos
        List<ProductoModel> productos = productoRepository.findByActivoTrue();
        
        if (productos.isEmpty()) {
            throw new ValidationException("No hay productos activos para distribuir costos");
        }

        // 3. Eliminar distribución previa del mismo período
        distribucionRepository.deleteByAnioAndMes(anio, mes);

        // 4. Calcular bases de distribución para el período
        BasesDistribucionDTO bases = calcularBasesDistribucion(anio, mes, productos);

        // 5. Distribuir cada costo
        List<DistribucionCostoModel> distribuciones = new ArrayList<>();
        
        for (CostoIndirectoModel costo : costos) {
            distribuciones.addAll(distribuirCosto(costo, anio, mes, productos, bases));
        }

        // 6. Guardar todas las distribuciones
        List<DistribucionCostoModel> saved = distribucionRepository.saveAll(distribuciones);
        log.info("Distribución completada: {} registros guardados", saved.size());

        return saved.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Distribuir un costo específico según su base
     */
    private List<DistribucionCostoModel> distribuirCosto(
            CostoIndirectoModel costo,
            Integer anio,
            Integer mes,
            List<ProductoModel> productos,
            BasesDistribucionDTO bases) {
        
        List<DistribucionCostoModel> resultados = new ArrayList<>();
        
        // Validar costo
        if (costo == null || !costo.getActivo()) {
            return resultados;
        }
        
        // Calcular monto total a distribuir
        double montoTotal = calcularMontoTotalCosto(costo, anio, mes);
        
        if (montoTotal == 0) {
            log.debug("Costo {} tiene monto 0, se omite", costo.getCodigo());
            return resultados;
        }

        // Obtener base de distribución según el tipo
        Map<Long, Double> basePorProducto = obtenerBasePorProducto(costo, productos, bases);
        
        double sumaBase = basePorProducto.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        
        if (sumaBase == 0) {
            log.debug("Base de distribución suma 0 para costo {}", costo.getCodigo());
            return resultados;
        }

        // Distribuir proporcionalmente
        for (ProductoModel producto : productos) {
            double baseProducto = basePorProducto.getOrDefault(producto.getId(), 0.0);
            
            if (baseProducto > 0) {
                double porcentaje = baseProducto / sumaBase;
                double montoAsignado = montoTotal * porcentaje;
                
                DistribucionCostoModel distribucion = DistribucionCostoModel.builder()
                        .costoIndirecto(costo)
                        .producto(producto)
                        .anio(anio)
                        .mes(mes)
                        .montoAsignado(montoAsignado)
                        .porcentajeParticipacion(porcentaje * 100)
                        .baseCalculo(baseProducto)
                        .build();
                
                resultados.add(distribucion);
            }
        }
        
        return resultados;
    }

    /**
     * Calcular monto total de un costo para el período
     * CORREGIDO: Convert switch to rule switch + manejo de nulls
     */
    private double calcularMontoTotalCosto(CostoIndirectoModel costo, Integer anio, Integer mes) {
        if (costo == null) return 0.0;
        
        TipoCostoIndirecto tipo = costo.getTipo();
        if (tipo == null) return 0.0;
        
        // Usando switch expression (Java 14+) - sin advertencias
        return switch (tipo) {
            case FIJO -> {
                Double monto = costo.getMontoMensual();
                yield monto != null ? monto : 0.0;
            }
            case VARIABLE -> {
                BaseDistribucion baseDist = costo.getBaseDistribucion();
                if (baseDist == null) yield 0.0;
                
                double actividad = obtenerActividadMensual(baseDist, anio, mes);
                Double tasa = costo.getTasaVariable();
                yield actividad * (tasa != null ? tasa : 0.0);
            }
            default -> 0.0;
        };
    }

    /**
     * Obtener base de distribución por producto
     * CORREGIDO: Convert switch to rule switch
     */
    private Map<Long, Double> obtenerBasePorProducto(
            CostoIndirectoModel costo,
            List<ProductoModel> productos,
            BasesDistribucionDTO bases) {
        
        if (costo == null || costo.getBaseDistribucion() == null || productos == null || bases == null) {
            return new HashMap<>();
        }
        
        BaseDistribucion baseDist = costo.getBaseDistribucion();
        
        // Usando switch expression (Java 14+) - sin advertencias
        return switch (baseDist) {
            case HORAS_MOD -> bases.horasModPorProducto;
            case HORAS_MAQUINA -> bases.horasMaquinaPorProducto;
            case UNIDADES -> bases.unidadesProducidasPorProducto;
            case PESO -> bases.pesoPorProducto;
            case COSTO_DIRECTO -> bases.costoDirectoPorProducto;
            case METROS_CUADRADOS -> {
                Map<Long, Double> resultado = new HashMap<>();
                double proporcion = productos.isEmpty() ? 0.0 : 1.0 / productos.size();
                for (ProductoModel p : productos) {
                    resultado.put(p.getId(), proporcion);
                }
                yield resultado;
            }
            default -> new HashMap<>();
        };
    }

    /**
     * Calcular todas las bases de distribución para el período
     */
    private BasesDistribucionDTO calcularBasesDistribucion(
            Integer anio, 
            Integer mes, 
            List<ProductoModel> productos) {
        
        BasesDistribucionDTO bases = new BasesDistribucionDTO();
        
        // Convertir a LocalDate para consultas
        LocalDate inicio = LocalDate.of(anio, mes, 1);
        LocalDate fin = inicio.withDayOfMonth(inicio.lengthOfMonth());
        
        for (ProductoModel producto : productos) {
            if (producto == null) continue;
            
            Long prodId = producto.getId();
            
            // Horas MOD (de producto_operacion)
            Double minutosPorUnidad = productoOperacionRepository.sumarTiempoTotalByProducto(prodId);
            double horasPorUnidad = (minutosPorUnidad != null ? minutosPorUnidad : 0.0) / 60.0;
            
            // Unidades producidas (de producción)
            Long unidadesProducidas = produccionRepository.countByProductoAndPeriodo(prodId, inicio, fin);
            double unidades = unidadesProducidas != null ? unidadesProducidas.doubleValue() : 0.0;
            bases.unidadesProducidasPorProducto.put(prodId, unidades);
            
            // Horas MOD totales = horas por unidad * unidades producidas
            bases.horasModPorProducto.put(prodId, horasPorUnidad * unidades);
            
            // Peso (del producto * unidades)
            Double pesoUnitario = producto.getPesoKg();
            double peso = (pesoUnitario != null ? pesoUnitario : 0.0) * unidades;
            bases.pesoPorProducto.put(prodId, peso);
            
            // Inicializar otros mapas con 0
            bases.horasMaquinaPorProducto.put(prodId, 0.0);
            bases.costoDirectoPorProducto.put(prodId, 0.0);
        }
        
        return bases;
    }

    /**
     * Obtener actividad mensual para costos variables
     * CORREGIDO: Convert switch to rule switch + manejo de nulls
     */
    private double obtenerActividadMensual(BaseDistribucion base, Integer anio, Integer mes) {
        if (base == null || anio == null || mes == null) return 0.0;
        
        // Convertir a LocalDate para consultas
        LocalDate inicio = LocalDate.of(anio, mes, 1);
        LocalDate fin = inicio.withDayOfMonth(inicio.lengthOfMonth());
        
        // Usando switch expression (Java 14+) - sin advertencias
        return switch (base) {
            case HORAS_MOD -> {
                Double horas = produccionRepository.totalHorasModEnPeriodo(inicio, fin);
                yield horas != null ? horas : 0.0;
            }
            case HORAS_MAQUINA -> {
                Double horas = produccionRepository.totalHorasMaquinaEnPeriodo(inicio, fin);
                yield horas != null ? horas : 0.0;
            }
            case UNIDADES -> {
                Long unidades = produccionRepository.totalUnidadesEnPeriodo(inicio, fin);
                yield unidades != null ? unidades.doubleValue() : 0.0;
            }
            default -> 0.0;
        };
    }

    /**
     * Obtener distribución por período
     */
    @Transactional(readOnly = true)
    public List<DistribucionCostoResponseDTO> obtenerDistribucionPorPeriodo(Integer anio, Integer mes) {
        if (anio == null || mes == null) {
            return Collections.emptyList();
        }
        
        return distribucionRepository.findByAnioAndMes(anio, mes)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener resumen por período
     * CORREGIDO: Unboxing possibly null value
     */
    @Transactional(readOnly = true)
    public DistribucionResumenDTO obtenerResumenPorPeriodo(Integer anio, Integer mes) {
        if (anio == null || mes == null) {
            throw new ValidationException("Año y mes son obligatorios");
        }
        
        List<DistribucionCostoModel> distribuciones = distribucionRepository.findDistribucionCompleta(anio, mes);
        
        if (distribuciones.isEmpty()) {
            throw new ValidationException("No hay distribución para el período " + mes + "/" + anio);
        }

        // Agrupar por producto
        Map<Long, List<DistribucionCostoModel>> porProducto = distribuciones.stream()
                .filter(d -> d.getProducto() != null)
                .collect(Collectors.groupingBy(d -> d.getProducto().getId()));

        List<DistribucionPorProductoDTO> productosDTO = new ArrayList<>();
        double totalGeneral = 0;

        for (Map.Entry<Long, List<DistribucionCostoModel>> entry : porProducto.entrySet()) {
            List<DistribucionCostoModel> distProducto = entry.getValue();
            if (distProducto.isEmpty()) continue;
            
            ProductoModel producto = distProducto.get(0).getProducto();
            if (producto == null) continue;
            
            // CORREGIDO: manejo seguro de nulls
            double totalProducto = 0.0;
            for (DistribucionCostoModel d : distProducto) {
                Double monto = d.getMontoAsignado();
                totalProducto += (monto != null ? monto : 0.0);
            }
            
            List<DetalleCostoDTO> detalles = new ArrayList<>();
            for (DistribucionCostoModel d : distProducto) {
                CostoIndirectoModel costo = d.getCostoIndirecto();
                if (costo == null) continue;
                
                Double monto = d.getMontoAsignado();
                BaseDistribucion base = costo.getBaseDistribucion();
                
                detalles.add(DetalleCostoDTO.builder()
                        .costoCodigo(costo.getCodigo())
                        .costoNombre(costo.getNombre())
                        .monto(monto != null ? monto : 0.0)
                        .baseDistribucion(base != null ? base.toString() : "N/A")
                        .build());
            }

            productosDTO.add(DistribucionPorProductoDTO.builder()
                    .productoId(producto.getId())
                    .productoSku(producto.getSku())
                    .productoNombre(producto.getNombre())
                    .montoAsignado(totalProducto)
                    .porcentaje(0.0) // Se calculará después
                    .detalles(detalles)
                    .build());
            
            totalGeneral += totalProducto;
        }

        // Calcular porcentajes
        if (totalGeneral > 0) {
            for (DistribucionPorProductoDTO p : productosDTO) {
                double monto = Objects.requireNonNullElse(p.getMontoAsignado(), 0.0);
                p.setPorcentaje((monto / totalGeneral) * 100);
            }
        }

        return DistribucionResumenDTO.builder()
                .anio(anio)
                .mes(mes)
                .totalCostosIndirectos(totalGeneral)
                .productos(productosDTO)
                .build();
    }

    /**
     * Mapear a ResponseDTO
     * CORREGIDO: Unboxing possibly null value
     */
    private DistribucionCostoResponseDTO mapToResponseDTO(DistribucionCostoModel dist) {
        if (dist == null) return null;
        
        CostoIndirectoModel costo = dist.getCostoIndirecto();
        ProductoModel producto = dist.getProducto();
        
        // CORREGIDO: manejo seguro de nulls
        Double montoAsignado = dist.getMontoAsignado();
        Double porcentaje = dist.getPorcentajeParticipacion();
        Double baseCalculo = dist.getBaseCalculo();
        
        return DistribucionCostoResponseDTO.builder()
                .id(dist.getId())
                .costoIndirectoId(costo != null ? costo.getId() : null)
                .costoIndirectoCodigo(costo != null ? costo.getCodigo() : null)
                .costoIndirectoNombre(costo != null ? costo.getNombre() : null)
                .productoId(producto != null ? producto.getId() : null)
                .productoSku(producto != null ? producto.getSku() : null)
                .productoNombre(producto != null ? producto.getNombre() : null)
                .anio(dist.getAnio())
                .mes(dist.getMes())
                .montoAsignado(montoAsignado != null ? montoAsignado : 0.0)
                .porcentajeParticipacion(porcentaje != null ? porcentaje : 0.0)
                .baseCalculo(baseCalculo != null ? baseCalculo : 0.0)
                .fechaRegistro(dist.getFechaRegistro())
                .build();
    }

    // Clase auxiliar para pasar las bases de distribución
    private static class BasesDistribucionDTO {
        Map<Long, Double> horasModPorProducto = new HashMap<>();
        Map<Long, Double> horasMaquinaPorProducto = new HashMap<>();
        Map<Long, Double> unidadesProducidasPorProducto = new HashMap<>();
        Map<Long, Double> pesoPorProducto = new HashMap<>();
        Map<Long, Double> costoDirectoPorProducto = new HashMap<>();
        
        public BasesDistribucionDTO() {
            // Inicializar mapas vacíos
        }
    }
}