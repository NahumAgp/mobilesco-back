package com.mobilesco.mobilesco_back.modules.imagen.application.usecases;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
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

        long maxBytes = 20L * 1024 * 1024;
        if (archivo.getSize() > maxBytes) {
            throw new IllegalArgumentException("La imagen supera el maximo permitido (20MB).");
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

    public String guardarImagenProducto(Long productoId, MultipartFile archivo) throws IOException {
        validarArchivoImagen(archivo);

        Path carpeta = Paths.get(uploadsDir, "productos", "catalogo", productoId.toString());
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

        return "/uploads/productos/catalogo/" + productoId + "/" + destinoJpg.getFileName().toString();
    }

    public String guardarImagenModelo(Long modeloId, MultipartFile archivo) throws IOException {
        validarArchivoImagen(archivo);

        Path carpeta = Paths.get(uploadsDir, "modelos", modeloId.toString());
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

        return "/uploads/modelos/" + modeloId + "/" + destinoJpg.getFileName().toString();
    }

    public void eliminarImagenPublica(String urlPublica) throws IOException {
        Path archivo = resolverRutaPublica(urlPublica);
        if (archivo != null) {
            Files.deleteIfExists(archivo);
        }
    }

    public void eliminarCarpetaImagenesProducto(Long productoId) throws IOException {
        if (productoId == null) {
            return;
        }

        Path carpeta = uploadsRoot()
                .resolve(Paths.get("productos", "catalogo", productoId.toString()))
                .normalize();
        validarRutaDentroDeUploads(carpeta);

        if (!Files.exists(carpeta)) {
            return;
        }

        try (var rutas = Files.walk(carpeta)) {
            rutas.sorted(Comparator.reverseOrder())
                    .forEach(ruta -> {
                        try {
                            Files.deleteIfExists(ruta);
                        } catch (IOException e) {
                            throw new RuntimeException("No se pudo eliminar la ruta: " + ruta, e);
                        }
                    });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException ioException) {
                throw ioException;
            }
            throw e;
        }
    }

    private Path resolverRutaPublica(String urlPublica) {
        if (urlPublica == null || urlPublica.isBlank()) {
            return null;
        }

        String path = urlPublica.trim();
        if (path.startsWith("http://") || path.startsWith("https://")) {
            path = URI.create(path).getPath();
        }

        String prefijo = "/uploads/";
        if (path.startsWith(prefijo)) {
            path = path.substring(prefijo.length());
        } else if (path.startsWith("uploads/")) {
            path = path.substring("uploads/".length());
        } else {
            return null;
        }

        String pathDecodificado = URLDecoder.decode(path, StandardCharsets.UTF_8);
        Path archivo = uploadsRoot().resolve(pathDecodificado).normalize();
        validarRutaDentroDeUploads(archivo);
        return archivo;
    }

    private Path uploadsRoot() {
        return Paths.get(uploadsDir).toAbsolutePath().normalize();
    }

    private void validarRutaDentroDeUploads(Path ruta) {
        if (!ruta.startsWith(uploadsRoot())) {
            throw new IllegalArgumentException("Ruta de imagen fuera del directorio de uploads.");
        }
    }
}
