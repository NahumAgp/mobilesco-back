package com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.controllers;

import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mobilesco.mobilesco_back.modules.auth.application.usecases.AccesoService;
import com.mobilesco.mobilesco_back.config.ApiPaths;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.RolModel;
import com.mobilesco.mobilesco_back.modules.auth.domain.models.UsuarioModel;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.InvitacionUsuarioCreateDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.InvitacionUsuarioResponseDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.PermisoResponseDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.RolCreateDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.RolResponseDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.RolUpdateDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.UsuarioAccesoResponseDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.UsuarioAccesoUpdateDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.UsuarioCreateDTO;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.in.api.dtos.UsuarioPendienteResponseDTO;
import com.mobilesco.mobilesco_back.modules.empleado.domain.models.EmpleadoModel;
import com.mobilesco.mobilesco_back.modules.shared.infrastructure.sort.TypeSafeSorts;

import jakarta.validation.Valid;

@RestController
@RequestMapping(ApiPaths.AUTH)
public class AccesoController {

    private final AccesoService accesoService;

    public AccesoController(AccesoService accesoService) {
        this.accesoService = accesoService;
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA')")
    public ResponseEntity<List<String>> roles() {
        return ResponseEntity.ok(accesoService.listarRolesDisponibles());
    }

    @GetMapping("/permisos")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA') or hasAuthority('VIEW_USERS')")
    public ResponseEntity<List<PermisoResponseDTO>> permisos() {
        return ResponseEntity.ok(accesoService.listarPermisos());
    }

    @GetMapping("/roles-config")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA') or hasAuthority('VIEW_USERS')")
    public ResponseEntity<?> rolesConfig(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        if (page != null) {
            int pageNumber = Math.max(page, 0);
            int pageSize = Math.max(size == null ? 10 : size, 1);
            PageRequest pageable = PageRequest.of(pageNumber, pageSize, construirSortRoles(sortBy, direction));
            return ResponseEntity.ok(accesoService.listarRolesDetallePaginado(busqueda, pageable));
        }
        return ResponseEntity.ok(accesoService.listarRolesDetalle());
    }

    @PostMapping("/roles-config")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA') or hasAuthority('ACTION_USERS_WRITE')")
    public ResponseEntity<RolResponseDTO> crearRol(@Valid @RequestBody RolCreateDTO dto, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accesoService.crearRol(dto, authentication.getName()));
    }

    @PatchMapping("/roles-config/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA') or hasAuthority('ACTION_USERS_WRITE')")
    public ResponseEntity<RolResponseDTO> actualizarRol(
            @PathVariable Long id,
            @Valid @RequestBody RolUpdateDTO dto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(accesoService.actualizarRol(id, dto, authentication.getName()));
    }

    @GetMapping("/usuarios")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA') or hasAuthority('VIEW_USERS')")
    public ResponseEntity<?> usuarios(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "correo") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        if (page != null) {
            int pageNumber = Math.max(page, 0);
            int pageSize = Math.max(size == null ? 10 : size, 1);
            PageRequest pageable = PageRequest.of(pageNumber, pageSize, construirSortUsuarios(sortBy, direction));
            return ResponseEntity.ok(accesoService.listarUsuariosPaginado(busqueda, pageable));
        }
        return ResponseEntity.ok(accesoService.listarUsuarios());
    }

    @PostMapping("/usuarios")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA') or hasAuthority('ACTION_USERS_WRITE')")
    public ResponseEntity<UsuarioAccesoResponseDTO> crearUsuario(
            @Valid @RequestBody UsuarioCreateDTO dto,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accesoService.crearUsuario(dto, authentication.getName()));
    }

    @PatchMapping("/usuarios/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA') or hasAuthority('ACTION_USERS_WRITE')")
    public ResponseEntity<UsuarioAccesoResponseDTO> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioAccesoUpdateDTO dto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(accesoService.actualizarUsuario(id, dto, authentication.getName()));
    }

    @PostMapping("/usuarios/{id}/desactivar")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA') or hasAuthority('ACTION_USERS_WRITE')")
    public ResponseEntity<UsuarioAccesoResponseDTO> desactivarUsuario(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(accesoService.desactivarUsuario(id, authentication.getName()));
    }

    @PostMapping("/invitaciones")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA')")
    public ResponseEntity<InvitacionUsuarioResponseDTO> crearInvitacion(
            @Valid @RequestBody InvitacionUsuarioCreateDTO dto,
            Authentication authentication
    ) {
        String creador = authentication != null ? authentication.getName() : "sistema";
        return ResponseEntity.status(HttpStatus.CREATED).body(accesoService.crearInvitacion(dto, creador));
    }

    @GetMapping("/usuarios-pendientes")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA')")
    public ResponseEntity<?> pendientes(
            @RequestParam(required = false) Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        if (page != null) {
            int pageNumber = Math.max(page, 0);
            int pageSize = Math.max(size == null ? 10 : size, 1);
            PageRequest pageable = PageRequest.of(
                    pageNumber,
                    pageSize,
                    TypeSafeSorts.asc(UsuarioModel.class, UsuarioModel::getEmail));
            return ResponseEntity.ok(accesoService.listarPendientesPaginado(pageable));
        }
        return ResponseEntity.ok(accesoService.listarPendientes());
    }

    @PostMapping("/usuarios-pendientes/{id}/aprobar")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR_GENERAL','SUBDIRECCION_ADMINISTRATIVA')")
    public ResponseEntity<UsuarioPendienteResponseDTO> aprobar(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(accesoService.aprobarUsuario(id, authentication.getName()));
    }

    private Sort construirSortRoles(String sortBy, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return switch ((sortBy == null ? "" : sortBy).toLowerCase(Locale.ROOT)) {
            case "descripcion" -> sortDirection == Sort.Direction.DESC
                    ? TypeSafeSorts.desc(RolModel.class, RolModel::getDescripcion)
                    : TypeSafeSorts.asc(RolModel.class, RolModel::getDescripcion);
            case "sistema", "tipo" -> sortDirection == Sort.Direction.DESC
                    ? TypeSafeSorts.desc(RolModel.class, RolModel::isSistema)
                    : TypeSafeSorts.asc(RolModel.class, RolModel::isSistema);
            default -> sortDirection == Sort.Direction.DESC
                    ? TypeSafeSorts.desc(RolModel.class, RolModel::getName)
                    : TypeSafeSorts.asc(RolModel.class, RolModel::getName);
        };
    }

    private Sort construirSortUsuarios(String sortBy, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return switch ((sortBy == null ? "" : sortBy).toLowerCase(Locale.ROOT)) {
            case "nombre" -> sortDirection == Sort.Direction.DESC
                    ? TypeSafeSorts.descNestedWithId(UsuarioModel.class, UsuarioModel::getEmpleado, EmpleadoModel::getNombre, UsuarioModel::getId)
                    : TypeSafeSorts.ascNestedWithId(UsuarioModel.class, UsuarioModel::getEmpleado, EmpleadoModel::getNombre, UsuarioModel::getId);
            case "apellidopaterno" -> sortDirection == Sort.Direction.DESC
                    ? TypeSafeSorts.descNestedWithId(UsuarioModel.class, UsuarioModel::getEmpleado, EmpleadoModel::getApellidoPaterno, UsuarioModel::getId)
                    : TypeSafeSorts.ascNestedWithId(UsuarioModel.class, UsuarioModel::getEmpleado, EmpleadoModel::getApellidoPaterno, UsuarioModel::getId);
            case "enabled" -> sortDirection == Sort.Direction.DESC
                    ? TypeSafeSorts.desc(UsuarioModel.class, UsuarioModel::isEnabled)
                    : TypeSafeSorts.asc(UsuarioModel.class, UsuarioModel::isEnabled);
            case "locked" -> sortDirection == Sort.Direction.DESC
                    ? TypeSafeSorts.desc(UsuarioModel.class, UsuarioModel::isLocked)
                    : TypeSafeSorts.asc(UsuarioModel.class, UsuarioModel::isLocked);
            case "lastloginat" -> sortDirection == Sort.Direction.DESC
                    ? TypeSafeSorts.desc(UsuarioModel.class, UsuarioModel::getLastLoginAt)
                    : TypeSafeSorts.asc(UsuarioModel.class, UsuarioModel::getLastLoginAt);
            default -> sortDirection == Sort.Direction.DESC
                    ? TypeSafeSorts.desc(UsuarioModel.class, UsuarioModel::getEmail)
                    : TypeSafeSorts.asc(UsuarioModel.class, UsuarioModel::getEmail);
        };
    }
}
