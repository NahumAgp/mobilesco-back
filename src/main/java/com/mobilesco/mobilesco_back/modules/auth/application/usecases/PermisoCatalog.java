package com.mobilesco.mobilesco_back.modules.auth.application.usecases;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PermisoCatalog {

    private PermisoCatalog() {}

    public record Definition(
            String code,
            String nombre,
            String modulo,
            String vista,
            String descripcion,
            String ruta,
            String tipo
    ) {}

    public static final List<Definition> DEFINITIONS = List.of(
            new Definition("VIEW_DASHBOARD", "Ver tablero", "General", "Tablero", "Acceso al tablero principal.", "/tablero", "VIEW"),
            new Definition("VIEW_USERS", "Administrar usuarios y accesos", "Seguridad", "Usuarios y accesos", "Gestionar usuarios, roles, permisos e invitaciones.", "/usuarios/accesos", "VIEW"),
            new Definition("ACTION_USERS_WRITE", "Modificar usuarios y roles", "Seguridad", "Usuarios y accesos", "Crear, editar, desactivar usuarios y administrar permisos.", "/usuarios/accesos", "ACTION"),
            new Definition("VIEW_EMPLOYEES", "Ver empleados", "Administracion", "Empleados", "Consultar el modulo de empleados.", "/empleados", "VIEW"),
            new Definition("VIEW_SUPPLIERS", "Ver proveedores", "Compras", "Proveedores", "Consultar proveedores.", "/proveedores", "VIEW"),
            new Definition("VIEW_PRODUCTS", "Ver productos", "Productos", "Productos", "Consultar catalogos de producto.", "/productos", "VIEW"),
            new Definition("VIEW_PRODUCT_CATALOG", "Ver catalogo visual", "Productos", "Catalogo visual", "Consultar el catalogo visual de productos.", "/productos/catalogo", "VIEW"),
            new Definition("VIEW_INVENTORY", "Ver almacen", "Almacen", "Almacen", "Consultar insumos, entradas, salidas y unidades.", "/insumos", "VIEW"),
            new Definition("VIEW_WORK_CENTERS", "Ver centros de trabajo", "Produccion", "Centros de trabajo", "Consultar centros de trabajo.", "/centros-trabajo", "VIEW"),
            new Definition("VIEW_OPERATIONS", "Ver operaciones", "Produccion", "Operaciones", "Consultar operaciones.", "/operaciones", "VIEW"),
            new Definition("VIEW_CIF", "Ver CIF", "Costos", "CIF", "Consultar costos indirectos de fabricacion.", "/cif", "VIEW"),
            new Definition("VIEW_PURCHASES", "Ver compras", "Compras", "Compras", "Consultar compras.", "/compras", "VIEW"),
            new Definition("VIEW_KARDEX", "Ver kardex", "Almacen", "Kardex", "Consultar movimientos de inventario.", "/kardex", "VIEW"),
            new Definition("VIEW_CUSTOMERS", "Ver clientes", "Ventas", "Clientes", "Consultar y administrar el catálogo comercial de clientes.", "/clientes", "VIEW"),
            new Definition("VIEW_QUOTES", "Ver cotizaciones", "Ventas", "Cotizaciones", "Consultar cotizaciones.", "/cotizaciones", "VIEW"),
            new Definition("ACTION_INSUMOS_COSTS", "Gestionar costos de insumos", "Almacen", "Insumos", "Editar costos de insumos.", "/insumos/costos", "ACTION")
    );

    public static final Set<String> ALL_CODES = DEFINITIONS.stream()
            .map(Definition::code)
            .collect(java.util.stream.Collectors.toUnmodifiableSet());

    public static final Set<String> BASIC_CODES = Set.of(
            "VIEW_DASHBOARD",
            "VIEW_EMPLOYEES",
            "VIEW_PRODUCTS",
            "VIEW_PRODUCT_CATALOG"
    );

    public static final Map<String, Set<String>> DEFAULT_ROLE_PERMISSIONS = Map.ofEntries(
            Map.entry("ADMIN", ALL_CODES),
            Map.entry("SUPER_ADMIN", ALL_CODES),
            Map.entry("DIRECTOR_GENERAL", ALL_CODES),
            Map.entry("SUBDIRECCION_ADMINISTRATIVA", ALL_CODES),
            Map.entry("ASISTENTE_GERENCIAL", Set.of("VIEW_DASHBOARD", "VIEW_EMPLOYEES", "VIEW_SUPPLIERS", "VIEW_PRODUCTS", "VIEW_PRODUCT_CATALOG", "VIEW_PURCHASES", "VIEW_CUSTOMERS", "VIEW_QUOTES")),
            Map.entry("SUPERVISOR_PRODUCCION", Set.of("VIEW_DASHBOARD", "VIEW_EMPLOYEES", "VIEW_PRODUCTS", "VIEW_PRODUCT_CATALOG", "VIEW_WORK_CENTERS", "VIEW_OPERATIONS")),
            Map.entry("JEFE_HERRERIA", Set.of("VIEW_DASHBOARD", "VIEW_PRODUCTS", "VIEW_PRODUCT_CATALOG", "VIEW_WORK_CENTERS", "VIEW_OPERATIONS")),
            Map.entry("JEFE_CARPINTERIA", Set.of("VIEW_DASHBOARD", "VIEW_PRODUCTS", "VIEW_PRODUCT_CATALOG", "VIEW_WORK_CENTERS", "VIEW_OPERATIONS")),
            Map.entry("JEFE_ARMADO", Set.of("VIEW_DASHBOARD", "VIEW_PRODUCTS", "VIEW_PRODUCT_CATALOG", "VIEW_WORK_CENTERS", "VIEW_OPERATIONS")),
            Map.entry("JEFE_ALMACEN", Set.of("VIEW_DASHBOARD", "VIEW_INVENTORY", "VIEW_KARDEX", "VIEW_PURCHASES", "VIEW_PRODUCTS", "VIEW_PRODUCT_CATALOG")),
            Map.entry("JEFE_LOGISTICA", Set.of("VIEW_DASHBOARD", "VIEW_INVENTORY", "VIEW_KARDEX", "VIEW_PRODUCTS", "VIEW_PRODUCT_CATALOG")),
            Map.entry("TECNICO", BASIC_CODES),
            Map.entry("AYUDANTE_GENERAL", BASIC_CODES),
            Map.entry("EMPLOYEE", BASIC_CODES)
    );
}
