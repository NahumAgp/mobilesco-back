package com.mobilesco.mobilesco_back.services;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.mobilesco.mobilesco_back.dto.imagen.ImagenCreateDTO;
import com.mobilesco.mobilesco_back.dto.imagen.ImagenResponseDTO;
import com.mobilesco.mobilesco_back.dto.imagen.ImagenUpdateDTO;
import com.mobilesco.mobilesco_back.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.models.ImagenModel;
import com.mobilesco.mobilesco_back.models.ProductoModel;
import com.mobilesco.mobilesco_back.repositories.ImagenRepository;
import com.mobilesco.mobilesco_back.repositories.ProductoRepository;

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

    private boolean tieneGrupoImagenCompartida(ProductoModel producto) {
        return producto != null
                && producto.getModelo() != null
                && producto.getModelo().getId() != null
                && producto.getColor() != null
                && producto.getColor().getId() != null;
    }

    private List<ImagenModel> obtenerImagenesDelGrupo(ProductoModel producto) {
        if (tieneGrupoImagenCompartida(producto)) {
            return imagenRepository.findByModeloIdAndColorIdOrderByOrdenAsc(
                    producto.getModelo().getId(),
                    producto.getColor().getId()
            );
        }

        return imagenRepository.findByProductoIdOrderByOrdenAsc(producto.getId());
    }

    private ImagenModel obtenerPrincipalDelGrupo(ProductoModel producto) {
        if (tieneGrupoImagenCompartida(producto)) {
            List<ImagenModel> principales = imagenRepository.findPrincipalesByModeloIdAndColorId(
                    producto.getModelo().getId(),
                    producto.getColor().getId()
            );
            return principales.isEmpty() ? null : principales.get(0);
        }

        return imagenRepository.findByProductoIdAndEsPrincipalTrue(producto.getId()).orElse(null);
    }

    private long contarImagenesDelGrupo(ProductoModel producto) {
        if (tieneGrupoImagenCompartida(producto)) {
            return imagenRepository.countByModeloIdAndColorId(
                    producto.getModelo().getId(),
                    producto.getColor().getId()
            );
        }

        return imagenRepository.countByProductoId(producto.getId());
    }

    private void resetPrincipalDelGrupo(ProductoModel producto) {
        if (tieneGrupoImagenCompartida(producto)) {
            imagenRepository.resetPrincipalFlagByModeloIdAndColorId(
                    producto.getModelo().getId(),
                    producto.getColor().getId()
            );
            return;
        }

        imagenRepository.resetPrincipalFlag(producto.getId());
    }

    @Transactional
    public ImagenResponseDTO crear(ImagenCreateDTO dto) {
        ProductoModel producto = obtenerProducto(dto.getProductoId());
        boolean esPrimeraImagen = contarImagenesDelGrupo(producto) == 0;

        ImagenModel imagen = new ImagenModel();
        imagen.setUrl(dto.getUrl());
        imagen.setAltTexto(dto.getAltTexto());
        imagen.setProducto(producto);

        if (esPrimeraImagen || (dto.getEsPrincipal() != null && dto.getEsPrincipal())) {
            resetPrincipalDelGrupo(producto);
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
        return mapToResponseDTOList(obtenerImagenesDelGrupo(obtenerProducto(productoId)));
    }

    public ImagenResponseDTO obtenerPrincipalPorProducto(Long productoId) {
        ImagenModel imagen = obtenerPrincipalDelGrupo(obtenerProducto(productoId));
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

        if (dto.getEsPrincipal() != null && dto.getEsPrincipal() && !existente.getEsPrincipal()) {
            resetPrincipalDelGrupo(existente.getProducto());
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
            List<ImagenModel> restantes = obtenerImagenesDelGrupo(productoGrupo);
            if (!restantes.isEmpty()) {
                ImagenModel nuevaPrincipal = restantes.get(0);
                nuevaPrincipal.setEsPrincipal(true);
                imagenRepository.save(nuevaPrincipal);
            }
        }
    }

    @Transactional
    public void eliminarTodasPorProducto(Long productoId) {
        List<ImagenModel> imagenes = obtenerImagenesDelGrupo(obtenerProducto(productoId));
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
