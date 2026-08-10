/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/producto/infrastructure/in/api/controllers/ProductoController.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: ProductoController
 * CONTEXTO: Controlador REST principal del modulo Producto.
 * NOTAS: Expone catalogo, filtros, costos y exportacion.
 */
package com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.controllers;

import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.modules.color.domain.models.ColorModel;
import com.mobilesco.mobilesco_back.modules.familia.domain.models.FamiliaModel;
import com.mobilesco.mobilesco_back.modules.linea.domain.models.LineaModel;
import com.mobilesco.mobilesco_back.modules.modelo.domain.models.ModeloModel;
import com.mobilesco.mobilesco_back.modules.nivel.domain.models.NivelModel;
import com.mobilesco.mobilesco_back.modules.producto.application.usecases.ProductoService;
import com.mobilesco.mobilesco_back.modules.producto.application.usecases.ProductoCreacionCompletaService;
import com.mobilesco.mobilesco_back.modules.producto.application.usecases.ProductoReclasificacionService;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoModel;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoCreateDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoCreacionCompletaDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoCreacionCompletaResponseDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoEstructuraCostosDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoFichaDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoResponseDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoUpdateDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoReclasificacionResponseDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoReclasificacionRequestDTO;
import com.mobilesco.mobilesco_back.modules.shared.infrastructure.sort.TypeSafeSorts;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Productos", description = "Catalogo visible de productos")
@RestController
@RequestMapping(ApiPaths.PRODUCTOS)
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    private final ProductoCreacionCompletaService productoCreacionCompletaService;
    private final ProductoReclasificacionService productoReclasificacionService;

    @Operation(summary = "Crear producto")
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crear(@Valid @RequestBody ProductoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(dto));
    }

    @Operation(summary = "Crear producto y catalogos relacionados en una sola transaccion")
    @PostMapping("/creacion-completa")
    public ResponseEntity<ProductoCreacionCompletaResponseDTO> crearCompleto(
            @Valid @RequestBody ProductoCreacionCompletaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoCreacionCompletaService.crear(dto));
    }

    @Operation(summary = "Listar productos con imagenes")
    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) Long modeloId,
            @RequestParam(required = false) Long nivelId,
            @RequestParam(required = false) Long colorId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "sku") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String direction) {
        if (page != null) {
            int pageNumber = Math.max(page, 0);
            int pageSize = size == null ? 10 : Math.max(1, Math.min(size, 100));
            PageRequest pageable = PageRequest.of(pageNumber, pageSize, construirSortProductos(sortBy, direction));
            return ResponseEntity.ok(productoService.obtenerTodosCompletosPaginado(
                    activo,
                    busqueda,
                    modeloId,
                    nivelId,
                    colorId,
                    pageable));
        }
        return ResponseEntity.ok(productoService.obtenerTodosCompletos());
    }

    @Operation(summary = "Exportar reporte de productos a Excel")
    @GetMapping("/reporte/excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String direction) {
        byte[] excel = productoService.generarReporteExcel(activo, busqueda, sortBy, direction);
        String filename = "productos.xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @Operation(summary = "Obtener producto por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerProductoCompleto(id));
    }

    @Operation(summary = "Obtener producto por SKU")
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductoResponseDTO> obtenerPorSku(@PathVariable String sku) {
        return ResponseEntity.ok(productoService.obtenerProductoCompletoPorSku(sku));
    }

    @Operation(summary = "Obtener estructura de costos del producto")
    @GetMapping("/{id}/estructura-costos")
    public ResponseEntity<ProductoEstructuraCostosDTO> obtenerEstructuraCostos(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerEstructuraCostos(id));
    }

    @Operation(summary = "Listar productos por modelo")
    @GetMapping("/por-modelo/{modeloId}")
    public ResponseEntity<List<ProductoResponseDTO>> obtenerPorModelo(@PathVariable Long modeloId) {
        return ResponseEntity.ok(productoService.obtenerCompletosPorModelo(modeloId));
    }

    @Operation(summary = "Obtener ficha del mueble (modelo) con todas sus variantes")
    @GetMapping("/modelo/{modeloId}/ficha")
    public ResponseEntity<ProductoFichaDTO> obtenerFichaPorModelo(@PathVariable Long modeloId) {
        return ResponseEntity.ok(productoService.obtenerFichaPorModelo(modeloId));
    }

    @Operation(summary = "Buscar productos con filtros")
    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoResponseDTO>> buscar(
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Long modeloId,
            @RequestParam(required = false) Long nivelId,
            @RequestParam(required = false) Long colorId) {
        return ResponseEntity.ok(productoService.buscarCompletasConFiltros(sku, nombre, modeloId, nivelId, colorId));
    }

    @Operation(summary = "Actualizar producto")
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoUpdateDTO dto) {
        return ResponseEntity.ok(productoService.actualizar(id, dto));
    }

    @Operation(summary = "Previsualizar el cambio de línea, familia o subfamilia del modelo")
    @PostMapping("/{id}/reclasificacion/preview")
    public ResponseEntity<ProductoReclasificacionResponseDTO> previsualizarReclasificacion(
            @PathVariable Long id,
            @Valid @RequestBody ProductoReclasificacionRequestDTO request) {
        return ResponseEntity.ok(productoReclasificacionService.previsualizar(id, request));
    }

    @Operation(summary = "Aplicar el cambio de línea, familia o subfamilia y recalcular los SKUs")
    @PutMapping("/{id}/reclasificacion")
    public ResponseEntity<ProductoReclasificacionResponseDTO> aplicarReclasificacion(
            @PathVariable Long id,
            @Valid @RequestBody ProductoReclasificacionRequestDTO request) {
        return ResponseEntity.ok(productoReclasificacionService.aplicar(id, request));
    }

    @Operation(summary = "Desactivar producto")
    @PreAuthorize("hasAuthority('VIEW_PRODUCTS') and hasAuthority('ACTION_PRODUCTS_STATUS')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Activar producto")
    @PreAuthorize("hasAuthority('VIEW_PRODUCTS') and hasAuthority('ACTION_PRODUCTS_STATUS')")
    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        productoService.activar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Eliminar producto definitivamente")
    @PreAuthorize("hasAuthority('VIEW_PRODUCTS') and hasAuthority('ACTION_PRODUCTS_DELETE')")
    @DeleteMapping("/{id}/definitivo")
    public ResponseEntity<Void> eliminarDefinitivo(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    private Sort construirSortProductos(String sortBy, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String campo = sortBy == null ? "sku" : sortBy.trim().toLowerCase(Locale.ROOT);

        return switch (campo) {
            case "nombre" -> sortDirection == Sort.Direction.DESC
                    ? TypeSafeSorts.descWithId(ProductoModel.class, ProductoModel::getNombre, ProductoModel::getId)
                    : TypeSafeSorts.ascWithId(ProductoModel.class, ProductoModel::getNombre, ProductoModel::getId);
            case "modelonombre" -> sortDirection == Sort.Direction.DESC
                    ? TypeSafeSorts.descNestedWithId(ProductoModel.class, ProductoModel::getModelo, ModeloModel::getNombre, ProductoModel::getId)
                    : TypeSafeSorts.ascNestedWithId(ProductoModel.class, ProductoModel::getModelo, ModeloModel::getNombre, ProductoModel::getId);
            case "familianombre" -> sortDirection == Sort.Direction.DESC
                    ? TypeSafeSorts.descNestedWithId(ProductoModel.class, ProductoModel::getModelo, ModeloModel::getFamilia, FamiliaModel::getNombre, ProductoModel::getId)
                    : TypeSafeSorts.ascNestedWithId(ProductoModel.class, ProductoModel::getModelo, ModeloModel::getFamilia, FamiliaModel::getNombre, ProductoModel::getId);
            case "lineanombre" -> sortDirection == Sort.Direction.DESC
                    ? TypeSafeSorts.descNestedWithId(
                            ProductoModel.class,
                            ProductoModel::getModelo,
                            ModeloModel::getFamilia,
                            FamiliaModel::getLinea,
                            LineaModel::getNombre,
                            ProductoModel::getId)
                    : TypeSafeSorts.ascNestedWithId(
                            ProductoModel.class,
                            ProductoModel::getModelo,
                            ModeloModel::getFamilia,
                            FamiliaModel::getLinea,
                            LineaModel::getNombre,
                            ProductoModel::getId);
            case "nivelnombre", "categorianombre" -> sortDirection == Sort.Direction.DESC
                    ? TypeSafeSorts.descNestedWithId(ProductoModel.class, ProductoModel::getNivel, NivelModel::getNombre, ProductoModel::getId)
                    : TypeSafeSorts.ascNestedWithId(ProductoModel.class, ProductoModel::getNivel, NivelModel::getNombre, ProductoModel::getId);
            case "colornombre" -> sortDirection == Sort.Direction.DESC
                    ? TypeSafeSorts.descNestedWithId(ProductoModel.class, ProductoModel::getColor, ColorModel::getNombre, ProductoModel::getId)
                    : TypeSafeSorts.ascNestedWithId(ProductoModel.class, ProductoModel::getColor, ColorModel::getNombre, ProductoModel::getId);
            case "activo" -> sortDirection == Sort.Direction.DESC
                    ? TypeSafeSorts.descWithId(ProductoModel.class, ProductoModel::getActivo, ProductoModel::getId)
                    : TypeSafeSorts.ascWithId(ProductoModel.class, ProductoModel::getActivo, ProductoModel::getId);
            case "createdat", "created_at" -> sortDirection == Sort.Direction.DESC
                    ? TypeSafeSorts.descWithId(ProductoModel.class, ProductoModel::getCreatedAt, ProductoModel::getId)
                    : TypeSafeSorts.ascWithId(ProductoModel.class, ProductoModel::getCreatedAt, ProductoModel::getId);
            case "updatedat", "updated_at" -> sortDirection == Sort.Direction.DESC
                    ? TypeSafeSorts.descWithId(ProductoModel.class, ProductoModel::getUpdatedAt, ProductoModel::getId)
                    : TypeSafeSorts.ascWithId(ProductoModel.class, ProductoModel::getUpdatedAt, ProductoModel::getId);
            default -> sortDirection == Sort.Direction.DESC
                    ? TypeSafeSorts.descWithId(ProductoModel.class, ProductoModel::getSku, ProductoModel::getId)
                    : TypeSafeSorts.ascWithId(ProductoModel.class, ProductoModel::getSku, ProductoModel::getId);
        };
    }
}
