package com.mobilesco.mobilesco_back.modules.imagen.application.usecases;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

class AlmacenamientoImagenesServiceTest {

    @TempDir
    Path uploadsDir;

    private AlmacenamientoImagenesService service;
    private byte[] pngValido;

    @BeforeEach
    void setUp() throws Exception {
        service = new AlmacenamientoImagenesService();
        ReflectionTestUtils.setField(service, "uploadsDir", uploadsDir.toString());
        ReflectionTestUtils.setField(service, "maxImageBytes", 1024L * 1024);
        ReflectionTestUtils.setField(service, "maxImagePixels", 1_000_000L);
        ReflectionTestUtils.setField(service, "maxFilenameLength", 128);

        BufferedImage imagen = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = imagen.createGraphics();
        graphics.setColor(Color.BLUE);
        graphics.fillRect(0, 0, 8, 8);
        graphics.dispose();
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(imagen, "png", output);
            pngValido = output.toByteArray();
        }
    }

    @Test
    void guardaImagenCuandoNombreTipoYContenidoCoinciden() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo", "catalogo.png", "image/png", pngValido
        );

        String url = service.guardarImagenProducto(42L, archivo);

        assertTrue(url.matches("/uploads/productos/catalogo/42/[a-f0-9-]+\\.jpg"));
        Path guardado = uploadsDir.resolve(url.substring("/uploads/".length()));
        assertTrue(Files.isRegularFile(guardado));
    }

    @Test
    void rechazaTipoDeclaradoQueNoCoincideConContenidoReal() {
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo", "catalogo.jpg", "image/jpeg", pngValido
        );

        assertThrows(IllegalArgumentException.class,
                () -> service.guardarImagenProducto(42L, archivo));
    }

    @Test
    void rechazaNombreConRutaIncrustada() {
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo", "../catalogo.png", "image/png", pngValido
        );

        assertThrows(IllegalArgumentException.class,
                () -> service.guardarImagenProducto(42L, archivo));
    }

    @Test
    void rechazaImagenQueSuperaLimiteConfigurable() {
        ReflectionTestUtils.setField(service, "maxImageBytes", 4L);
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo", "catalogo.png", "image/png", pngValido
        );

        assertThrows(IllegalArgumentException.class,
                () -> service.guardarImagenProducto(42L, archivo));
    }
}
