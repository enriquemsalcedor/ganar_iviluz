package com.example.iviluz_app.modelos;

public class FechaServicio {
    private String id;
    private String fecha;
    private String estatus;


    public FechaServicio(){

    }

    public FechaServicio(String id, String fecha,  String estatus) {
        this.id = id;
        this.fecha = fecha;
        this.estatus = estatus;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

}
