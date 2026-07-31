package com.mobilesco.mobilesco_back.modules.insumo.application.usecases;

import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.notificacion.application.usecases.NotificacionService;
import com.mobilesco.mobilesco_back.modules.notificacion.domain.models.TipoNotificacion;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockMinimoNotificacionService {

    private static final Set<String> ROLES_DESTINATARIOS = Set.of(
            "ADMIN",
            "DIRECTOR_GENERAL",
            "SUBDIRECCION_ADMINISTRATIVA",
            "JEFE_ALMACEN");

    private final NotificacionService notificacionService;

    public void notificarSiCruzaMinimo(InsumoModel insumo, double stockAnterior, double stockNuevo) {
        double minimo = minimo(insumo);
        if (esAlertaValida(insumo, minimo) && stockAnterior > minimo && stockNuevo <= minimo) {
            notificar(insumo, stockNuevo, minimo);
        }
    }

    public void notificarSiNuevoInsumoIniciaBajo(InsumoModel insumo) {
        double minimo = minimo(insumo);
        double actual = stock(insumo);
        if (esAlertaValida(insumo, minimo) && actual <= minimo) {
            notificar(insumo, actual, minimo);
        }
    }

    public void notificarSiCambioMinimoGeneraAlerta(InsumoModel insumo, Double minimoAnterior) {
        double minimoNuevo = minimo(insumo);
        double actual = stock(insumo);
        boolean estabaEnAlerta = minimoAnterior != null && minimoAnterior > 0 && actual <= minimoAnterior;
        if (esAlertaValida(insumo, minimoNuevo) && !estabaEnAlerta && actual <= minimoNuevo) {
            notificar(insumo, actual, minimoNuevo);
        }
    }

    private void notificar(InsumoModel insumo, double actual, double minimo) {
        String unidad = insumo.getUnidadMedida() != null && insumo.getUnidadMedida().getSimbolo() != null
                ? " " + insumo.getUnidadMedida().getSimbolo()
                : "";
        String mensaje = String.format(
                Locale.ROOT,
                "%s (%s) alcanzó su existencia mínima. Stock actual: %.2f%s; mínimo: %.2f%s.",
                insumo.getNombre(),
                insumo.getCodigo(),
                actual,
                unidad,
                minimo,
                unidad);

        notificacionService.notificarRoles(
                ROLES_DESTINATARIOS,
                TipoNotificacion.ALERTA,
                "Stock mínimo alcanzado",
                mensaje,
                "ALMACEN",
                "INSUMO",
                insumo.getId(),
                "/insumos/" + insumo.getId());
    }

    private boolean esAlertaValida(InsumoModel insumo, double minimo) {
        return insumo != null
                && insumo.getId() != null
                && Boolean.TRUE.equals(insumo.getActivo())
                && minimo > 0;
    }

    private double stock(InsumoModel insumo) {
        return insumo.getStockActual() == null ? 0 : insumo.getStockActual();
    }

    private double minimo(InsumoModel insumo) {
        return insumo == null || insumo.getStockMinimo() == null ? 0 : insumo.getStockMinimo();
    }
}
