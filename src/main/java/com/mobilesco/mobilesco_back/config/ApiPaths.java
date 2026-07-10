package com.mobilesco.mobilesco_back.config;

public final class ApiPaths {

    private ApiPaths() {
        // Evita instanciación
    }

    // =============================
    // 🔹 VERSION
    // =============================
    public static final String API_VERSION = "/api/v1";

    // =============================
    // 🔹 DOMINIOS
    // =============================
    public static final String AUTH = API_VERSION + "/auth";
    public static final String AUTH_REGISTER = AUTH + "/register";
    public static final String AUTH_ROLES = AUTH + "/roles";
    public static final String AUTH_INVITACIONES = AUTH + "/invitaciones";
    public static final String AUTH_PENDIENTES = AUTH + "/usuarios-pendientes";
    public static final String EMPLEADOS = API_VERSION + "/empleados"; 
    public static final String AREAS_TRABAJO = API_VERSION + "/areas-trabajo";
    public static final String PROVEEDORES = API_VERSION + "/proveedores";
    public static final String UNIDADES_MEDIDA = API_VERSION + "/unidades-medida";
    public static final String TIPOS_INSUMO = API_VERSION + "/tipos-insumo";

    //  PRODUCTOS
    public static final String LINEAS = API_VERSION + "/lineas"; 
    public static final String FAMILIAS = API_VERSION + "/familias";
    public static final String MODELOS = API_VERSION + "/modelos";
    public static final String NIVELES = API_VERSION + "/niveles";
    public static final String COLORES = API_VERSION + "/colores";
    public static final String MATERIALES = API_VERSION + "/materiales";
    public static final String IMAGENES = API_VERSION + "/imagenes";
    
    public static final String CATEGORIA = API_VERSION + "/categorias";//pendiente: Ver si se queda o se integra en LineaProducto|
    public static final String INSUMOS = API_VERSION + "/insumos";

    public static final String CENTRO_TRABAJO = API_VERSION + "/centros-trabajo";

    public static final String OPERACION = API_VERSION + "/operaciones";
    public static final String CIF = API_VERSION + "/cif";
    public static final String PRODUCTO_OPERACION = API_VERSION + "/producto-operaciones";

    public static final String COMPRAS = API_VERSION + "/compras";
    public static final String CUENTAS_POR_PAGAR = API_VERSION + "/cuentas-por-pagar";
    public static final String DETALLES_COMPRA = API_VERSION + "/detalles-compra";
    public static final String SALIDAS_INSUMOS = API_VERSION + "/salidas-insumos";
    public static final String KARDEX = API_VERSION + "/kardex";
    
    public static final String PRODUCTOS = API_VERSION + "/productos";
    public static final String CLIENTES = API_VERSION + "/clientes";
    public static final String COTIZACIONES = API_VERSION + "/cotizaciones";

    // =============================
    // 🔹 CATALOGO PUBLICO (sin autenticacion, solo lectura)
    // =============================
    public static final String PUBLIC = API_VERSION + "/public";
    public static final String PUBLIC_CATALOGO = PUBLIC + "/catalogo";

}
