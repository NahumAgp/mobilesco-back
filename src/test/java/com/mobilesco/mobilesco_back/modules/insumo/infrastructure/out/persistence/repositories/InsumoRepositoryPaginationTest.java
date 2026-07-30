package com.mobilesco.mobilesco_back.modules.insumo.infrastructure.out.persistence.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories.DetalleCompraRepository;
import com.mobilesco.mobilesco_back.modules.unidadmedida.domain.models.UnidadMedidaModel;
import com.mobilesco.mobilesco_back.modules.unidadmedida.infrastructure.out.persistence.repositories.UnidadMedidaRepository;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@ActiveProfiles("test")
@Transactional
class InsumoRepositoryPaginationTest {

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private UnidadMedidaRepository unidadMedidaRepository;

    @Autowired
    private DetalleCompraRepository detalleCompraRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private EntityManager entityManager;

    private UnidadMedidaModel pieza;

    @BeforeEach
    void setUp() {
        pieza = new UnidadMedidaModel();
        pieza.setNombre("Pieza");
        pieza.setSimbolo("pz");
        pieza.setEstado(true);
        pieza = unidadMedidaRepository.save(pieza);

        guardar("TOR-01", "Tornillo bajo", true, 1.0, 5.0);
        guardar("TOR-02", "Tornillo suficiente", true, 8.0, 5.0);
        guardar("TOR-03", "Tornillo inactivo", false, 1.0, 5.0);
        guardar("TUE-01", "Tuerca bajo", true, 1.0, 5.0);
    }

    @Test
    void filtraCuentaOrdenaYPaginaEnBaseDeDatos() {
        var page = insumoRepository.buscarPaginado(
                "tornillo",
                true,
                true,
                PageRequest.of(0, 1, Sort.by("nombre").ascending()));

        assertEquals(1, page.getContent().size());
        assertEquals("Tornillo bajo", page.getContent().get(0).getNombre());
        assertEquals(1, page.getTotalElements());
        assertEquals(1, page.getTotalPages());
        assertTrue(detalleCompraRepository
                .findUltimosCostosRecibidosByInsumos(
                        page.getContent().stream().map(InsumoModel::getId).toList())
                .isEmpty());
    }

    @Test
    void laPaginaConUnidadSeResuelveConConsultaAcotada() {
        entityManager.flush();
        entityManager.clear();
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        var page = insumoRepository.buscarPaginado(
                null,
                null,
                false,
                PageRequest.of(0, 2, Sort.by("nombre").ascending()));
        page.getContent().forEach(insumo -> insumo.getUnidadMedida().getNombre());

        assertEquals(2, page.getContent().size());
        assertEquals(4, page.getTotalElements());
        assertTrue(
                statistics.getPrepareStatementCount() <= 2,
                "La pagina y su conteo no deben disparar consultas por cada unidad");
    }

    private void guardar(String codigo, String nombre, boolean activo, double actual, double minimo) {
        insumoRepository.save(InsumoModel.builder()
                .codigo(codigo)
                .codigoBarras(null)
                .nombre(nombre)
                .unidadMedida(pieza)
                .stockActual(actual)
                .stockMinimo(minimo)
                .activo(activo)
                .build());
    }
}
