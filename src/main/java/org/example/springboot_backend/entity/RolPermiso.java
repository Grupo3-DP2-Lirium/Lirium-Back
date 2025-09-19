package org.example.springboot_backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "rol_permiso")
public class RolPermiso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRolPermiso;

    @ManyToOne(optional = false)
    @JoinColumn(name = "rol_id")
    private Rol rol;

    @ManyToOne(optional = false)
    @JoinColumn(name = "permiso_id")
    private Permiso permiso;

    public Long getIdRolPermiso() { return idRolPermiso; }
    public void setIdRolPermiso(Long idRolPermiso) { this.idRolPermiso = idRolPermiso; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public Permiso getPermiso() { return permiso; }
    public void setPermiso(Permiso permiso) { this.permiso = permiso; }
}