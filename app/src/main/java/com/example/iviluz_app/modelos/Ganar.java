package com.example.iviluz_app.modelos;

public class Ganar {
    private String id;
    private String nombre;
    private String telefono;
    private String direccion;
    private String peticion;
    private String invitado_por;
    private String servicio;
    private String fecha;

    private String fechaServicio;
    private String estatus;
    private String comentario;
    private String estado;
    private String lider;

    public Ganar() {
    }

    public Ganar(String id, String nombre, String telefono, String direccion, String peticion, String invitado_por, String servicio, String fecha, String fechaServicio, String estatus, String comentario, String estado, String lider ) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
        this.peticion = peticion;
        this.invitado_por = invitado_por;
        this.servicio = servicio;
        this.fecha = fecha;
        this.fechaServicio = fechaServicio;
        this.estatus = estatus;
        this.comentario = comentario;
        this.estado = estado;
        this.lider = lider;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getPeticion() {
        return peticion;
    }

    public void setPeticion(String peticion) {
        this.peticion = peticion;
    }

    public String getInvitado_por() {
        return invitado_por;
    }

    public void setInvitado_por(String invitado_por) {
        this.invitado_por = invitado_por;
    }

    public String getServicio() {
        return servicio;
    }

    public void setServicio(String servicio) {
        this.servicio = servicio;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getLider() {
        return lider;
    }

    public void setLider(String lider) {
        this.lider = lider;
    }

    public String getFechaServicio() {
        return fechaServicio;
    }

    public String setFechaServicio(String fechaServicio) {
        this.fechaServicio = fechaServicio;
        return fechaServicio;
    }
}
