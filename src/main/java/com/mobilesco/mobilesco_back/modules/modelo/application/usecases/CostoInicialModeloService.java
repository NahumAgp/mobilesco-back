package com.mobilesco.mobilesco_back.modules.modelo.application.usecases;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.CostoInicialModeloDTO;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.CostoInicialModeloDTO.Renglon;

@Service
public class CostoInicialModeloService {

    private static final Pattern MATERIAL_SUFFIX = Pattern.compile("(PO|N|F|P)(?:0[123])?$");

    private final JdbcTemplate jdbc;

    public CostoInicialModeloService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public List<CostoInicialModeloDTO> obtenerPorModelo(Long modeloId) {
        Map<String, MaterialFuente> materiales = jdbc.query("""
                SELECT mat.id, mat.codigo, mat.nombre
                FROM modelo_material mm
                JOIN material mat ON mat.id = mm.material_id
                WHERE mm.modelo_id = ?
                """, (rs, rowNum) -> new MaterialFuente(
                rs.getLong("id"), rs.getString("codigo"), rs.getString("nombre")), modeloId)
                .stream().collect(Collectors.toMap(MaterialFuente::codigo, Function.identity()));
        return jdbc.query("""
                SELECT cr.fuente_hoja_id, cr.nivel_id, c.nombre AS categoria,
                       h.codigo_fuente, h.archivo, h.hoja, n.costo_inicial_pendiente,
                       cr.materia_prima, cr.mano_obra, cr.costo_primo, cr.indirectos,
                       cr.costo_fabricacion, cr.minutos_elaboracion,
                       cr.precio_base_sin_iva, cr.precio_menudeo_sin_iva, cr.precio_foraneo_sin_iva,
                       cr.precio_base_con_iva, cr.precio_menudeo_con_iva, cr.precio_foraneo_con_iva
                FROM costo_referencia_catalogo cr
                JOIN costo_fuente_hoja h ON h.id = cr.fuente_hoja_id
                JOIN niveles n ON n.id = cr.nivel_id
                JOIN categoria c ON c.id = n.categoria_id
                WHERE n.producto_base_id = ?
                ORDER BY c.nombre, h.codigo_fuente, h.hoja
                """, (rs, rowNum) -> mapFicha(rs, materiales), modeloId);
    }

    private CostoInicialModeloDTO mapFicha(ResultSet rs, Map<String, MaterialFuente> materialesModelo) throws SQLException {
        long fuenteId = rs.getLong("fuente_hoja_id");
        MaterialFuente material = materialDeFicha(rs.getString("codigo_fuente"), materialesModelo);
        List<Renglon> materiales = new ArrayList<>();
        List<Renglon> operaciones = new ArrayList<>();
        jdbc.query("""
                SELECT fila, tipo, descripcion, unidad, cantidad, precio_unitario,
                       importe, estado, motivo, insumo_id, operacion_id
                FROM costo_fuente_renglon
                WHERE fuente_hoja_id = ? AND tipo IN ('MATERIAL', 'OPERACION')
                ORDER BY fila, id
                """, row -> {
            Renglon renglon = new Renglon(
                    row.getInt("fila"), row.getString("descripcion"), row.getString("unidad"),
                    row.getBigDecimal("cantidad"), row.getBigDecimal("precio_unitario"),
                    row.getBigDecimal("importe"), row.getString("estado"), row.getString("motivo"),
                    nullableLong(row, "insumo_id"), nullableLong(row, "operacion_id"));
            if ("MATERIAL".equals(row.getString("tipo"))) {
                materiales.add(renglon);
            } else {
                operaciones.add(renglon);
            }
        }, fuenteId);
        return new CostoInicialModeloDTO(
                fuenteId, rs.getLong("nivel_id"), material != null ? material.id() : null,
                material != null ? material.nombre() : null, rs.getString("categoria"),
                rs.getString("codigo_fuente"), rs.getString("archivo"), rs.getString("hoja"),
                rs.getBoolean("costo_inicial_pendiente"),
                rs.getBigDecimal("materia_prima"), rs.getBigDecimal("mano_obra"),
                rs.getBigDecimal("costo_primo"), rs.getBigDecimal("indirectos"),
                rs.getBigDecimal("costo_fabricacion"), rs.getBigDecimal("minutos_elaboracion"),
                rs.getBigDecimal("precio_base_sin_iva"), rs.getBigDecimal("precio_menudeo_sin_iva"),
                rs.getBigDecimal("precio_foraneo_sin_iva"), rs.getBigDecimal("precio_base_con_iva"),
                rs.getBigDecimal("precio_menudeo_con_iva"), rs.getBigDecimal("precio_foraneo_con_iva"),
                materiales, operaciones);
    }

    private static MaterialFuente materialDeFicha(String codigoFuente, Map<String, MaterialFuente> materiales) {
        if (materiales.size() == 1) {
            return materiales.values().iterator().next();
        }
        Matcher suffix = MATERIAL_SUFFIX.matcher(codigoFuente == null ? "" : codigoFuente.toUpperCase());
        if (!suffix.find()) {
            return null;
        }
        String materialCodigo = switch (suffix.group(1)) {
            case "N" -> "T";   // Al natural = triplay de pino.
            case "F" -> "F";   // Formaica.
            case "P" -> "PA"; // Panelart.
            case "PO" -> "P"; // Polipropileno.
            default -> null;
        };
        return materiales.get(materialCodigo);
    }

    private record MaterialFuente(Long id, String codigo, String nombre) {
    }

    private static Long nullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }
}
