package com.mobilesco.mobilesco_back.modules.cotizacion.application.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.mobilesco.mobilesco_back.modules.cotizacion.domain.models.FleteTabuladorModel;
import com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.in.api.dtos.FleteTabuladorDTO;
import com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.in.api.dtos.CotizacionFleteRequestDTO;
import com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.out.persistence.repositories.FleteTabuladorRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;

class FleteServiceTest {
    @Test
    void usaLaDistanciaPorCarreteraYElPrimerLimiteQueLaCubre() {
        FleteTabuladorRepository repository = mock(FleteTabuladorRepository.class);
        RestClient.Builder builder = RestClient.builder().baseUrl("https://routes.googleapis.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://routes.googleapis.com/directions/v2:computeRoutes"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Goog-Api-Key", "test-key"))
                .andExpect(header("X-Goog-FieldMask", "routes.distanceMeters,routes.duration,routes.polyline.encodedPolyline"))
                .andExpect(jsonPath("$.origin.location.latLng.latitude").value(20.109986478309487))
                .andExpect(jsonPath("$.routingPreference").value("TRAFFIC_AWARE_OPTIMAL"))
                .andRespond(withSuccess("{\"routes\":[{\"distanceMeters\":15001,\"duration\":\"1250s\",\"polyline\":{\"encodedPolyline\":\"abc\"}}]}", MediaType.APPLICATION_JSON));
        FleteTabuladorModel tarifa = new FleteTabuladorModel();
        tarifa.setHastaKm(new BigDecimal("20.00"));
        tarifa.setPrecio(new BigDecimal("400.00"));
        when(repository.findFirstByHastaKmGreaterThanEqualOrderByHastaKmAsc(new BigDecimal("15.01")))
                .thenReturn(Optional.of(tarifa));

        FleteService service = new FleteService(repository, "test-key", "Rosales", 20.109986478309487,
                -98.8268290353736, builder.build());
        var resultado = service.cotizar(new CotizacionFleteRequestDTO("Pachuca, Hidalgo", null, null));

        assertThat(resultado.distanciaKm()).isEqualByComparingTo("15.01");
        assertThat(resultado.hastaKm()).isEqualByComparingTo("20.00");
        assertThat(resultado.fleteSugerido()).isEqualByComparingTo("400.00");
        assertThat(resultado.duracionSegundos()).isEqualTo(1250);
        assertThat(resultado.polilinea()).isEqualTo("abc");
        server.verify();
    }

    @Test
    void devuelveRutaSeleccionadaEnMapaAunqueNoExistaTarifa() {
        FleteTabuladorRepository repository = mock(FleteTabuladorRepository.class);
        RestClient.Builder builder = RestClient.builder().baseUrl("https://routes.googleapis.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://routes.googleapis.com/directions/v2:computeRoutes"))
                .andExpect(jsonPath("$.destination.location.latLng.latitude").value(20.12))
                .andRespond(withSuccess("{\"routes\":[{\"distanceMeters\":8500,\"duration\":\"960s\"}]}", MediaType.APPLICATION_JSON));
        FleteService service = new FleteService(repository, "test-key", "Rosales", 20.109986478309487,
                -98.8268290353736, builder.build());

        var resultado = service.cotizar(new CotizacionFleteRequestDTO(null, 20.12, -98.8));

        assertThat(resultado.distanciaKm()).isEqualByComparingTo("8.50");
        assertThat(resultado.duracionSegundos()).isEqualTo(960);
        assertThat(resultado.fleteSugerido()).isNull();
        server.verify();
    }

    @Test
    void rechazaLimitesDuplicadosSinBorrarElTabuladorAnterior() {
        FleteTabuladorRepository repository = mock(FleteTabuladorRepository.class);
        FleteService service = new FleteService(repository, "test-key", "Rosales", 20.1,
                -98.8, RestClient.create());

        assertThatThrownBy(() -> service.guardarTabulador(List.of(
                new FleteTabuladorDTO(new BigDecimal("10"), new BigDecimal("100")),
                new FleteTabuladorDTO(new BigDecimal("10.00"), new BigDecimal("200")))))
                .isInstanceOf(ValidationException.class);
        verify(repository, never()).deleteAllInBatch();
        verify(repository, never()).saveAll(any());
    }
}
