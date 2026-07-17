package com.mobilesco.mobilesco_back.modules.subfamilia.infrastructure.in.api.dtos;

import java.time.LocalDateTime;

public class SubfamiliaResponseDTO {

    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Boolean activo;
    private LocalDateTime createdAt;
    private Long familiaId;
    private String familiaNombre;
    private Long lineaId;
    private String lineaNombre;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getFamiliaId() { return familiaId; }
    public void setFamiliaId(Long familiaId) { this.familiaId = familiaId; }
    public String getFamiliaNombre() { return familiaNombre; }
    public void setFamiliaNombre(String familiaNombre) { this.familiaNombre = familiaNombre; }
    public Long getLineaId() { return lineaId; }
    public void setLineaId(Long lineaId) { this.lineaId = lineaId; }
    public String getLineaNombre() { return lineaNombre; }
    public void setLineaNombre(String lineaNombre) { this.lineaNombre = lineaNombre; }
}
