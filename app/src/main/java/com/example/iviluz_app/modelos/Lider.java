package com.example.iviluz_app.modelos;

public class Lider {
    private String id;
    private String nombre;
    private String telefono;
    private String email;
    private String estatus;
    private String lider;
    private String nivel;
    private String ganar;
    private String coordGanar;
    private String movilizacion;

    public Lider() {
    }

    public Lider(String id, String nombre, String telefono, String email, String estatus, String lider, String nivel
            , String ganar, String coordGanar, String movilizacion) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.estatus = estatus;
        this.lider = lider;
        this.nivel = nivel;
        this.ganar = ganar;
        this.coordGanar = coordGanar;
        this.movilizacion = movilizacion;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    public String getLider() {
        return lider;
    }

    public void setLider(String lider) {
        this.lider = lider;
    }

    public String getGanar() {
        return ganar;
    }

    public void setGanar(String ganar) {
        this.ganar = ganar;
    }

    public String getCoordGanar() {
        return coordGanar;
    }

    public void setCoordGanar(String coordGanar) {
        this.coordGanar = coordGanar;
    }

    public String getMovilizacion() {
        return movilizacion;
    }

    public void setMovilizacion(String movilizacion) {
        this.movilizacion = movilizacion;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }
}
