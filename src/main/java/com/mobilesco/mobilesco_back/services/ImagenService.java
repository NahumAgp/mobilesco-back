// ============================================
// RUTA: src/main/java/com/mobilesco/mobilesco_back/services/ImagenService.java
// ============================================
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

    // ========== MAPPER ==========
    
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
    
    // ========== CREATE ==========
    
    @Transactional
    public ImagenResponseDTO crear(ImagenCreateDTO dto) {
        
        ProductoModel producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new NotFoundException("Producto no encontrado con ID: " + dto.getProductoId()));
        
        // Si es la primera imagen del producto, forzar como principal
        boolean esPrimeraImagen = imagenRepository.countByProductoId(producto.getId()) == 0;
        
        ImagenModel imagen = new ImagenModel();
        imagen.setUrl(dto.getUrl());
        imagen.setAltTexto(dto.getAltTexto());
        imagen.setProducto(producto);
        
        // Manejar imagen principal
        if (esPrimeraImagen || (dto.getEsPrincipal() != null && dto.getEsPrincipal())) {
            // Resetear flag principal en otras imágenes de este producto
            imagenRepository.resetPrincipalFlag(producto.getId());
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
    
    // ========== READ ==========
    
    public List<ImagenResponseDTO> obtenerPorProducto(Long productoId) {
        if (!productoRepository.existsById(productoId)) {
            throw new NotFoundException("Producto no encontrado con ID: " + productoId);
        }
        return mapToResponseDTOList(imagenRepository.findByProductoIdOrderByOrdenAsc(productoId));
    }
    
    public ImagenResponseDTO obtenerPrincipalPorProducto(Long productoId) {
        ImagenModel imagen = imagenRepository.findByProductoIdAndEsPrincipalTrue(productoId)
                .orElse(null);
        return imagen != null ? mapToResponseDTO(imagen) : null;
    }
    
    public ImagenResponseDTO obtenerPorId(Long id) {
        ImagenModel imagen = imagenRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Imagen no encontrada con ID: " + id));
        return mapToResponseDTO(imagen);
    }
    
    // ========== UPDATE ==========
    
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
        
        // Manejar cambio de imagen principal
        if (dto.getEsPrincipal() != null && dto.getEsPrincipal() && !existente.getEsPrincipal()) {
            // Resetear flag principal en otras imágenes de este producto
            imagenRepository.resetPrincipalFlag(existente.getProducto().getId());
            existente.setEsPrincipal(true);
        } else if (dto.getEsPrincipal() != null && !dto.getEsPrincipal() && existente.getEsPrincipal()) {
            // No permitir desmarcar la única imagen principal
            long totalPrincipales = imagenRepository.findByProductoIdAndEsPrincipalTrue(existente.getProducto().getId()).stream().count();
            if (totalPrincipales <= 1) {
                throw new BadRequestException("No se puede desmarcar la única imagen principal. Debe marcar otra como principal primero.");
            }
            existente.setEsPrincipal(false);
        }
        
        ImagenModel actualizado = imagenRepository.save(existente);
        return mapToResponseDTO(actualizado);
    }
    
    // ========== DELETE ==========
    
    @Transactional
    public void eliminar(Long id) {
        
        ImagenModel imagen = imagenRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Imagen no encontrada con ID: " + id));
        
        Long productoId = imagen.getProducto().getId();
        boolean eraPrincipal = imagen.getEsPrincipal();
        
        imagenRepository.deleteById(id);
        
        // Si se eliminó la imagen principal y quedan otras imágenes, promover la primera como principal
        if (eraPrincipal) {
            List<ImagenModel> restantes = imagenRepository.findByProductoIdOrderByOrdenAsc(productoId);
            if (!restantes.isEmpty()) {
                ImagenModel nuevaPrincipal = restantes.get(0);
                nuevaPrincipal.setEsPrincipal(true);
                imagenRepository.save(nuevaPrincipal);
            }
        }
    }
    
    // ========== BULK DELETE ==========
    
    @Transactional
    public void eliminarTodasPorProducto(Long productoId) {
        List<ImagenModel> imagenes = imagenRepository.findByProductoId(productoId);
        imagenRepository.deleteAll(imagenes);
    }
}
