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
    public static final String EMPLEADOS = API_VERSION + "/empleados"; 
    public static final String PROVEEDORES = API_VERSION + "/proveedores";
    public static final String UNIDADES_MEDIDA = API_VERSION + "/unidades-medida";

    //  PRODUCTOS
    public static final String LINEAS = API_VERSION + "/lineas"; 
    public static final String FAMILIAS = API_VERSION + "/familias"; //PENDIENTE: Ver si se queda o se integra en LineaProducto|
    
    public static final String CATEGORIA = API_VERSION + "/categorias";//pendiente: Ver si se queda o se integra en LineaProducto|
    public static final String MATERIALES = API_VERSION + "/materiales";//Quitar
    public static final String TIPO_PRODUCTO = API_VERSION + "/tipos-producto";//quitar

    public static final String INSUMOS = API_VERSION + "/insumos";

    public static final String CENTRO_TRABAJO = API_VERSION + "/centros-trabajo";

    public static final String OPERACION = API_VERSION + "/operaciones";
    public static final String PRODUCTO_OPERACION = API_VERSION + "producto-operaciones";

    public static final String COMPRAS = API_VERSION + "/compras";
    
    public static final String PRODUCTOS = API_VERSION + "/productos";
    public static final String CLIENTES = API_VERSION + "/clientes";
    public static final String COTIZACIONES = API_VERSION + "/cotizaciones";

}
