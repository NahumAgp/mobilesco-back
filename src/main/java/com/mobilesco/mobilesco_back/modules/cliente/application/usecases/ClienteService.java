package com.mobilesco.mobilesco_back.modules.cliente.application.usecases;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.mobilesco.mobilesco_back.modules.cliente.domain.models.ClasificacionCliente;
import com.mobilesco.mobilesco_back.modules.cliente.domain.models.ClienteModel;
import com.mobilesco.mobilesco_back.modules.cliente.infrastructure.in.api.dtos.CatalogoClienteDTO;
import com.mobilesco.mobilesco_back.modules.cliente.infrastructure.in.api.dtos.ClienteRequestDTO;
import com.mobilesco.mobilesco_back.modules.cliente.infrastructure.in.api.dtos.ClienteResponseDTO;
import com.mobilesco.mobilesco_back.modules.cliente.infrastructure.out.persistence.repositories.ClienteRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional
    public ClienteResponseDTO crear(ClienteRequestDTO dto) {
        validarRfcUnico(dto.getRfc(), null);
        ClienteModel cliente = new ClienteModel();
        cliente.setCodigo(generarCodigo());
        aplicar(cliente, dto);
        cliente.setActivo(true);
        return map(clienteRepository.save(cliente));
    }

    @Transactional(readOnly = true)
    public Page<ClienteResponseDTO> listar(
            Boolean activo,
            ClasificacionCliente clasificacion,
            String busqueda,
            Pageable pageable) {
        String filtro = StringUtils.hasText(busqueda) ? busqueda.trim() : null;
        return clienteRepository.buscar(activo, clasificacion, filtro, pageable).map(this::map);
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO obtenerPorId(Long id) {
        return map(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarActivos() {
        return clienteRepository.findByActivoTrueOrderByNombreAscRazonSocialAsc()
                .stream()
                .map(this::map)
                .toList();
    }

    public List<CatalogoClienteDTO> listarClasificaciones() {
        return Arrays.stream(ClasificacionCliente.values())
                .map(tipo -> new CatalogoClienteDTO(tipo.name(), tipo.getEtiqueta()))
                .toList();
    }

    @Transactional
    public ClienteResponseDTO actualizar(Long id, ClienteRequestDTO dto) {
        ClienteModel cliente = buscar(id);
        validarRfcUnico(dto.getRfc(), id);
        aplicar(cliente, dto);
        cliente.setActivo(dto.getActivo() == null ? cliente.getActivo() : dto.getActivo());
        return map(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponseDTO cambiarEstatus(Long id, boolean activo) {
        ClienteModel cliente = buscar(id);
        cliente.setActivo(activo);
        return map(clienteRepository.save(cliente));
    }

    @Transactional
    public void eliminar(Long id) {
        ClienteModel cliente = buscar(id);
        try {
            clienteRepository.delete(cliente);
            clienteRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ValidationException(
                    "El cliente tiene movimientos relacionados. Desactívalo para conservar su historial.");
        }
    }

    private ClienteModel buscar(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));
    }

    private void validarRfcUnico(String rfc, Long idActual) {
        String normalizado = normalizarRfc(rfc);
        if (normalizado == null) {
            return;
        }
        clienteRepository.findByRfcIgnoreCase(normalizado).ifPresent(existente -> {
            if (idActual == null || !existente.getId().equals(idActual)) {
                throw new BadRequestException("Ya existe un cliente con ese RFC");
            }
        });
    }

    private void aplicar(ClienteModel cliente, ClienteRequestDTO dto) {
        cliente.setClasificacion(dto.getClasificacion());
        cliente.setTipoPersona(dto.getTipoPersona());
        cliente.setNombre(limpiar(dto.getNombre()));
        cliente.setRazonSocial(limpiar(dto.getRazonSocial()));
        cliente.setNombreComercial(limpiar(dto.getNombreComercial()));
        cliente.setRfc(normalizarRfc(dto.getRfc()));
        cliente.setContactoNombre(limpiar(dto.getContactoNombre()));
        cliente.setCorreo(normalizarCorreo(dto.getCorreo()));
        cliente.setTelefono(limpiar(dto.getTelefono()));
        cliente.setWhatsapp(limpiar(dto.getWhatsapp()));
        cliente.setEstado(limpiar(dto.getEstado()));
        cliente.setCiudad(limpiar(dto.getCiudad()));
        cliente.setColonia(limpiar(dto.getColonia()));
        cliente.setCalle(limpiar(dto.getCalle()));
        cliente.setNumeroExterior(limpiar(dto.getNumeroExterior()));
        cliente.setNumeroInterior(limpiar(dto.getNumeroInterior()));
        cliente.setCodigoPostal(limpiar(dto.getCodigoPostal()));
        cliente.setDiasCredito(dto.getDiasCredito() == null ? 0 : dto.getDiasCredito());
        cliente.setLimiteCredito(dto.getLimiteCredito() == null ? BigDecimal.ZERO : dto.getLimiteCredito());
        cliente.setNotas(limpiar(dto.getNotas()));
    }

    private ClienteResponseDTO map(ClienteModel cliente) {
        return ClienteResponseDTO.builder()
                .id(cliente.getId())
                .codigo(cliente.getCodigo())
                .clasificacion(cliente.getClasificacion())
                .clasificacionEtiqueta(cliente.getClasificacion().getEtiqueta())
                .tipoPersona(cliente.getTipoPersona())
                .tipoPersonaEtiqueta(cliente.getTipoPersona().getEtiqueta())
                .nombre(cliente.getNombre())
                .razonSocial(cliente.getRazonSocial())
                .nombreComercial(cliente.getNombreComercial())
                .nombreVisual(primerTexto(cliente.getNombreComercial(), cliente.getRazonSocial(), cliente.getNombre()))
                .rfc(cliente.getRfc())
                .contactoNombre(cliente.getContactoNombre())
                .correo(cliente.getCorreo())
                .telefono(cliente.getTelefono())
                .whatsapp(cliente.getWhatsapp())
                .estado(cliente.getEstado())
                .ciudad(cliente.getCiudad())
                .colonia(cliente.getColonia())
                .calle(cliente.getCalle())
                .numeroExterior(cliente.getNumeroExterior())
                .numeroInterior(cliente.getNumeroInterior())
                .codigoPostal(cliente.getCodigoPostal())
                .diasCredito(cliente.getDiasCredito())
                .limiteCredito(cliente.getLimiteCredito())
                .notas(cliente.getNotas())
                .activo(cliente.getActivo())
                .fechaRegistro(cliente.getFechaRegistro())
                .fechaActualizacion(cliente.getFechaActualizacion())
                .build();
    }

    private String generarCodigo() {
        return "CLI-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
    }

    private String normalizarRfc(String value) {
        String limpio = limpiar(value);
        return limpio == null ? null : limpio.toUpperCase(Locale.ROOT);
    }

    private String normalizarCorreo(String value) {
        String limpio = limpiar(value);
        return limpio == null ? null : limpio.toLowerCase(Locale.ROOT);
    }

    private String limpiar(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String primerTexto(String... values) {
        return Arrays.stream(values).filter(StringUtils::hasText).findFirst().orElse("Cliente");
    }
}
