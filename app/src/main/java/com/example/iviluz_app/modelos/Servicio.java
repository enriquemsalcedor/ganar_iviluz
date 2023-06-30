package com.example.iviluz_app.modelos;

public class Servicio {
    private String nombre;
    private String numero;
    private String estatus;

    public Servicio(){

    }

    public Servicio(String nombre, String numero, String estatus) {
        this.nombre = nombre;
        this.numero = numero;
        this.estatus = estatus;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

}
