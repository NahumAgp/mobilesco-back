package com.mobilesco.mobilesco_back.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class PermissionEnforcementFilter extends OncePerRequestFilter {

    private record Rule(String prefix, String view, String create, String edit, String delete) {}

    private static final List<Rule> RULES = List.of(
            new Rule("/api/v1/cuentas-por-pagar", "VIEW_ACCOUNTS_PAYABLE", null, "ACTION_ACCOUNTS_PAYABLE_EDIT", null),
            new Rule("/api/v1/detalles-compra", "VIEW_PURCHASES", "ACTION_PURCHASES_EDIT", "ACTION_PURCHASES_RECEIVE", "ACTION_PURCHASES_EDIT"),
            new Rule("/api/v1/compras", "VIEW_PURCHASES", "ACTION_PURCHASES_CREATE", "ACTION_PURCHASES_EDIT", "ACTION_PURCHASES_DELETE"),
            new Rule("/api/v1/requisiciones-almacen", "VIEW_WAREHOUSE_REQUISITIONS", "ACTION_WAREHOUSE_REQUISITIONS_CREATE", "ACTION_WAREHOUSE_REQUISITIONS_RESOLVE", null),
            new Rule("/api/v1/salidas-insumos", "VIEW_INVENTORY_OUTPUTS", "ACTION_INVENTORY_OUTPUTS_CREATE", null, "ACTION_INVENTORY_OUTPUTS_DELETE"),
            new Rule("/api/v1/tipos-insumo", "VIEW_INPUT_TYPES", "ACTION_INPUT_TYPES_CREATE", "ACTION_INPUT_TYPES_EDIT", "ACTION_INPUT_TYPES_STATUS"),
            new Rule("/api/v1/unidades-medida", "VIEW_MEASURE_UNITS", "ACTION_MEASURE_UNITS_CREATE", "ACTION_MEASURE_UNITS_EDIT", "ACTION_MEASURE_UNITS_DELETE"),
            new Rule("/api/v1/insumos", "VIEW_INVENTORY", "ACTION_INVENTORY_CREATE", "ACTION_INVENTORY_EDIT", "ACTION_INVENTORY_STATUS"),
            new Rule("/api/v1/kardex", "VIEW_KARDEX", null, null, null),
            new Rule("/api/v1/areas-trabajo", "VIEW_WORK_AREAS", "ACTION_WORK_AREAS_CREATE", "ACTION_WORK_AREAS_EDIT", "ACTION_WORK_AREAS_STATUS"),
            new Rule("/api/v1/empleados", "VIEW_EMPLOYEES", "ACTION_EMPLOYEES_CREATE", "ACTION_EMPLOYEES_EDIT", "ACTION_EMPLOYEES_STATUS"),
            new Rule("/api/v1/proveedores", "VIEW_SUPPLIERS", "ACTION_SUPPLIERS_CREATE", "ACTION_SUPPLIERS_EDIT", "ACTION_SUPPLIERS_DELETE"),
            new Rule("/api/v1/lineas", "VIEW_PRODUCT_LINES", "ACTION_PRODUCT_LINES_CREATE", "ACTION_PRODUCT_LINES_EDIT", "ACTION_PRODUCT_LINES_DELETE"),
            new Rule("/lineas-Producto", "VIEW_PRODUCT_LINES", "ACTION_PRODUCT_LINES_CREATE", "ACTION_PRODUCT_LINES_EDIT", "ACTION_PRODUCT_LINES_DELETE"),
            new Rule("/api/v1/familias", "VIEW_FAMILIES", "ACTION_FAMILIES_CREATE", "ACTION_FAMILIES_EDIT", "ACTION_FAMILIES_DELETE"),
            new Rule("/api/v1/subfamilias", "VIEW_SUBFAMILIES", "ACTION_SUBFAMILIES_CREATE", "ACTION_SUBFAMILIES_EDIT", "ACTION_SUBFAMILIES_DELETE"),
            new Rule("/api/v1/modelos", "VIEW_MODELS", "ACTION_MODELS_CREATE", "ACTION_MODELS_EDIT", "ACTION_MODELS_DELETE"),
            new Rule("/api/v1/niveles", "VIEW_MODELS", "ACTION_MODELS_CREATE", "ACTION_MODELS_EDIT", "ACTION_MODELS_DELETE"),
            new Rule("/api/v1/categorias", "VIEW_MODELS", "ACTION_MODELS_CREATE", "ACTION_MODELS_EDIT", "ACTION_MODELS_DELETE"),
            new Rule("/api/v1/materiales", "VIEW_MATERIALS", "ACTION_MATERIALS_CREATE", "ACTION_MATERIALS_EDIT", "ACTION_MATERIALS_DELETE"),
            new Rule("/api/v1/colores", "VIEW_COLORS", "ACTION_COLORS_CREATE", "ACTION_COLORS_EDIT", "ACTION_COLORS_DELETE"),
            new Rule("/api/v1/imagenes", "VIEW_PRODUCTS", "ACTION_PRODUCTS_EDIT", "ACTION_PRODUCTS_EDIT", "ACTION_PRODUCTS_EDIT"),
            new Rule("/api/v1/productos", "VIEW_PRODUCTS", "ACTION_PRODUCTS_CREATE", "ACTION_PRODUCTS_EDIT", "ACTION_PRODUCTS_DELETE"),
            new Rule("/api/v1/centros-trabajo", "VIEW_WORK_CENTERS", "ACTION_WORK_CENTERS_CREATE", "ACTION_WORK_CENTERS_EDIT", "ACTION_WORK_CENTERS_DELETE"),
            new Rule("/api/v1/operaciones", "VIEW_OPERATIONS", "ACTION_OPERATIONS_CREATE", "ACTION_OPERATIONS_EDIT", "ACTION_OPERATIONS_DELETE"),
            new Rule("/api/v1/cif", "VIEW_CIF", "ACTION_CIF_CREATE", "ACTION_CIF_EDIT", "ACTION_CIF_STATUS"),
            new Rule("/api/v1/clientes", "VIEW_CUSTOMERS", "ACTION_CUSTOMERS_CREATE", "ACTION_CUSTOMERS_EDIT", "ACTION_CUSTOMERS_DELETE"),
            new Rule("/api/v1/cotizaciones", "VIEW_QUOTES", "ACTION_QUOTES_CREATE", "ACTION_QUOTES_EDIT", "ACTION_QUOTES_EDIT"),
            new Rule("/api/v1/tablero", "VIEW_DASHBOARD", null, null, null)
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || path.startsWith("/api/v1/public/")
                || path.startsWith("/api/v1/auth/")
                || path.startsWith("/api/v1/notificaciones")
                || path.startsWith("/uploads/")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs/")
                || path.startsWith("/api/v1/empleados/me/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Rule rule = RULES.stream()
                .filter(candidate -> request.getRequestURI().startsWith(candidate.prefix()))
                .findFirst()
                .orElse(null);
        if (rule == null) {
            chain.doFilter(request, response);
            return;
        }

        String action = resolveAction(request, rule);
        if (!hasAuthority(rule.view()) || (action != null && !hasAuthority(action))) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"message\":\"No tienes permisos para realizar esta operación.\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    private String resolveAction(HttpServletRequest request, Rule rule) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        if (path.matches(".*/productos/[^/]+/(insumos|operaciones)(/.*)?$")) return "ACTION_PRODUCTS_BOM";
        if (path.endsWith("/ajustar-stock")) return "ACTION_STOCK_ADJUSTMENTS";
        if (path.endsWith("/costo-cotizacion") || path.endsWith("/costos")) return "ACTION_INSUMOS_COSTS";
        if (path.matches(".*/empleados/[^/]+/foto$")) return "ACTION_EMPLOYEES_PHOTO";
        if (path.endsWith("/reporte/excel")) return exportPermission(rule.view());
        if ((path.endsWith("/activar") || path.endsWith("/desactivar") || path.endsWith("/estado"))) {
            String statusPermission = statusPermission(rule.view());
            if (statusPermission != null) return statusPermission;
        }
        if (HttpMethod.POST.matches(method)) return rule.create();
        if (HttpMethod.PUT.matches(method) || HttpMethod.PATCH.matches(method)) return rule.edit();
        if (HttpMethod.DELETE.matches(method)) return rule.delete();
        return null;
    }

    private String exportPermission(String view) {
        return switch (view) {
            case "VIEW_SUPPLIERS" -> "ACTION_SUPPLIERS_EXPORT";
            case "VIEW_PRODUCTS" -> "ACTION_PRODUCTS_EXPORT";
            case "VIEW_PRODUCT_LINES" -> "ACTION_PRODUCT_LINES_EXPORT";
            case "VIEW_FAMILIES" -> "ACTION_FAMILIES_EXPORT";
            case "VIEW_MODELS" -> "ACTION_MODELS_EXPORT";
            case "VIEW_MATERIALS" -> "ACTION_MATERIALS_EXPORT";
            case "VIEW_MEASURE_UNITS" -> "ACTION_MEASURE_UNITS_EXPORT";
            default -> null;
        };
    }

    private String statusPermission(String view) {
        return switch (view) {
            case "VIEW_PRODUCT_LINES" -> "ACTION_PRODUCT_LINES_STATUS";
            case "VIEW_FAMILIES" -> "ACTION_FAMILIES_STATUS";
            case "VIEW_SUBFAMILIES" -> "ACTION_SUBFAMILIES_STATUS";
            case "VIEW_MODELS" -> "ACTION_MODELS_STATUS";
            case "VIEW_MATERIALS" -> "ACTION_MATERIALS_STATUS";
            case "VIEW_COLORS" -> "ACTION_COLORS_STATUS";
            case "VIEW_INPUT_TYPES" -> "ACTION_INPUT_TYPES_STATUS";
            case "VIEW_CIF" -> "ACTION_CIF_STATUS";
            default -> null;
        };
    }

    private boolean hasAuthority(String required) {
        if (required == null) return true;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return false;
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(required::equals);
    }
}
