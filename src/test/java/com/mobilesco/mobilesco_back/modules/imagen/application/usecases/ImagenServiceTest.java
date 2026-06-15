package com.mobilesco.mobilesco_back.modules.imagen.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mobilesco.mobilesco_back.modules.color.domain.models.ColorModel;
import com.mobilesco.mobilesco_back.modules.imagen.domain.models.ImagenModel;
import com.mobilesco.mobilesco_back.modules.imagen.infrastructure.in.api.dtos.ImagenCreateDTO;
import com.mobilesco.mobilesco_back.modules.imagen.infrastructure.in.api.dtos.ImagenResponseDTO;
import com.mobilesco.mobilesco_back.modules.imagen.infrastructure.out.persistence.repositories.ImagenRepository;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoModel;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.NotFoundException;

class ImagenServiceTest {

    private ImagenRepository imagenRepository;
    private ProductoRepository productoRepository;
    private AlmacenamientoImagenesService almacenamientoImagenesService;
    private ImagenService service;

    @BeforeEach
    void setUp() {
        imagenRepository = mock(ImagenRepository.class);
        productoRepository = mock(ProductoRepository.class);
        almacenamientoImagenesService = mock(AlmacenamientoImagenesService.class);
        service = new ImagenService(imagenRepository, productoRepository, almacenamientoImagenesService);
    }

    @Test
    void creaLaPrimeraImagenComoPrincipalUsandoSoloElProducto() {
        ProductoModel producto = producto(10L, 1L, 2L);
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(imagenRepository.countByProductoId(10L)).thenReturn(0L);
        when(imagenRepository.save(any(ImagenModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ImagenCreateDTO dto = new ImagenCreateDTO();
        dto.setProductoId(10L);
        dto.setUrl("/uploads/10/imagen.jpg");
        dto.setAltTexto("Frente");

        ImagenResponseDTO response = service.crear(dto);

        ArgumentCaptor<ImagenModel> captor = ArgumentCaptor.forClass(ImagenModel.class);
        verify(imagenRepository).countByProductoId(10L);
        verify(imagenRepository).resetPrincipalFlag(10L);
        verify(imagenRepository).save(captor.capture());
        assertTrue(captor.getValue().getEsPrincipal());
        assertEquals(10L, captor.getValue().getProducto().getId());
        assertEquals(10L, response.getProductoId());
    }

    @Test
    void obtenerPorProductoSoloConsultaLasImagenesDelProducto() {
        ProductoModel producto = producto(10L, 1L, 2L);
        ImagenModel imagen = imagen(1L, producto, true, 0);
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(imagenRepository.findByProductoIdOrderByOrdenAsc(10L)).thenReturn(List.of(imagen));

        List<ImagenResponseDTO> resultado = service.obtenerPorProducto(10L);

        verify(imagenRepository).findByProductoIdOrderByOrdenAsc(10L);
        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).getProductoId());
    }

    @Test
    void eliminarTodasPorProductoNoAfectaOtrasCombinaciones() {
        ProductoModel producto = producto(10L, 1L, 2L);
        ImagenModel imagen1 = imagen(1L, producto, true, 0);
        ImagenModel imagen2 = imagen(2L, producto, false, 1);
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(imagenRepository.findByProductoIdOrderByOrdenAsc(10L)).thenReturn(List.of(imagen1, imagen2));

        service.eliminarTodasPorProducto(10L);

        verify(imagenRepository).deleteAll(List.of(imagen1, imagen2));
    }

    @Test
    void obtenerPorProductoFallaSiNoExisteElProducto() {
        when(productoRepository.findById(10L)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(NotFoundException.class, () -> service.obtenerPorProducto(10L));
    }

    private ProductoModel producto(Long id, Long modeloId, Long colorId) {
        ProductoModel producto = new ProductoModel();
        producto.setId(id);
        producto.setModelo(modelo(modeloId));
        producto.setColor(color(colorId));
        return producto;
    }

    private com.mobilesco.mobilesco_back.modules.modelo.domain.models.ModeloModel modelo(Long id) {
        com.mobilesco.mobilesco_back.modules.modelo.domain.models.ModeloModel modelo =
                new com.mobilesco.mobilesco_back.modules.modelo.domain.models.ModeloModel();
        modelo.setId(id);
        return modelo;
    }

    private ColorModel color(Long id) {
        ColorModel color = new ColorModel();
        color.setId(id);
        return color;
    }

    private ImagenModel imagen(Long id, ProductoModel producto, boolean principal, int orden) {
        ImagenModel imagen = new ImagenModel();
        imagen.setId(id);
        imagen.setProducto(producto);
        imagen.setEsPrincipal(principal);
        imagen.setOrden(orden);
        imagen.setUrl("/uploads/" + producto.getId() + "/" + id + ".jpg");
        return imagen;
    }
}
