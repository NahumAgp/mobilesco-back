package com.mobilesco.mobilesco_back.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mobilesco.mobilesco_back.dto.imagen.ImagenResponseDTO;
import com.mobilesco.mobilesco_back.dto.variante.VarianteCompletaResponseDTO;
import com.mobilesco.mobilesco_back.dto.variante.VarianteCreateDTO;
import com.mobilesco.mobilesco_back.dto.variante.VarianteResponseDTO;
import com.mobilesco.mobilesco_back.dto.variante.VarianteUpdateDTO;
import com.mobilesco.mobilesco_back.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.models.ColorModel;
import com.mobilesco.mobilesco_back.models.NivelModel;
import com.mobilesco.mobilesco_back.models.ProductoBaseModel;
import com.mobilesco.mobilesco_back.models.VarianteModel;
import com.mobilesco.mobilesco_back.repositories.ColorRepository;
import com.mobilesco.mobilesco_back.repositories.NivelRepository;
import com.mobilesco.mobilesco_back.repositories.ProductoBaseRepository;
import com.mobilesco.mobilesco_back.repositories.VarianteRepository;

@Service
public class VarianteService {

    private final VarianteRepository varianteRepository;
    private final ProductoBaseRepository productoBaseRepository;
    private final NivelRepository nivelRepository;
    private final ColorRepository colorRepository;
    private final ImagenService imagenService;

    public VarianteService(VarianteRepository varianteRepository,
                           ProductoBaseRepository productoBaseRepository,
                           NivelRepository nivelRepository,
                           ColorRepository colorRepository,
                           ImagenService imagenService) {
        this.varianteRepository = varianteRepository;
        this.productoBaseRepository = productoBaseRepository;
        this.nivelRepository = nivelRepository;
        this.colorRepository = colorRepository;
        this.imagenService = imagenService;
    }

    private VarianteResponseDTO mapToResponseDTO(VarianteModel variante) {
        VarianteResponseDTO dto = new VarianteResponseDTO();
        dto.setId(variante.getId());
        dto.setSku(variante.getSku());
        dto.setNombre(variante.getNombre());
        dto.setDescripcion(variante.getDescripcion());
        dto.setActivo(variante.getActivo());
        dto.setCreatedAt(variante.getCreatedAt());
        dto.setUpdatedAt(variante.getUpdatedAt());

        if (variante.getProductoBase() != null) {
            dto.setProductoBaseId(variante.getProductoBase().getId());
        }
        if (variante.getNivel() != null) {
            dto.setNivelId(variante.getNivel().getId());
        }
        if (variante.getColor() != null) {
            dto.setColorId(variante.getColor().getId());
        }

        return dto;
    }

    private String generarSku(ProductoBaseModel productoBase, NivelModel nivel, ColorModel color) {
        if (productoBase.getFamilia() == null || productoBase.getFamilia().getLinea() == null) {
            throw new BadRequestException("El modelo debe tener familia y linea para generar el sku");
        }

        String lineaCodigo = productoBase.getFamilia().getLinea().getCodigo();
        String familiaCodigo = productoBase.getFamilia().getCodigo();
        String modeloCodigo = productoBase.getCodigo();
        String nivelCodigo = nivel.getCodigo();
        String colorCodigo = color.getCodigo();

        if (lineaCodigo == null || familiaCodigo == null || modeloCodigo == null || nivelCodigo == null || colorCodigo == null) {
            throw new BadRequestException("Faltan codigos requeridos para generar el sku de variante");
        }

        return (lineaCodigo + familiaCodigo + modeloCodigo + "-" + nivelCodigo + "-" + colorCodigo).toUpperCase();
    }

    private List<VarianteResponseDTO> mapToResponseDTOList(List<VarianteModel> variantes) {
        return variantes.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    public VarianteResponseDTO crear(VarianteCreateDTO dto) {

        ProductoBaseModel productoBase = productoBaseRepository.findById(dto.getProductoBaseId())
                .orElseThrow(() -> new NotFoundException("Producto base no encontrado con ID: " + dto.getProductoBaseId()));

        NivelModel nivel = nivelRepository.findById(dto.getNivelId())
                .orElseThrow(() -> new NotFoundException("Nivel no encontrado con ID: " + dto.getNivelId()));

        ColorModel color = colorRepository.findById(dto.getColorId())
                .orElseThrow(() -> new NotFoundException("Color no encontrado con ID: " + dto.getColorId()));

        String skuGenerado = generarSku(productoBase, nivel, color);
        if (varianteRepository.existsBySku(skuGenerado)) {
            throw new BadRequestException("Ya existe una variante con el sku: " + skuGenerado);
        }

        VarianteModel variante = new VarianteModel();
        variante.setSku(skuGenerado);
        variante.setNombre(dto.getNombre());
        variante.setDescripcion(dto.getDescripcion());
        variante.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        variante.setProductoBase(productoBase);
        variante.setNivel(nivel);
        variante.setColor(color);

        return mapToResponseDTO(varianteRepository.save(variante));
    }

    public List<VarianteResponseDTO> obtenerTodos() {
        return mapToResponseDTOList(varianteRepository.findAll());
    }

    public VarianteResponseDTO obtenerPorId(Long id) {
        VarianteModel variante = varianteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Variante no encontrada con ID: " + id));
        return mapToResponseDTO(variante);
    }

    public VarianteResponseDTO obtenerPorSku(String sku) {
        VarianteModel variante = varianteRepository.findBySku(sku)
                .orElseThrow(() -> new NotFoundException("Variante no encontrada con SKU: " + sku));
        return mapToResponseDTO(variante);
    }

    public List<VarianteResponseDTO> obtenerPorProductoBase(Long productoBaseId) {
        return mapToResponseDTOList(varianteRepository.findByProductoBaseId(productoBaseId));
    }

    public List<VarianteResponseDTO> buscarConFiltros(String sku, String nombre, Long productoBaseId,
                                                       Long nivelId, Long colorId) {
        return mapToResponseDTOList(varianteRepository.buscarConFiltros(sku, nombre, productoBaseId, nivelId, colorId));
    }

    public VarianteResponseDTO actualizar(Long id, VarianteUpdateDTO dto) {

        VarianteModel existente = varianteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Variante no encontrada con ID: " + id));

        if (dto.getNombre() != null) {
            existente.setNombre(dto.getNombre());
        }

        if (dto.getDescripcion() != null) {
            existente.setDescripcion(dto.getDescripcion());
        }

        if (dto.getProductoBaseId() != null) {
            ProductoBaseModel productoBase = productoBaseRepository.findById(dto.getProductoBaseId())
                    .orElseThrow(() -> new NotFoundException("Producto base no encontrado con ID: " + dto.getProductoBaseId()));
            existente.setProductoBase(productoBase);
        }

        if (dto.getNivelId() != null) {
            NivelModel nivel = nivelRepository.findById(dto.getNivelId())
                    .orElseThrow(() -> new NotFoundException("Nivel no encontrado con ID: " + dto.getNivelId()));
            existente.setNivel(nivel);
        }

        if (dto.getColorId() != null) {
            ColorModel color = colorRepository.findById(dto.getColorId())
                    .orElseThrow(() -> new NotFoundException("Color no encontrado con ID: " + dto.getColorId()));
            existente.setColor(color);
        }

        if (dto.getActivo() != null) {
            existente.setActivo(dto.getActivo());
        }

        String skuGenerado = generarSku(existente.getProductoBase(), existente.getNivel(), existente.getColor());
        varianteRepository.findBySku(skuGenerado)
                .filter(otra -> !otra.getId().equals(existente.getId()))
                .ifPresent(otra -> {
                    throw new BadRequestException("Ya existe una variante con el sku: " + skuGenerado);
                });
        existente.setSku(skuGenerado);

        return mapToResponseDTO(varianteRepository.save(existente));
    }

    public void eliminar(Long id) {
        if (!varianteRepository.existsById(id)) {
            throw new NotFoundException("Variante no encontrada con ID: " + id);
        }
        varianteRepository.deleteById(id);
    }

    public VarianteCompletaResponseDTO obtenerVarianteCompleta(Long id) {
        VarianteModel variante = varianteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Variante no encontrada con ID: " + id));
        return mapToVarianteCompletaDTO(variante);
    }

    public VarianteCompletaResponseDTO obtenerVarianteCompletaPorSku(String sku) {
        VarianteModel variante = varianteRepository.findBySku(sku)
                .orElseThrow(() -> new NotFoundException("Variante no encontrada con SKU: " + sku));
        return mapToVarianteCompletaDTO(variante);
    }

    private VarianteCompletaResponseDTO mapToVarianteCompletaDTO(VarianteModel variante) {
        VarianteCompletaResponseDTO dto = new VarianteCompletaResponseDTO();
        dto.setId(variante.getId());
        dto.setSku(variante.getSku());
        dto.setNombre(variante.getNombre());
        dto.setDescripcion(variante.getDescripcion());
        dto.setActivo(variante.getActivo());
        dto.setCreatedAt(variante.getCreatedAt());
        dto.setUpdatedAt(variante.getUpdatedAt());

        if (variante.getProductoBase() != null) {
            dto.setProductoBaseId(variante.getProductoBase().getId());
        }
        if (variante.getNivel() != null) {
            dto.setNivelId(variante.getNivel().getId());
        }
        if (variante.getColor() != null) {
            dto.setColorId(variante.getColor().getId());
        }

        List<ImagenResponseDTO> imagenes = imagenService.obtenerPorVariante(variante.getId());
        dto.setImagenes(imagenes);
        dto.setImagenPrincipal(imagenService.obtenerPrincipalPorVariante(variante.getId()));

        return dto;
    }
}
