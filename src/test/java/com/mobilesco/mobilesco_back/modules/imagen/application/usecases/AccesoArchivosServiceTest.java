package com.mobilesco.mobilesco_back.modules.imagen.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.mobilesco.mobilesco_back.modules.auth.domain.models.UsuarioModel;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.UsuarioRepository;
import com.mobilesco.mobilesco_back.modules.empleado.domain.models.EmpleadoModel;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.NotFoundException;

class AccesoArchivosServiceTest {

    @TempDir
    Path uploadsDir;

    private UsuarioRepository usuarioRepository;
    private AccesoArchivosService service;

    @BeforeEach
    void setUp() throws Exception {
        usuarioRepository = org.mockito.Mockito.mock(UsuarioRepository.class);
        service = new AccesoArchivosService(usuarioRepository, uploadsDir.toString());
        Path carpeta = uploadsDir.resolve("empleados/7/perfil");
        Files.createDirectories(carpeta);
        ImageIO.write(
                new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB),
                "jpg",
                carpeta.resolve("foto-segura.jpg").toFile()
        );
    }

    @Test
    void propietarioPuedeConsultarSuFoto() {
        EmpleadoModel empleado = EmpleadoModel.builder().id(7L).build();
        UsuarioModel usuario = new UsuarioModel();
        usuario.setEmpleado(empleado);
        when(usuarioRepository.findOneByEmail("empleado@mobilesco.test"))
                .thenReturn(Optional.of(usuario));
        var auth = new UsernamePasswordAuthenticationToken(
                "empleado@mobilesco.test",
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_EMPLEADO"))
        );

        assertTrue(service.cargarFotoEmpleado(7L, "foto-segura.jpg", auth).recurso().exists());
    }

    @Test
    void otroEmpleadoNoPuedeConsultarLaFoto() {
        EmpleadoModel empleado = EmpleadoModel.builder().id(8L).build();
        UsuarioModel usuario = new UsuarioModel();
        usuario.setEmpleado(empleado);
        when(usuarioRepository.findOneByEmail("otro@mobilesco.test"))
                .thenReturn(Optional.of(usuario));
        var auth = new UsernamePasswordAuthenticationToken(
                "otro@mobilesco.test",
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_EMPLEADO"))
        );

        assertThrows(AccessDeniedException.class,
                () -> service.cargarFotoEmpleado(7L, "foto-segura.jpg", auth));
    }

    @Test
    void administradorPuedeConsultarFotoDeEmpleado() {
        var auth = new UsernamePasswordAuthenticationToken(
                "admin@mobilesco.test",
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        assertTrue(service.cargarFotoEmpleado(7L, "foto-segura.jpg", auth).recurso().exists());
    }

    @Test
    void nombreConTraversalNoSeResuelve() {
        var auth = new UsernamePasswordAuthenticationToken(
                "admin@mobilesco.test",
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        assertThrows(NotFoundException.class,
                () -> service.cargarFotoEmpleado(7L, "../secreto.jpg", auth));
    }

    @Test
    void imagenDeCatalogoValidaSeSirvePublicamenteConTipoReal() throws Exception {
        Path carpeta = uploadsDir.resolve("modelos/15");
        Files.createDirectories(carpeta);
        ImageIO.write(
                new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB),
                "png",
                carpeta.resolve("modelo.png").toFile()
        );

        var archivo = service.cargarImagenModelo(15L, "modelo.png");

        assertTrue(archivo.recurso().exists());
        assertEquals("image/png", archivo.mediaType().toString());
    }

    @Test
    void archivoNoImagenEnCatalogoNoSePublica() throws Exception {
        Path carpeta = uploadsDir.resolve("productos/catalogo/20");
        Files.createDirectories(carpeta);
        Files.writeString(carpeta.resolve("contenido.jpg"), "<script>alert(1)</script>");

        assertThrows(NotFoundException.class,
                () -> service.cargarImagenProducto(20L, "contenido.jpg"));
    }
}
