package com.mobilesco.mobilesco_back.config;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.mobilesco.mobilesco_back.modules.compra.domain.models.CompraModel;
import com.mobilesco.mobilesco_back.modules.compra.domain.models.DetalleCompraModel;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories.CompraRepository;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories.DetalleCompraRepository;
import com.mobilesco.mobilesco_back.modules.kardex.infrastructure.out.persistence.repositories.KardexRepository;

@Configuration
public class CompraRecepcionConsistencyConfig {

    private static final Logger log = LoggerFactory.getLogger(CompraRecepcionConsistencyConfig.class);

    @Bean
    @Order(2)
    CommandLineRunner normalizePendingPurchaseReceipts(
            CompraRepository compraRepository,
            DetalleCompraRepository detalleCompraRepository,
            KardexRepository kardexRepository) {
        return args -> {
            int comprasCorregidas = 0;
            int detallesCorregidos = 0;

            for (CompraModel compra : compraRepository.findByEstadoAndActivoTrue("PENDIENTE")) {
                if (kardexRepository.existsByCompraId(compra.getId())) {
                    continue;
                }

                List<DetalleCompraModel> detalles = detalleCompraRepository.findByCompraId(compra.getId());
                List<DetalleCompraModel> corregidos = new ArrayList<>();

                for (DetalleCompraModel detalle : detalles) {
                    double recibida = valorSeguro(detalle.getCantidadRecibida());
                    if (recibida <= 0) {
                        continue;
                    }

                    detalle.setCantidadRecibida(0.0);
                    detalle.setMotivoNoRecepcion(null);
                    corregidos.add(detalle);
                }

                if (!corregidos.isEmpty()) {
                    detalleCompraRepository.saveAll(corregidos);
                    comprasCorregidas++;
                    detallesCorregidos += corregidos.size();
                }
            }

            if (detallesCorregidos > 0) {
                log.info(
                        "Recepcion normalizada para compras pendientes sin kardex: {} compras, {} detalles",
                        comprasCorregidas,
                        detallesCorregidos);
            }
        };
    }

    private static double valorSeguro(Double valor) {
        return valor != null ? valor.doubleValue() : 0.0;
    }
}
