package com.example.iviluz_app.modelos;

public class Ministerio {

    private String id;
    private String nombre;
    private String lider1;
    private String lider2;

    public Ministerio(){}

    public Ministerio(String id, String nombre, String lider1, String lider2) {
        this.id = id;
        this.nombre = nombre;
        this.lider1 = lider1;
        this.lider2 = lider2;
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

    public String getLider1() {
        return lider1;
    }

    public void setLider1(String lider1) {
        this.lider1 = lider1;
    }

    public String getLider2() {
        return lider2;
    }

    public void setLider2(String lider2) {
        this.lider2 = lider2;
    }
}
