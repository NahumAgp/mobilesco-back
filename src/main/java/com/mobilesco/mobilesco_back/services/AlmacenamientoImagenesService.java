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

    public String guardarFotoPerfilEmpleado(Long empleadoId, MultipartFile archivo) throws IOException {

        // ✅ Validación fuerte: si Postman no manda el archivo, aquí te enteras
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException(
                    "No se recibió archivo. En Postman usa Body -> form-data, key=archivo (File).");
        }

        String tipo = archivo.getContentType();
        if (tipo == null || !TIPOS_PERMITIDOS.contains(tipo)) {
            throw new IllegalArgumentException("Tipo no permitido. Sube JPG, PNG o WEBP.");
        }

        long maxBytes = 5L * 1024 * 1024;
        if (archivo.getSize() > maxBytes) {
            throw new IllegalArgumentException("La imagen supera el máximo permitido (5MB).");
        }

        // Carpeta física: uploads/empleados/{id}/perfil
        Path carpeta = Paths.get(uploadsDir, "empleados", empleadoId.toString(), "perfil");
        System.out.println("UPLOADS_DIR = " + uploadsDir);
        System.out.println("Carpeta destino absoluta = " + carpeta.toAbsolutePath());
        Files.createDirectories(carpeta);

        // Nombre único
        String nombre = UUID.randomUUID().toString();
        Path destinoJpg = carpeta.resolve(nombre + ".jpg");

        // Leer imagen real
        BufferedImage img = ImageIO.read(archivo.getInputStream());
        if (img == null) {
            throw new IllegalArgumentException("El archivo no es una imagen válida o no es compatible.");
        }

        // Guardar como JPG (seguro)
        Thumbnails.of(img)
                .scale(1.0)
                .outputFormat("jpg")
                .toFile(destinoJpg.toFile());

        // Ruta pública
        return "/uploads/empleados/" + empleadoId + "/perfil/" + destinoJpg.getFileName().toString();
    }
}