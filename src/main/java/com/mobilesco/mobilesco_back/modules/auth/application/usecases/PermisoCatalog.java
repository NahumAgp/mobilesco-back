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
            String tipo,
            String vistaRequerida
    ) {}

    private static Definition view(String code, String nombre, String modulo, String vista, String ruta) {
        return new Definition(code, nombre, modulo, vista, "Permite abrir y consultar " + vista + ".", ruta, "VIEW", null);
    }

    private static Definition action(String code, String nombre, String modulo, String vista, String ruta, String vistaRequerida) {
        return new Definition(code, nombre, modulo, vista, nombre + " en " + vista + ".", ruta, "ACTION", vistaRequerida);
    }

    public static final List<Definition> DEFINITIONS = List.of(
            view("VIEW_DASHBOARD", "Ver tablero", "General", "Tablero", "/tablero"),

            view("VIEW_USERS", "Ver usuarios, roles y permisos", "Seguridad", "Usuarios y accesos", "/usuarios/accesos"),
            action("ACTION_USERS_CREATE", "Crear usuarios", "Seguridad", "Usuarios y accesos", "/usuarios/accesos", "VIEW_USERS"),
            action("ACTION_USER_ROLES", "Asignar y quitar roles", "Seguridad", "Usuarios y accesos", "/usuarios/accesos", "VIEW_USERS"),
            action("ACTION_USER_PERMISSIONS", "Administrar permisos directos", "Seguridad", "Usuarios y accesos", "/usuarios/accesos", "VIEW_USERS"),
            action("ACTION_USERS_STATUS", "Activar, bloquear o desactivar usuarios", "Seguridad", "Usuarios y accesos", "/usuarios/accesos", "VIEW_USERS"),
            action("ACTION_ROLES_CREATE", "Crear roles", "Seguridad", "Roles y permisos", "/usuarios/accesos", "VIEW_USERS"),
            action("ACTION_ROLES_PERMISSIONS", "Editar permisos de roles", "Seguridad", "Roles y permisos", "/usuarios/accesos", "VIEW_USERS"),
            action("ACTION_INVITATIONS_CREATE", "Crear invitaciones", "Seguridad", "Invitaciones", "/usuarios/accesos", "VIEW_USERS"),
            action("ACTION_USERS_APPROVE", "Aprobar usuarios", "Seguridad", "Aprobaciones", "/usuarios/accesos", "VIEW_USERS"),

            view("VIEW_EMPLOYEES", "Ver empleados", "Administración", "Empleados", "/empleados"),
            action("ACTION_EMPLOYEES_CREATE", "Crear empleados", "Administración", "Empleados", "/empleados", "VIEW_EMPLOYEES"),
            action("ACTION_EMPLOYEES_EDIT", "Editar empleados", "Administración", "Empleados", "/empleados", "VIEW_EMPLOYEES"),
            action("ACTION_EMPLOYEES_STATUS", "Activar o desactivar empleados", "Administración", "Empleados", "/empleados", "VIEW_EMPLOYEES"),
            action("ACTION_EMPLOYEES_PHOTO", "Administrar fotografías", "Administración", "Empleados", "/empleados", "VIEW_EMPLOYEES"),
            view("VIEW_WORK_AREAS", "Ver áreas de trabajo", "Administración", "Áreas de trabajo", "/areas-trabajo"),
            action("ACTION_WORK_AREAS_CREATE", "Crear áreas de trabajo", "Administración", "Áreas de trabajo", "/areas-trabajo", "VIEW_WORK_AREAS"),
            action("ACTION_WORK_AREAS_EDIT", "Editar áreas de trabajo", "Administración", "Áreas de trabajo", "/areas-trabajo", "VIEW_WORK_AREAS"),
            action("ACTION_WORK_AREAS_STATUS", "Activar o desactivar áreas", "Administración", "Áreas de trabajo", "/areas-trabajo", "VIEW_WORK_AREAS"),

            view("VIEW_SUPPLIERS", "Ver proveedores", "Compras", "Proveedores", "/proveedores"),
            action("ACTION_SUPPLIERS_CREATE", "Crear proveedores", "Compras", "Proveedores", "/proveedores", "VIEW_SUPPLIERS"),
            action("ACTION_SUPPLIERS_EDIT", "Editar proveedores", "Compras", "Proveedores", "/proveedores", "VIEW_SUPPLIERS"),
            action("ACTION_SUPPLIERS_DELETE", "Eliminar proveedores", "Compras", "Proveedores", "/proveedores", "VIEW_SUPPLIERS"),
            action("ACTION_SUPPLIERS_EXPORT", "Exportar proveedores", "Compras", "Proveedores", "/proveedores", "VIEW_SUPPLIERS"),
            view("VIEW_PURCHASES", "Ver compras", "Compras", "Compras", "/compras"),
            action("ACTION_PURCHASES_CREATE", "Crear compras", "Compras", "Compras", "/compras", "VIEW_PURCHASES"),
            action("ACTION_PURCHASES_EDIT", "Editar compras", "Compras", "Compras", "/compras", "VIEW_PURCHASES"),
            action("ACTION_PURCHASES_DELETE", "Eliminar compras", "Compras", "Compras", "/compras", "VIEW_PURCHASES"),
            action("ACTION_PURCHASES_RECEIVE", "Recibir compras", "Compras", "Entradas de almacén", "/almacen/entradas", "VIEW_PURCHASES"),
            view("VIEW_ACCOUNTS_PAYABLE", "Ver cuentas por pagar", "Compras", "Cuentas por pagar", "/compras/cuentas-por-pagar"),
            action("ACTION_ACCOUNTS_PAYABLE_EDIT", "Actualizar cuentas por pagar", "Compras", "Cuentas por pagar", "/compras/cuentas-por-pagar", "VIEW_ACCOUNTS_PAYABLE"),

            view("VIEW_PRODUCTS", "Ver productos", "Productos", "Productos", "/productos"),
            action("ACTION_PRODUCTS_CREATE", "Crear productos", "Productos", "Productos", "/productos", "VIEW_PRODUCTS"),
            action("ACTION_PRODUCTS_EDIT", "Editar productos", "Productos", "Productos", "/productos", "VIEW_PRODUCTS"),
            action("ACTION_PRODUCTS_STATUS", "Activar o desactivar productos", "Productos", "Productos", "/productos", "VIEW_PRODUCTS"),
            action("ACTION_PRODUCTS_DELETE", "Eliminar productos definitivamente", "Productos", "Productos", "/productos", "VIEW_PRODUCTS"),
            action("ACTION_PRODUCTS_EXPORT", "Exportar productos", "Productos", "Productos", "/productos", "VIEW_PRODUCTS"),
            action("ACTION_PRODUCTS_BOM", "Administrar listas de materiales y operaciones", "Productos", "Estructura de producto", "/productos", "VIEW_PRODUCTS"),
            view("VIEW_PRODUCT_CATALOG", "Ver catálogo visual", "Productos", "Catálogo visual", "/productos/catalogo"),
            view("VIEW_PRODUCT_QUALITY", "Ver calidad de datos", "Productos", "Calidad de datos", "/productos/calidad"),
            view("VIEW_PRODUCT_LINES", "Ver líneas de producto", "Productos", "Líneas de producto", "/lineas-producto"),
            action("ACTION_PRODUCT_LINES_CREATE", "Crear líneas de producto", "Productos", "Líneas de producto", "/lineas-producto", "VIEW_PRODUCT_LINES"),
            action("ACTION_PRODUCT_LINES_EDIT", "Editar líneas de producto", "Productos", "Líneas de producto", "/lineas-producto", "VIEW_PRODUCT_LINES"),
            action("ACTION_PRODUCT_LINES_STATUS", "Activar o desactivar líneas", "Productos", "Líneas de producto", "/lineas-producto", "VIEW_PRODUCT_LINES"),
            action("ACTION_PRODUCT_LINES_DELETE", "Eliminar líneas de producto", "Productos", "Líneas de producto", "/lineas-producto", "VIEW_PRODUCT_LINES"),
            action("ACTION_PRODUCT_LINES_EXPORT", "Exportar líneas de producto", "Productos", "Líneas de producto", "/lineas-producto", "VIEW_PRODUCT_LINES"),
            view("VIEW_FAMILIES", "Ver familias", "Productos", "Familias", "/familias"),
            action("ACTION_FAMILIES_CREATE", "Crear familias", "Productos", "Familias", "/familias", "VIEW_FAMILIES"),
            action("ACTION_FAMILIES_EDIT", "Editar familias", "Productos", "Familias", "/familias", "VIEW_FAMILIES"),
            action("ACTION_FAMILIES_STATUS", "Activar o desactivar familias", "Productos", "Familias", "/familias", "VIEW_FAMILIES"),
            action("ACTION_FAMILIES_DELETE", "Eliminar familias", "Productos", "Familias", "/familias", "VIEW_FAMILIES"),
            action("ACTION_FAMILIES_EXPORT", "Exportar familias", "Productos", "Familias", "/familias", "VIEW_FAMILIES"),
            view("VIEW_SUBFAMILIES", "Ver subfamilias", "Productos", "Subfamilias", "/subfamilias"),
            action("ACTION_SUBFAMILIES_CREATE", "Crear subfamilias", "Productos", "Subfamilias", "/subfamilias", "VIEW_SUBFAMILIES"),
            action("ACTION_SUBFAMILIES_EDIT", "Editar subfamilias", "Productos", "Subfamilias", "/subfamilias", "VIEW_SUBFAMILIES"),
            action("ACTION_SUBFAMILIES_STATUS", "Activar o desactivar subfamilias", "Productos", "Subfamilias", "/subfamilias", "VIEW_SUBFAMILIES"),
            action("ACTION_SUBFAMILIES_DELETE", "Eliminar subfamilias", "Productos", "Subfamilias", "/subfamilias", "VIEW_SUBFAMILIES"),
            view("VIEW_MODELS", "Ver modelos", "Productos", "Modelos", "/modelos"),
            action("ACTION_MODELS_CREATE", "Crear modelos y niveles", "Productos", "Modelos", "/modelos", "VIEW_MODELS"),
            action("ACTION_MODELS_EDIT", "Editar modelos y niveles", "Productos", "Modelos", "/modelos", "VIEW_MODELS"),
            action("ACTION_MODELS_STATUS", "Activar o desactivar modelos", "Productos", "Modelos", "/modelos", "VIEW_MODELS"),
            action("ACTION_MODELS_DELETE", "Eliminar modelos y niveles", "Productos", "Modelos", "/modelos", "VIEW_MODELS"),
            action("ACTION_MODELS_EXPORT", "Exportar modelos", "Productos", "Modelos", "/modelos", "VIEW_MODELS"),
            view("VIEW_MATERIALS", "Ver materiales", "Productos", "Materiales", "/materiales"),
            action("ACTION_MATERIALS_CREATE", "Crear materiales", "Productos", "Materiales", "/materiales", "VIEW_MATERIALS"),
            action("ACTION_MATERIALS_EDIT", "Editar materiales", "Productos", "Materiales", "/materiales", "VIEW_MATERIALS"),
            action("ACTION_MATERIALS_STATUS", "Activar o desactivar materiales", "Productos", "Materiales", "/materiales", "VIEW_MATERIALS"),
            action("ACTION_MATERIALS_DELETE", "Eliminar materiales", "Productos", "Materiales", "/materiales", "VIEW_MATERIALS"),
            action("ACTION_MATERIALS_EXPORT", "Exportar materiales", "Productos", "Materiales", "/materiales", "VIEW_MATERIALS"),
            view("VIEW_COLORS", "Ver colores", "Productos", "Colores", "/colores"),
            action("ACTION_COLORS_CREATE", "Crear colores", "Productos", "Colores", "/colores", "VIEW_COLORS"),
            action("ACTION_COLORS_EDIT", "Editar colores", "Productos", "Colores", "/colores", "VIEW_COLORS"),
            action("ACTION_COLORS_STATUS", "Activar o desactivar colores", "Productos", "Colores", "/colores", "VIEW_COLORS"),
            action("ACTION_COLORS_DELETE", "Eliminar colores", "Productos", "Colores", "/colores", "VIEW_COLORS"),

            view("VIEW_INVENTORY", "Ver insumos", "Almacén", "Insumos", "/insumos"),
            action("ACTION_INVENTORY_CREATE", "Crear insumos", "Almacén", "Insumos", "/insumos", "VIEW_INVENTORY"),
            action("ACTION_INVENTORY_EDIT", "Editar insumos", "Almacén", "Insumos", "/insumos", "VIEW_INVENTORY"),
            action("ACTION_INVENTORY_STATUS", "Activar o desactivar insumos", "Almacén", "Insumos", "/insumos", "VIEW_INVENTORY"),
            action("ACTION_INSUMOS_COSTS", "Gestionar costos de insumos", "Almacén", "Costos de insumos", "/insumos/costos", "VIEW_INVENTORY"),
            action("ACTION_STOCK_ADJUSTMENTS", "Ajustar existencias", "Almacén", "Insumos", "/insumos", "VIEW_INVENTORY"),
            view("VIEW_INPUT_TYPES", "Ver tipos de insumo", "Almacén", "Tipos de insumo", "/insumos/tipos"),
            action("ACTION_INPUT_TYPES_CREATE", "Crear tipos de insumo", "Almacén", "Tipos de insumo", "/insumos/tipos", "VIEW_INPUT_TYPES"),
            action("ACTION_INPUT_TYPES_EDIT", "Editar tipos de insumo", "Almacén", "Tipos de insumo", "/insumos/tipos", "VIEW_INPUT_TYPES"),
            action("ACTION_INPUT_TYPES_STATUS", "Activar o desactivar tipos", "Almacén", "Tipos de insumo", "/insumos/tipos", "VIEW_INPUT_TYPES"),
            view("VIEW_MEASURE_UNITS", "Ver unidades de medida", "Almacén", "Unidades de medida", "/unidades-medida"),
            action("ACTION_MEASURE_UNITS_CREATE", "Crear unidades de medida", "Almacén", "Unidades de medida", "/unidades-medida", "VIEW_MEASURE_UNITS"),
            action("ACTION_MEASURE_UNITS_EDIT", "Editar unidades de medida", "Almacén", "Unidades de medida", "/unidades-medida", "VIEW_MEASURE_UNITS"),
            action("ACTION_MEASURE_UNITS_DELETE", "Eliminar unidades de medida", "Almacén", "Unidades de medida", "/unidades-medida", "VIEW_MEASURE_UNITS"),
            action("ACTION_MEASURE_UNITS_EXPORT", "Exportar unidades de medida", "Almacén", "Unidades de medida", "/unidades-medida", "VIEW_MEASURE_UNITS"),
            view("VIEW_INVENTORY_OUTPUTS", "Ver salidas de insumos", "Almacén", "Salidas de insumos", "/salidas-insumos"),
            action("ACTION_INVENTORY_OUTPUTS_CREATE", "Registrar salidas de insumos", "Almacén", "Salidas de insumos", "/salidas-insumos", "VIEW_INVENTORY_OUTPUTS"),
            action("ACTION_INVENTORY_OUTPUTS_DELETE", "Eliminar salidas de insumos", "Almacén", "Salidas de insumos", "/salidas-insumos", "VIEW_INVENTORY_OUTPUTS"),
            view("VIEW_WAREHOUSE_REQUISITIONS", "Ver requisiciones de almacén", "Almacén", "Requisiciones", "/almacen/requisiciones"),
            action("ACTION_WAREHOUSE_REQUISITIONS_CREATE", "Crear requisiciones", "Almacén", "Requisiciones", "/almacen/requisiciones", "VIEW_WAREHOUSE_REQUISITIONS"),
            action("ACTION_WAREHOUSE_REQUISITIONS_RESOLVE", "Resolver requisiciones", "Almacén", "Requisiciones", "/almacen/requisiciones", "VIEW_WAREHOUSE_REQUISITIONS"),
            view("VIEW_WAREHOUSE_RECEIPTS", "Ver entradas de almacén", "Almacén", "Entradas de almacén", "/almacen/entradas"),
            action("ACTION_WAREHOUSE_RECEIPTS_RECEIVE", "Recibir entradas de almacén", "Almacén", "Entradas de almacén", "/almacen/entradas", "VIEW_WAREHOUSE_RECEIPTS"),
            view("VIEW_KARDEX", "Ver Kardex", "Almacén", "Kardex", "/kardex"),

            view("VIEW_WORK_CENTERS", "Ver centros de trabajo", "Producción", "Centros de trabajo", "/centros-trabajo"),
            action("ACTION_WORK_CENTERS_CREATE", "Crear centros de trabajo", "Producción", "Centros de trabajo", "/centros-trabajo", "VIEW_WORK_CENTERS"),
            action("ACTION_WORK_CENTERS_EDIT", "Editar centros de trabajo", "Producción", "Centros de trabajo", "/centros-trabajo", "VIEW_WORK_CENTERS"),
            action("ACTION_WORK_CENTERS_DELETE", "Eliminar centros de trabajo", "Producción", "Centros de trabajo", "/centros-trabajo", "VIEW_WORK_CENTERS"),
            view("VIEW_OPERATIONS", "Ver operaciones", "Producción", "Operaciones", "/operaciones"),
            action("ACTION_OPERATIONS_CREATE", "Crear operaciones", "Producción", "Operaciones", "/operaciones", "VIEW_OPERATIONS"),
            action("ACTION_OPERATIONS_EDIT", "Editar operaciones", "Producción", "Operaciones", "/operaciones", "VIEW_OPERATIONS"),
            action("ACTION_OPERATIONS_DELETE", "Eliminar operaciones", "Producción", "Operaciones", "/operaciones", "VIEW_OPERATIONS"),
            view("VIEW_PRODUCTION_ORDERS", "Ver órdenes de producción", "Producción", "Órdenes de producción", "/ordenes-produccion"),
            action("ACTION_PRODUCTION_ORDERS_CREATE", "Crear y convertir órdenes", "Producción", "Órdenes de producción", "/ordenes-produccion", "VIEW_PRODUCTION_ORDERS"),
            action("ACTION_PRODUCTION_ORDERS_EDIT", "Editar órdenes en borrador", "Producción", "Órdenes de producción", "/ordenes-produccion", "VIEW_PRODUCTION_ORDERS"),
            action("ACTION_PRODUCTION_ORDERS_RELEASE", "Liberar órdenes", "Producción", "Órdenes de producción", "/ordenes-produccion", "VIEW_PRODUCTION_ORDERS"),
            action("ACTION_PRODUCTION_MATERIAL_ISSUE", "Surtir materiales de producción", "Producción", "Órdenes de producción", "/ordenes-produccion", "VIEW_PRODUCTION_ORDERS"),
            action("ACTION_PRODUCTION_PROGRESS", "Registrar operaciones y avances", "Producción", "Órdenes de producción", "/ordenes-produccion", "VIEW_PRODUCTION_ORDERS"),
            action("ACTION_PRODUCTION_ORDERS_CANCEL", "Cancelar órdenes", "Producción", "Órdenes de producción", "/ordenes-produccion", "VIEW_PRODUCTION_ORDERS"),
            view("VIEW_CIF", "Ver CIF", "Costos", "Costos indirectos de fabricación", "/cif"),
            action("ACTION_CIF_CREATE", "Crear conceptos CIF", "Costos", "Costos indirectos de fabricación", "/cif", "VIEW_CIF"),
            action("ACTION_CIF_EDIT", "Editar conceptos y configuración CIF", "Costos", "Costos indirectos de fabricación", "/cif", "VIEW_CIF"),
            action("ACTION_CIF_STATUS", "Activar o desactivar conceptos CIF", "Costos", "Costos indirectos de fabricación", "/cif", "VIEW_CIF"),

            view("VIEW_CUSTOMERS", "Ver clientes", "Ventas", "Clientes", "/clientes"),
            action("ACTION_CUSTOMERS_CREATE", "Crear clientes", "Ventas", "Clientes", "/clientes", "VIEW_CUSTOMERS"),
            action("ACTION_CUSTOMERS_EDIT", "Editar clientes", "Ventas", "Clientes", "/clientes", "VIEW_CUSTOMERS"),
            action("ACTION_CUSTOMERS_DELETE", "Eliminar clientes", "Ventas", "Clientes", "/clientes", "VIEW_CUSTOMERS"),
            view("VIEW_QUOTES", "Ver cotizaciones", "Ventas", "Cotizaciones", "/cotizaciones"),
            action("ACTION_QUOTES_CREATE", "Crear cotizaciones", "Ventas", "Cotizaciones", "/cotizaciones", "VIEW_QUOTES"),
            action("ACTION_QUOTES_EDIT", "Editar cotizaciones", "Ventas", "Cotizaciones", "/cotizaciones", "VIEW_QUOTES")
    );

    public static final Set<String> ALL_CODES = DEFINITIONS.stream()
            .map(Definition::code)
            .collect(java.util.stream.Collectors.toUnmodifiableSet());

    public static final Set<String> FULL_ACCESS_ROLES = Set.of(
            "ADMIN",
            "DIRECTOR_GENERAL",
            "SUBDIRECCION_ADMINISTRATIVA"
    );

    public static final Set<String> BASIC_CODES = Set.of(
            "VIEW_DASHBOARD", "VIEW_EMPLOYEES", "VIEW_PRODUCTS", "VIEW_PRODUCT_CATALOG"
    );

    public static final Map<String, Set<String>> DEFAULT_ROLE_PERMISSIONS = Map.ofEntries(
            Map.entry("ADMIN", ALL_CODES),
            Map.entry("DIRECTOR_GENERAL", ALL_CODES),
            Map.entry("SUBDIRECCION_ADMINISTRATIVA", ALL_CODES),
            Map.entry("ASISTENTE_GERENCIAL", Set.of("VIEW_DASHBOARD", "VIEW_EMPLOYEES", "VIEW_SUPPLIERS", "VIEW_PRODUCTS", "VIEW_PRODUCT_CATALOG", "VIEW_PURCHASES", "VIEW_CUSTOMERS", "VIEW_QUOTES")),
            Map.entry("SUPERVISOR_PRODUCCION", Set.of("VIEW_DASHBOARD", "VIEW_EMPLOYEES", "VIEW_PRODUCTS", "VIEW_PRODUCT_CATALOG", "VIEW_WORK_CENTERS", "VIEW_OPERATIONS", "VIEW_PRODUCTION_ORDERS", "ACTION_PRODUCTION_ORDERS_CREATE", "ACTION_PRODUCTION_ORDERS_EDIT", "ACTION_PRODUCTION_ORDERS_RELEASE", "ACTION_PRODUCTION_PROGRESS", "ACTION_PRODUCTION_ORDERS_CANCEL")),
            Map.entry("JEFE_HERRERIA", Set.of("VIEW_DASHBOARD", "VIEW_PRODUCTS", "VIEW_PRODUCT_CATALOG", "VIEW_WORK_CENTERS", "VIEW_OPERATIONS")),
            Map.entry("JEFE_CARPINTERIA", Set.of("VIEW_DASHBOARD", "VIEW_PRODUCTS", "VIEW_PRODUCT_CATALOG", "VIEW_WORK_CENTERS", "VIEW_OPERATIONS")),
            Map.entry("JEFE_ARMADO", Set.of("VIEW_DASHBOARD", "VIEW_PRODUCTS", "VIEW_PRODUCT_CATALOG", "VIEW_WORK_CENTERS", "VIEW_OPERATIONS")),
            Map.entry("JEFE_ALMACEN", Set.of("VIEW_DASHBOARD", "VIEW_INVENTORY", "VIEW_INPUT_TYPES", "VIEW_MEASURE_UNITS", "VIEW_INVENTORY_OUTPUTS", "VIEW_WAREHOUSE_REQUISITIONS", "VIEW_WAREHOUSE_RECEIPTS", "VIEW_KARDEX", "VIEW_PURCHASES", "VIEW_PRODUCTS", "VIEW_PRODUCT_CATALOG", "ACTION_STOCK_ADJUSTMENTS", "VIEW_PRODUCTION_ORDERS", "ACTION_PRODUCTION_MATERIAL_ISSUE")),
            Map.entry("JEFE_LOGISTICA", Set.of("VIEW_DASHBOARD", "VIEW_INVENTORY", "VIEW_INVENTORY_OUTPUTS", "VIEW_KARDEX", "VIEW_PRODUCTS", "VIEW_PRODUCT_CATALOG")),
            Map.entry("TECNICO", BASIC_CODES),
            Map.entry("AYUDANTE_GENERAL", BASIC_CODES),
            Map.entry("EMPLOYEE", BASIC_CODES)
    );

    /** Legacy broad permissions are expanded once so existing roles keep their capabilities. */
    public static final Map<String, Set<String>> LEGACY_EXPANSIONS = Map.ofEntries(
            Map.entry("VIEW_USERS", Set.of("ACTION_USERS_CREATE", "ACTION_USER_ROLES", "ACTION_USER_PERMISSIONS", "ACTION_USERS_STATUS", "ACTION_ROLES_CREATE", "ACTION_ROLES_PERMISSIONS", "ACTION_INVITATIONS_CREATE", "ACTION_USERS_APPROVE")),
            Map.entry("VIEW_EMPLOYEES", Set.of("ACTION_EMPLOYEES_CREATE", "ACTION_EMPLOYEES_EDIT", "ACTION_EMPLOYEES_STATUS", "ACTION_EMPLOYEES_PHOTO", "VIEW_WORK_AREAS", "ACTION_WORK_AREAS_CREATE", "ACTION_WORK_AREAS_EDIT", "ACTION_WORK_AREAS_STATUS")),
            Map.entry("VIEW_SUPPLIERS", Set.of("ACTION_SUPPLIERS_CREATE", "ACTION_SUPPLIERS_EDIT", "ACTION_SUPPLIERS_DELETE", "ACTION_SUPPLIERS_EXPORT")),
            Map.entry("VIEW_PRODUCTS", Set.of("ACTION_PRODUCTS_CREATE", "ACTION_PRODUCTS_EDIT", "ACTION_PRODUCTS_STATUS", "ACTION_PRODUCTS_DELETE", "ACTION_PRODUCTS_EXPORT", "ACTION_PRODUCTS_BOM", "VIEW_PRODUCT_QUALITY", "VIEW_PRODUCT_LINES", "ACTION_PRODUCT_LINES_CREATE", "ACTION_PRODUCT_LINES_EDIT", "ACTION_PRODUCT_LINES_STATUS", "ACTION_PRODUCT_LINES_DELETE", "ACTION_PRODUCT_LINES_EXPORT", "VIEW_FAMILIES", "ACTION_FAMILIES_CREATE", "ACTION_FAMILIES_EDIT", "ACTION_FAMILIES_STATUS", "ACTION_FAMILIES_DELETE", "ACTION_FAMILIES_EXPORT", "VIEW_SUBFAMILIES", "ACTION_SUBFAMILIES_CREATE", "ACTION_SUBFAMILIES_EDIT", "ACTION_SUBFAMILIES_STATUS", "ACTION_SUBFAMILIES_DELETE", "VIEW_MODELS", "ACTION_MODELS_CREATE", "ACTION_MODELS_EDIT", "ACTION_MODELS_STATUS", "ACTION_MODELS_DELETE", "ACTION_MODELS_EXPORT", "VIEW_MATERIALS", "ACTION_MATERIALS_CREATE", "ACTION_MATERIALS_EDIT", "ACTION_MATERIALS_STATUS", "ACTION_MATERIALS_DELETE", "ACTION_MATERIALS_EXPORT", "VIEW_COLORS", "ACTION_COLORS_CREATE", "ACTION_COLORS_EDIT", "ACTION_COLORS_STATUS", "ACTION_COLORS_DELETE")),
            Map.entry("VIEW_INVENTORY", Set.of("ACTION_INVENTORY_CREATE", "ACTION_INVENTORY_EDIT", "ACTION_INVENTORY_STATUS", "VIEW_INPUT_TYPES", "ACTION_INPUT_TYPES_CREATE", "ACTION_INPUT_TYPES_EDIT", "ACTION_INPUT_TYPES_STATUS", "VIEW_MEASURE_UNITS", "ACTION_MEASURE_UNITS_CREATE", "ACTION_MEASURE_UNITS_EDIT", "ACTION_MEASURE_UNITS_DELETE", "ACTION_MEASURE_UNITS_EXPORT", "VIEW_INVENTORY_OUTPUTS", "ACTION_INVENTORY_OUTPUTS_CREATE", "ACTION_INVENTORY_OUTPUTS_DELETE")),
            Map.entry("VIEW_WAREHOUSE_REQUISITIONS", Set.of("ACTION_WAREHOUSE_REQUISITIONS_CREATE", "ACTION_WAREHOUSE_REQUISITIONS_RESOLVE")),
            Map.entry("VIEW_WORK_CENTERS", Set.of("ACTION_WORK_CENTERS_CREATE", "ACTION_WORK_CENTERS_EDIT", "ACTION_WORK_CENTERS_DELETE")),
            Map.entry("VIEW_OPERATIONS", Set.of("ACTION_OPERATIONS_CREATE", "ACTION_OPERATIONS_EDIT", "ACTION_OPERATIONS_DELETE")),
            Map.entry("VIEW_CIF", Set.of("ACTION_CIF_CREATE", "ACTION_CIF_EDIT", "ACTION_CIF_STATUS")),
            Map.entry("VIEW_PURCHASES", Set.of("ACTION_PURCHASES_CREATE", "ACTION_PURCHASES_EDIT", "ACTION_PURCHASES_DELETE", "ACTION_PURCHASES_RECEIVE", "VIEW_ACCOUNTS_PAYABLE", "ACTION_ACCOUNTS_PAYABLE_EDIT", "VIEW_WAREHOUSE_RECEIPTS", "ACTION_WAREHOUSE_RECEIPTS_RECEIVE")),
            Map.entry("VIEW_CUSTOMERS", Set.of("ACTION_CUSTOMERS_CREATE", "ACTION_CUSTOMERS_EDIT", "ACTION_CUSTOMERS_DELETE")),
            Map.entry("VIEW_QUOTES", Set.of("ACTION_QUOTES_CREATE", "ACTION_QUOTES_EDIT"))
    );

    public static String requiredView(String permissionCode) {
        return DEFINITIONS.stream()
                .filter(definition -> definition.code().equals(permissionCode))
                .findFirst()
                .map(Definition::vistaRequerida)
                .orElse(null);
    }
}
