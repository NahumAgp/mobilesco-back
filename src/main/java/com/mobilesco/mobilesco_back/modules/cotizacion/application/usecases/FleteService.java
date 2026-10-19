package com.mobilesco.mobilesco_back.modules.cotizacion.application.usecases;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import com.mobilesco.mobilesco_back.modules.cotizacion.domain.models.FleteTabuladorModel;
import com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.in.api.dtos.CotizacionFleteResponseDTO;
import com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.in.api.dtos.CotizacionFleteRequestDTO;
import com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.in.api.dtos.FleteOrigenDTO;
import com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.in.api.dtos.FleteTabuladorDTO;
import com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.out.persistence.repositories.FleteTabuladorRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;

@Service
public class FleteService {
    private final FleteTabuladorRepository tabuladorRepository;
    private final RestClient routesClient;
    private final String apiKey;
    private final String origenNombre;
    private final double origenLat;
    private final double origenLng;

    @Autowired
    public FleteService(FleteTabuladorRepository tabuladorRepository,
            @Value("${app.google.maps.api-key:}") String apiKey,
            @Value("${app.flete.origen-nombre}") String origenNombre,
            @Value("${app.flete.origen-lat}") double origenLat,
            @Value("${app.flete.origen-lng}") double origenLng) {
        this(tabuladorRepository, apiKey, origenNombre, origenLat, origenLng, crearRoutesClient());
    }

    FleteService(FleteTabuladorRepository tabuladorRepository, String apiKey, String origenNombre,
            double origenLat, double origenLng, RestClient routesClient) {
        this.tabuladorRepository = tabuladorRepository;
        this.apiKey = apiKey;
        this.origenNombre = origenNombre;
        this.origenLat = origenLat;
        this.origenLng = origenLng;
        this.routesClient = routesClient;
    }

    private static RestClient crearRoutesClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));
        return RestClient.builder().baseUrl("https://routes.googleapis.com")
                .requestFactory(factory).build();
    }

    @Transactional(readOnly = true)
    public CotizacionFleteResponseDTO cotizar(CotizacionFleteRequestDTO solicitud) {
        if (solicitud == null) throw new ValidationException("Selecciona un punto o ingresa la dirección de entrega");
        boolean tieneLat = solicitud.latitud() != null;
        boolean tieneLng = solicitud.longitud() != null;
        if (tieneLat != tieneLng) throw new ValidationException("Selecciona un punto válido en el mapa");
        if (tieneLat && (!Double.isFinite(solicitud.latitud()) || !Double.isFinite(solicitud.longitud())
                || Math.abs(solicitud.latitud()) > 90 || Math.abs(solicitud.longitud()) > 180)) {
            throw new ValidationException("Selecciona un punto válido en el mapa");
        }
        String destino = solicitud.destino() == null ? "" : solicitud.destino().trim();
        if (!tieneLat && destino.isBlank()) throw new ValidationException("Selecciona un punto o ingresa la dirección de entrega");
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Configura GOOGLE_MAPS_API_KEY en el servidor");
        }
        Map<String, ?> destinoRuta = tieneLat
                ? Map.of("location", Map.of("latLng", Map.of("latitude", solicitud.latitud(), "longitude", solicitud.longitud())))
                : Map.of("address", destino);
        Map<?, ?> respuesta;
        try {
            respuesta = routesClient.post().uri("/directions/v2:computeRoutes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Goog-Api-Key", apiKey)
                    .header("X-Goog-FieldMask", "routes.distanceMeters,routes.duration,routes.polyline.encodedPolyline")
                    .body(Map.of(
                            "origin", Map.of("location", Map.of("latLng", Map.of(
                                    "latitude", origenLat, "longitude", origenLng))),
                            "destination", destinoRuta,
                            "travelMode", "DRIVE",
                            "routingPreference", "TRAFFIC_AWARE_OPTIMAL"))
                    .retrieve().body(Map.class);
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Google Maps no pudo calcular la ruta. Revisa la clave, la API Routes y las direcciones.", ex);
        }

        long metros = -1;
        long duracionSegundos = 0;
        String polilinea = "";
        if (respuesta != null && respuesta.get("routes") instanceof List<?> rutas
                && !rutas.isEmpty() && rutas.get(0) instanceof Map<?, ?> ruta) {
            if (ruta.get("distanceMeters") instanceof Number distancia) metros = distancia.longValue();
            if (ruta.get("duration") instanceof String duracion && duracion.endsWith("s")) {
                try { duracionSegundos = Math.round(Double.parseDouble(duracion.substring(0, duracion.length() - 1))); }
                catch (NumberFormatException ignored) { /* Google puede omitir la duración. */ }
            }
            if (ruta.get("polyline") instanceof Map<?, ?> polyline
                    && polyline.get("encodedPolyline") instanceof String encoded) polilinea = encoded;
        }
        if (metros < 0) throw new ValidationException("No se encontró una ruta por carretera hasta ese destino");
        BigDecimal km = BigDecimal.valueOf(metros).divide(BigDecimal.valueOf(1000), 2, RoundingMode.CEILING);
        FleteTabuladorModel tarifa = tabuladorRepository
                .findFirstByHastaKmGreaterThanEqualOrderByHastaKmAsc(km).orElse(null);
        return new CotizacionFleteResponseDTO(origenNombre,
                tieneLat ? String.format(java.util.Locale.ROOT, "%.6f, %.6f", solicitud.latitud(), solicitud.longitud()) : destino,
                km, duracionSegundos, polilinea,
                tarifa == null ? null : tarifa.getHastaKm(), tarifa == null ? null : tarifa.getPrecio());
    }

    public FleteOrigenDTO obtenerOrigen() {
        return new FleteOrigenDTO(origenNombre, origenLat, origenLng);
    }

    @Transactional(readOnly = true)
    public List<FleteTabuladorDTO> listarTabulador() {
        return tabuladorRepository.findAllByOrderByHastaKmAsc().stream()
                .map(fila -> new FleteTabuladorDTO(fila.getHastaKm(), fila.getPrecio())).toList();
    }

    @Transactional
    public List<FleteTabuladorDTO> guardarTabulador(List<FleteTabuladorDTO> filas) {
        if (filas == null || filas.size() > 1000) throw new ValidationException("El tabulador admite hasta 1000 rangos");
        if (filas.stream().anyMatch(fila -> fila == null || fila.hastaKm() == null)) {
            throw new ValidationException("Cada rango necesita un límite de kilómetros");
        }
        List<FleteTabuladorDTO> ordenadas = filas.stream()
                .sorted(Comparator.comparing(FleteTabuladorDTO::hastaKm))
                .toList();
        BigDecimal anterior = BigDecimal.ZERO;
        for (FleteTabuladorDTO fila : ordenadas) {
            if (fila == null || fila.hastaKm() == null || fila.precio() == null
                    || fila.hastaKm().signum() <= 0 || fila.hastaKm().scale() > 2
                    || fila.hastaKm().precision() - fila.hastaKm().scale() > 8
                    || fila.precio().signum() < 0 || fila.precio().scale() > 2
                    || fila.precio().precision() - fila.precio().scale() > 12) {
                throw new ValidationException("Cada rango necesita un límite mayor a cero y un precio válido con hasta dos decimales");
            }
            if (fila.hastaKm().compareTo(anterior) <= 0) {
                throw new ValidationException("Los límites de kilómetros no pueden repetirse");
            }
            anterior = fila.hastaKm();
        }
        tabuladorRepository.deleteAllInBatch();
        tabuladorRepository.saveAll(ordenadas.stream().map(fila -> {
            FleteTabuladorModel modelo = new FleteTabuladorModel();
            modelo.setHastaKm(fila.hastaKm());
            modelo.setPrecio(fila.precio());
            return modelo;
        }).toList());
        return listarTabulador();
    }
}
