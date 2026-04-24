package com.mobilesco.mobilesco_back.services;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.coobird.thumbnailator.Thumbnails;

@Service
public class AlmacenamientoImagenesService {

    @Value("${app.uploads.dir}")
    private String uploadsDir;

    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private void validarArchivoImagen(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException(
                    "No se recibio archivo. En Postman usa Body -> form-data, key=archivo (File)."
            );
        }

        String tipo = archivo.getContentType();
        if (tipo == null || !TIPOS_PERMITIDOS.contains(tipo)) {
            throw new IllegalArgumentException("Tipo no permitido. Sube JPG, PNG o WEBP.");
        }

        long maxBytes = 5L * 1024 * 1024;
        if (archivo.getSize() > maxBytes) {
            throw new IllegalArgumentException("La imagen supera el maximo permitido (5MB).");
        }
    }

    public String guardarFotoPerfilEmpleado(Long empleadoId, MultipartFile archivo) throws IOException {
        validarArchivoImagen(archivo);

        Path carpeta = Paths.get(uploadsDir, "empleados", empleadoId.toString(), "perfil");
        Files.createDirectories(carpeta);

        String nombre = UUID.randomUUID().toString();
        Path destinoJpg = carpeta.resolve(nombre + ".jpg");

        BufferedImage img = ImageIO.read(archivo.getInputStream());
        if (img == null) {
            throw new IllegalArgumentException("El archivo no es una imagen valida o no es compatible.");
        }

        Thumbnails.of(img)
                .scale(1.0)
                .outputFormat("jpg")
                .toFile(destinoJpg.toFile());

        return "/uploads/empleados/" + empleadoId + "/perfil/" + destinoJpg.getFileName().toString();
    }

    public String guardarImagenVariante(Long varianteId, MultipartFile archivo) throws IOException {
        validarArchivoImagen(archivo);

        Path carpeta = Paths.get(uploadsDir, "productos", "variantes", varianteId.toString());
        Files.createDirectories(carpeta);

        String nombre = UUID.randomUUID().toString();
        Path destinoJpg = carpeta.resolve(nombre + ".jpg");

        BufferedImage img = ImageIO.read(archivo.getInputStream());
        if (img == null) {
            throw new IllegalArgumentException("El archivo no es una imagen valida o no es compatible.");
        }

        Thumbnails.of(img)
                .scale(1.0)
                .outputFormat("jpg")
                .toFile(destinoJpg.toFile());

        return "/uploads/productos/variantes/" + varianteId + "/" + destinoJpg.getFileName().toString();
    }
}
