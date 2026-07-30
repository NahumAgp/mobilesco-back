package com.mobilesco.mobilesco_back.modules.imagen.application.usecases;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.UsuarioRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.NotFoundException;

@Service
public class AccesoArchivosService {

    private static final Pattern NOMBRE_ALMACENADO = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{0,127}");
    private static final Map<String, MediaType> TIPOS_IMAGEN = Map.of(
            "jpeg", MediaType.IMAGE_JPEG,
            "png", MediaType.IMAGE_PNG,
            "webp", MediaType.parseMediaType("image/webp")
    );
    private static final Set<String> ROLES_GESTION_EMPLEADOS = Set.of(
            "ROLE_ADMIN",
            "ROLE_SUPER_ADMIN",
            "ROLE_DIRECTOR_GENERAL",
            "ROLE_SUBDIRECCION_ADMINISTRATIVA"
    );

    private final UsuarioRepository usuarioRepository;
    private final Path uploadsRoot;

    public AccesoArchivosService(
            UsuarioRepository usuarioRepository,
            @Value("${app.uploads.dir}") String uploadsDir
    ) {
        this.usuarioRepository = usuarioRepository;
        this.uploadsRoot = Paths.get(uploadsDir).toAbsolutePath().normalize();
    }

    public ArchivoDescargable cargarFotoEmpleado(
            Long empleadoId,
            String nombreArchivo,
            Authentication authentication
    ) {
        if (empleadoId == null || empleadoId <= 0 || !esNombreAlmacenadoSeguro(nombreArchivo)) {
            throw new NotFoundException("Archivo no encontrado");
        }

        autorizarFotoEmpleado(empleadoId, authentication);
        return cargarImagen(Paths.get("empleados", empleadoId.toString(), "perfil", nombreArchivo));
    }

    public ArchivoDescargable cargarImagenModelo(Long modeloId, String nombreArchivo) {
        validarIdentificadorYNombre(modeloId, nombreArchivo);
        return cargarImagen(Paths.get("modelos", modeloId.toString(), nombreArchivo));
    }

    public ArchivoDescargable cargarImagenProducto(Long productoId, String nombreArchivo) {
        validarIdentificadorYNombre(productoId, nombreArchivo);
        return cargarImagen(Paths.get("productos", "catalogo", productoId.toString(), nombreArchivo));
    }

    private void validarIdentificadorYNombre(Long id, String nombreArchivo) {
        if (id == null || id <= 0 || !esNombreAlmacenadoSeguro(nombreArchivo)) {
            throw new NotFoundException("Archivo no encontrado");
        }
    }

    private ArchivoDescargable cargarImagen(Path rutaRelativa) {
        Path candidata = uploadsRoot.resolve(rutaRelativa).normalize();
        if (!candidata.startsWith(uploadsRoot)) {
            throw new NotFoundException("Archivo no encontrado");
        }

        try {
            Path raizReal = uploadsRoot.toRealPath();
            Path archivoReal = candidata.toRealPath();
            if (!archivoReal.startsWith(raizReal) || !Files.isRegularFile(archivoReal)) {
                throw new NotFoundException("Archivo no encontrado");
            }
            return new ArchivoDescargable(
                    new FileSystemResource(archivoReal),
                    detectarTipoReal(archivoReal)
            );
        } catch (IOException e) {
            throw new NotFoundException("Archivo no encontrado");
        }
    }

    private MediaType detectarTipoReal(Path archivo) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(archivo.toFile())) {
            if (input == null) {
                throw new NotFoundException("Archivo no encontrado");
            }
            var lectores = ImageIO.getImageReaders(input);
            if (!lectores.hasNext()) {
                throw new NotFoundException("Archivo no encontrado");
            }
            ImageReader lector = lectores.next();
            try {
                String formato = lector.getFormatName().toLowerCase(Locale.ROOT);
                if ("jpg".equals(formato)) {
                    formato = "jpeg";
                }
                MediaType tipo = TIPOS_IMAGEN.get(formato);
                if (tipo == null) {
                    throw new NotFoundException("Archivo no encontrado");
                }
                return tipo;
            } finally {
                lector.dispose();
            }
        }
    }

    private void autorizarFotoEmpleado(Long empleadoId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Autenticacion requerida");
        }

        boolean puedeGestionar = authentication.getAuthorities().stream()
                .anyMatch(authority -> ROLES_GESTION_EMPLEADOS.contains(authority.getAuthority()));
        if (puedeGestionar) {
            return;
        }

        boolean esPropietario = usuarioRepository.findOneByEmail(authentication.getName())
                .map(usuario -> usuario.getEmpleado() != null
                        && empleadoId.equals(usuario.getEmpleado().getId()))
                .orElse(false);
        if (!esPropietario) {
            throw new AccessDeniedException("No tienes permiso para consultar esta foto");
        }
    }

    private boolean esNombreAlmacenadoSeguro(String nombreArchivo) {
        return nombreArchivo != null
                && NOMBRE_ALMACENADO.matcher(nombreArchivo).matches()
                && !".".equals(nombreArchivo)
                && !"..".equals(nombreArchivo);
    }

    public record ArchivoDescargable(Resource recurso, MediaType mediaType) {
    }
}
