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
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.coobird.thumbnailator.Thumbnails;

@Service
public class AlmacenamientoImagenesService {

    @Value("${app.uploads.dir}")
    private String uploadsDir;

    @Value("${app.uploads.max-image-bytes:20971520}")
    private long maxImageBytes;

    @Value("${app.uploads.max-image-pixels:25000000}")
    private long maxImagePixels;

    @Value("${app.uploads.max-filename-length:128}")
    private int maxFilenameLength;

    private static final Map<String, String> FORMATOS_PERMITIDOS = Map.of(
            "jpeg", "image/jpeg",
            "png", "image/png",
            "webp", "image/webp"
    );
    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Pattern NOMBRE_SEGURO = Pattern.compile(
            "[\\p{L}\\p{N}][\\p{L}\\p{N} ._()-]*"
    );

    private BufferedImage leerImagenValidada(MultipartFile archivo) throws IOException {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException(
                    "No se recibio archivo. En Postman usa Body -> form-data, key=archivo (File)."
            );
        }

        String nombreOriginal = archivo.getOriginalFilename();
        validarNombre(nombreOriginal);

        String tipoDeclarado = archivo.getContentType();
        if (tipoDeclarado == null || !FORMATOS_PERMITIDOS.containsValue(tipoDeclarado.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Tipo no permitido. Sube JPG, PNG o WEBP.");
        }

        if (archivo.getSize() > maxImageBytes) {
            throw new IllegalArgumentException("La imagen supera el maximo permitido.");
        }

        try (ImageInputStream input = ImageIO.createImageInputStream(archivo.getInputStream())) {
            if (input == null) {
                throw new IllegalArgumentException("No se pudo leer el archivo.");
            }

            var lectores = ImageIO.getImageReaders(input);
            if (!lectores.hasNext()) {
                throw new IllegalArgumentException("El contenido real no es una imagen JPG, PNG o WEBP valida.");
            }

            ImageReader lector = lectores.next();
            try {
                lector.setInput(input, true, true);
                String formatoReal = normalizarFormato(lector.getFormatName());
                String tipoReal = FORMATOS_PERMITIDOS.get(formatoReal);
                if (tipoReal == null) {
                    throw new IllegalArgumentException("El formato real de la imagen no esta permitido.");
                }
                if (!tipoReal.equals(tipoDeclarado.toLowerCase(Locale.ROOT))) {
                    throw new IllegalArgumentException("El tipo declarado no coincide con el contenido real.");
                }
                validarExtension(nombreOriginal, formatoReal);

                int ancho = lector.getWidth(0);
                int alto = lector.getHeight(0);
                if (ancho <= 0 || alto <= 0 || (long) ancho * alto > maxImagePixels) {
                    throw new IllegalArgumentException("Las dimensiones de la imagen superan el limite permitido.");
                }

                BufferedImage imagen = lector.read(0);
                if (imagen == null) {
                    throw new IllegalArgumentException("El archivo no es una imagen valida o compatible.");
                }
                return imagen;
            } finally {
                lector.dispose();
            }
        }
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank() || nombre.length() > maxFilenameLength) {
            throw new IllegalArgumentException("El nombre del archivo es invalido.");
        }
        if (!nombre.equals(Paths.get(nombre).getFileName().toString())
                || nombre.contains("/") || nombre.contains("\\")
                || !NOMBRE_SEGURO.matcher(nombre).matches()) {
            throw new IllegalArgumentException("El nombre del archivo contiene caracteres o rutas no permitidas.");
        }
    }

    private void validarExtension(String nombre, String formatoReal) {
        int punto = nombre.lastIndexOf('.');
        if (punto <= 0 || punto == nombre.length() - 1) {
            throw new IllegalArgumentException("El archivo debe incluir una extension valida.");
        }
        String extension = nombre.substring(punto + 1).toLowerCase(Locale.ROOT);
        if (!EXTENSIONES_PERMITIDAS.contains(extension)
                || ("jpeg".equals(formatoReal) && !Set.of("jpg", "jpeg").contains(extension))
                || (!"jpeg".equals(formatoReal) && !formatoReal.equals(extension))) {
            throw new IllegalArgumentException("La extension no coincide con el contenido real de la imagen.");
        }
    }

    private String normalizarFormato(String formato) {
        String normalizado = formato.toLowerCase(Locale.ROOT);
        return "jpg".equals(normalizado) ? "jpeg" : normalizado;
    }

    public String guardarFotoPerfilEmpleado(Long empleadoId, MultipartFile archivo) throws IOException {
        BufferedImage img = leerImagenValidada(archivo);

        Path carpeta = Paths.get(uploadsDir, "empleados", empleadoId.toString(), "perfil");
        Files.createDirectories(carpeta);

        String nombre = UUID.randomUUID().toString();
        Path destinoJpg = carpeta.resolve(nombre + ".jpg");

        Thumbnails.of(img)
                .scale(1.0)
                .outputFormat("jpg")
                .toFile(destinoJpg.toFile());

        return "/uploads/empleados/" + empleadoId + "/perfil/" + destinoJpg.getFileName().toString();
    }

    public String guardarImagenProducto(Long productoId, MultipartFile archivo) throws IOException {
        BufferedImage img = leerImagenValidada(archivo);

        Path carpeta = Paths.get(uploadsDir, "productos", "catalogo", productoId.toString());
        Files.createDirectories(carpeta);

        String nombre = UUID.randomUUID().toString();
        Path destinoJpg = carpeta.resolve(nombre + ".jpg");

        Thumbnails.of(img)
                .scale(1.0)
                .outputFormat("jpg")
                .toFile(destinoJpg.toFile());

        return "/uploads/productos/catalogo/" + productoId + "/" + destinoJpg.getFileName().toString();
    }

    public String guardarImagenModelo(Long modeloId, MultipartFile archivo) throws IOException {
        BufferedImage img = leerImagenValidada(archivo);

        Path carpeta = Paths.get(uploadsDir, "modelos", modeloId.toString());
        Files.createDirectories(carpeta);

        String nombre = UUID.randomUUID().toString();
        Path destinoJpg = carpeta.resolve(nombre + ".jpg");

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
