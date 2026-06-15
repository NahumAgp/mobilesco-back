package com.mobilesco.mobilesco_back.modules.imagen.application.usecases;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.mobilesco.mobilesco_back.modules.imagen.infrastructure.in.api.dtos.ImagenCreateDTO;
import com.mobilesco.mobilesco_back.modules.imagen.infrastructure.in.api.dtos.ImagenResponseDTO;
import com.mobilesco.mobilesco_back.modules.imagen.infrastructure.in.api.dtos.ImagenUpdateDTO;
import com.mobilesco.mobilesco_back.modules.imagen.domain.models.ImagenModel;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoModel;
import com.mobilesco.mobilesco_back.modules.imagen.infrastructure.out.persistence.repositories.ImagenRepository;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.NotFoundException;

@Service
public class ImagenService {

    private final ImagenRepository imagenRepository;
    private final ProductoRepository productoRepository;
    private final AlmacenamientoImagenesService almacenamientoImagenesService;

    public ImagenService(
            ImagenRepository imagenRepository,
            ProductoRepository productoRepository,
            AlmacenamientoImagenesService almacenamientoImagenesService
    ) {
        this.imagenRepository = imagenRepository;
        this.productoRepository = productoRepository;
        this.almacenamientoImagenesService = almacenamientoImagenesService;
    }

    private ImagenResponseDTO mapToResponseDTO(ImagenModel imagen) {
        ImagenResponseDTO dto = new ImagenResponseDTO();
        dto.setId(imagen.getId());
        dto.setUrl(imagen.getUrl());
        dto.setEsPrincipal(imagen.getEsPrincipal());
        dto.setOrden(imagen.getOrden());
        dto.setAltTexto(imagen.getAltTexto());
        dto.setCreatedAt(imagen.getCreatedAt());
        if (imagen.getProducto() != null) {
            dto.setProductoId(imagen.getProducto().getId());
        }
        return dto;
    }

    private List<ImagenResponseDTO> mapToResponseDTOList(List<ImagenModel> imagenes) {
        return imagenes.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private ProductoModel obtenerProducto(Long productoId) {
        return productoRepository.findById(productoId)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado con ID: " + productoId));
    }

    private List<ImagenModel> obtenerImagenesDelProducto(Long productoId) {
        return imagenRepository.findByProductoIdOrderByOrdenAsc(productoId);
    }

    private ImagenModel obtenerPrincipalDelProducto(Long productoId) {
        return imagenRepository.findByProductoIdAndEsPrincipalTrue(productoId).orElse(null);
    }

    private long contarImagenesDelProducto(Long productoId) {
        return imagenRepository.countByProductoId(productoId);
    }

    private void resetPrincipalDelProducto(Long productoId) {
        imagenRepository.resetPrincipalFlag(productoId);
    }

    @Transactional
    public ImagenResponseDTO crear(ImagenCreateDTO dto) {
        ProductoModel producto = obtenerProducto(dto.getProductoId());
        boolean esPrimeraImagen = contarImagenesDelProducto(producto.getId()) == 0;

        ImagenModel imagen = new ImagenModel();
        imagen.setUrl(dto.getUrl());
        imagen.setAltTexto(dto.getAltTexto());
        imagen.setProducto(producto);

        if (esPrimeraImagen || (dto.getEsPrincipal() != null && dto.getEsPrincipal())) {
            resetPrincipalDelProducto(producto.getId());
            imagen.setEsPrincipal(true);
        } else {
            imagen.setEsPrincipal(false);
        }

        if (dto.getOrden() != null) {
            imagen.setOrden(dto.getOrden());
        }

        ImagenModel guardado = imagenRepository.save(imagen);
        return mapToResponseDTO(guardado);
    }

    @Transactional
    public ImagenResponseDTO crearDesdeArchivo(
            Long productoId,
            MultipartFile archivo,
            Boolean esPrincipal,
            Integer orden,
            String altTexto
    ) {
        try {
            String urlPublica = almacenamientoImagenesService.guardarImagenProducto(productoId, archivo);

            ImagenCreateDTO dto = new ImagenCreateDTO();
            dto.setProductoId(productoId);
            dto.setUrl(urlPublica);
            dto.setAltTexto(altTexto);
            if (esPrincipal != null) {
                dto.setEsPrincipal(esPrincipal);
            }
            if (orden != null) {
                dto.setOrden(orden);
            }

            return crear(dto);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        } catch (IOException e) {
            throw new BadRequestException("No se pudo guardar la imagen. Verifica que el archivo sea valido.");
        }
    }

    public List<ImagenResponseDTO> obtenerPorProducto(Long productoId) {
        obtenerProducto(productoId);
        return mapToResponseDTOList(obtenerImagenesDelProducto(productoId));
    }

    public ImagenResponseDTO obtenerPrincipalPorProducto(Long productoId) {
        obtenerProducto(productoId);
        ImagenModel imagen = obtenerPrincipalDelProducto(productoId);
        return imagen != null ? mapToResponseDTO(imagen) : null;
    }

    public ImagenResponseDTO obtenerPorId(Long id) {
        ImagenModel imagen = imagenRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Imagen no encontrada con ID: " + id));
        return mapToResponseDTO(imagen);
    }

    @Transactional
    public ImagenResponseDTO actualizar(Long id, ImagenUpdateDTO dto) {
        ImagenModel existente = imagenRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Imagen no encontrada con ID: " + id));

        if (dto.getUrl() != null) {
            existente.setUrl(dto.getUrl());
        }

        if (dto.getAltTexto() != null) {
            existente.setAltTexto(dto.getAltTexto());
        }

        if (dto.getOrden() != null) {
            existente.setOrden(dto.getOrden());
        }

        if (dto.getEsPrincipal() != null && dto.getEsPrincipal() && !Boolean.TRUE.equals(existente.getEsPrincipal())) {
            resetPrincipalDelProducto(existente.getProducto().getId());
            existente.setEsPrincipal(true);
        } else if (dto.getEsPrincipal() != null && !dto.getEsPrincipal() && existente.getEsPrincipal()) {
            throw new BadRequestException("No se puede desmarcar la imagen principal. Debe marcar otra como principal primero.");
        }

        ImagenModel actualizado = imagenRepository.save(existente);
        return mapToResponseDTO(actualizado);
    }

    @Transactional
    public void eliminar(Long id) {
        ImagenModel imagen = imagenRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Imagen no encontrada con ID: " + id));

        ProductoModel productoGrupo = imagen.getProducto();
        boolean eraPrincipal = Boolean.TRUE.equals(imagen.getEsPrincipal());
        eliminarArchivoImagen(imagen);

        imagenRepository.deleteById(id);

        if (eraPrincipal) {
            List<ImagenModel> restantes = obtenerImagenesDelProducto(productoGrupo.getId());
            if (!restantes.isEmpty()) {
                ImagenModel nuevaPrincipal = restantes.get(0);
                nuevaPrincipal.setEsPrincipal(true);
                imagenRepository.save(nuevaPrincipal);
            }
        }
    }

    @Transactional
    public void eliminarTodasPorProducto(Long productoId) {
        obtenerProducto(productoId);
        List<ImagenModel> imagenes = obtenerImagenesDelProducto(productoId);
        imagenes.forEach(this::eliminarArchivoImagen);
        imagenRepository.deleteAll(imagenes);
    }

    public void eliminarArchivosFisicosPorProducto(Long productoId) {
        List<ImagenModel> imagenes = imagenRepository.findByProductoId(productoId);
        imagenes.forEach(this::eliminarArchivoImagen);

        try {
            almacenamientoImagenesService.eliminarCarpetaImagenesProducto(productoId);
        } catch (IOException e) {
            throw new BadRequestException("No se pudieron eliminar los archivos de imagen del producto.");
        }
    }

    private void eliminarArchivoImagen(ImagenModel imagen) {
        if (imagen == null || imagen.getUrl() == null || imagen.getUrl().isBlank()) {
            return;
        }

        try {
            almacenamientoImagenesService.eliminarImagenPublica(imagen.getUrl());
        } catch (IOException e) {
            throw new BadRequestException("No se pudo eliminar el archivo de imagen: " + imagen.getUrl());
        }
    }
}
