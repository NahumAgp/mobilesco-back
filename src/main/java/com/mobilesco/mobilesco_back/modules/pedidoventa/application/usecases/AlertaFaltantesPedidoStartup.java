package com.mobilesco.mobilesco_back.modules.pedidoventa.application.usecases;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.mobilesco.mobilesco_back.modules.pedidoventa.domain.models.EstadoPedidoVenta;
import com.mobilesco.mobilesco_back.modules.pedidoventa.infrastructure.out.persistence.repositories.PedidoVentaRepository;

import lombok.RequiredArgsConstructor;

@Component
@Order(2)
@RequiredArgsConstructor
public class AlertaFaltantesPedidoStartup implements ApplicationRunner {

    private final PedidoVentaRepository pedidoRepository;
    private final AlertaFaltanteInsumosPedidoService alertaService;

    @Override
    public void run(ApplicationArguments args) {
        pedidoRepository.buscarIdsPorEstados(List.of(
                        EstadoPedidoVenta.CONFIRMADO,
                        EstadoPedidoVenta.EN_PRODUCCION))
                .forEach(alertaService::notificarFaltantes);
    }
}
