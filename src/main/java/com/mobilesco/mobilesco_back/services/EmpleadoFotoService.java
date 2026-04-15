package com.mobilesco.mobilesco_back.services;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mobilesco.mobilesco_back.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.models.EmpleadoModel;
import com.mobilesco.mobilesco_back.repositories.EmpleadoRepository;
import com.mobilesco.mobilesco_back.repositories.UsuarioRepository;

@Service
public class EmpleadoFotoService {

    private final UsuarioRepository usuarioRepository;
    private final EmpleadoRepository empleadoRepository;
    private final AlmacenamientoImagenesService almacenamientoImagenesService;

    public EmpleadoFotoService(
            UsuarioRepository usuarioRepository,
            EmpleadoRepository empleadoRepository,
            AlmacenamientoImagenesService almacenamientoImagenesService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.empleadoRepository = empleadoRepository;
        this.almacenamientoImagenesService = almacenamientoImagenesService;
    }

    public String subirFotoDelEmpleadoActual(Authentication auth, MultipartFile archivo) {

        // 1) Sacamos el correo del usuario logueado (viene del JWT)
        String email = auth.getName();

        // 2) Buscamos el usuario en BD
        var usuario = usuarioRepository.findOneByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        // 3) Validamos que tenga empleado asociado
        if (usuario.getEmpleado() == null) {
            throw new BadRequestException("Tu usuario no tiene un empleado asociado");
        }

        EmpleadoModel empleado = usuario.getEmpleado();

        try {
            // 4) Guardamos archivo físicamente y obtenemos URL pública /uploads/...
            String fotoUrl = almacenamientoImagenesService.guardarFotoPerfilEmpleado(empleado.getId(), archivo);

            // 5) Guardamos URL en BD
            empleado.setFotoUrl(fotoUrl);
            empleadoRepository.save(empleado);

            return fotoUrl;

        } catch (IOException e) {
            // Si algo falla con disco/imagen, regresamos un error claro
            throw new BadRequestException("No se pudo guardar la imagen. Verifica que sea válida.");
        }
    }
}