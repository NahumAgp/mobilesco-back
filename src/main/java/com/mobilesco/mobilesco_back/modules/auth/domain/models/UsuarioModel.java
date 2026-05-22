package com.mobilesco.mobilesco_back.modules.auth.domain.models;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.mobilesco.mobilesco_back.modules.empleado.domain.models.EmpleadoModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class UsuarioModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 190)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private boolean locked = false;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 20)
    private EstadoCuentaUsuario estadoCuenta = EstadoCuentaUsuario.PENDING;

    @Column(name = "approved_by_director")
    private String approvedByDirector;

    @Column(name = "approved_by_director_email")
    private String approvedByDirectorEmail;

    @Column(name = "approved_by_director_at")
    private LocalDateTime approvedByDirectorAt;

    @Column(name = "approved_by_dev")
    private String approvedByDev;

    @Column(name = "approved_by_dev_email")
    private String approvedByDevEmail;

    @Column(name = "approved_by_dev_at")
    private LocalDateTime approvedByDevAt;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "invitacion_id", unique = true)
    private InvitacionUsuarioModel invitacion;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RolModel> roles = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "empleado_id", unique = true)
    private EmpleadoModel empleado;

    public EmpleadoModel getEmpleado() {
        return empleado;
    }

    public void setEmpleado(EmpleadoModel empleado) {
        this.empleado = empleado;
    }

    // ====== getters/setters ======
    public Long getId() { return id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public EstadoCuentaUsuario getEstadoCuenta() { return estadoCuenta; }
    public void setEstadoCuenta(EstadoCuentaUsuario estadoCuenta) { this.estadoCuenta = estadoCuenta; }

    public String getApprovedByDirector() { return approvedByDirector; }
    public void setApprovedByDirector(String approvedByDirector) { this.approvedByDirector = approvedByDirector; }

    public String getApprovedByDirectorEmail() { return approvedByDirectorEmail; }
    public void setApprovedByDirectorEmail(String approvedByDirectorEmail) { this.approvedByDirectorEmail = approvedByDirectorEmail; }

    public LocalDateTime getApprovedByDirectorAt() { return approvedByDirectorAt; }
    public void setApprovedByDirectorAt(LocalDateTime approvedByDirectorAt) { this.approvedByDirectorAt = approvedByDirectorAt; }

    public String getApprovedByDev() { return approvedByDev; }
    public void setApprovedByDev(String approvedByDev) { this.approvedByDev = approvedByDev; }

    public String getApprovedByDevEmail() { return approvedByDevEmail; }
    public void setApprovedByDevEmail(String approvedByDevEmail) { this.approvedByDevEmail = approvedByDevEmail; }

    public LocalDateTime getApprovedByDevAt() { return approvedByDevAt; }
    public void setApprovedByDevAt(LocalDateTime approvedByDevAt) { this.approvedByDevAt = approvedByDevAt; }

    public InvitacionUsuarioModel getInvitacion() { return invitacion; }
    public void setInvitacion(InvitacionUsuarioModel invitacion) { this.invitacion = invitacion; }

    public Set<RolModel> getRoles() { return roles; }
    public void setRoles(Set<RolModel> roles) { this.roles = roles; }
}
