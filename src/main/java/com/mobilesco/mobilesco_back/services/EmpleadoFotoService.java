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

    public String subirFotoDelEmpleado(Long empleadoId, MultipartFile archivo) {
        EmpleadoModel empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new NotFoundException("Empleado no encontrado"));

        return guardarYActualizarFoto(empleado, archivo);
    }

    public String subirFotoDelEmpleadoActual(Authentication auth, MultipartFile archivo) {
        String email = auth.getName();
        var usuario = usuarioRepository.findOneByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (usuario.getEmpleado() == null) {
            throw new BadRequestException("Tu usuario no tiene un empleado asociado");
        }

        return guardarYActualizarFoto(usuario.getEmpleado(), archivo);
    }

    private String guardarYActualizarFoto(EmpleadoModel empleado, MultipartFile archivo) {
        try {
            String fotoUrl = almacenamientoImagenesService.guardarFotoPerfilEmpleado(empleado.getId(), archivo);
            empleado.setFotoUrl(fotoUrl);
            empleadoRepository.save(empleado);

            return fotoUrl;
        } catch (IOException e) {
            throw new BadRequestException("No se pudo guardar la imagen. Verifica que sea válida.");
        }
    }
}
