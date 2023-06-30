package com.example.iviluz_app.modelos;

public class Inicio {
    private int cantidad_primero;
    private int cantidad_segundo;
    private int cantidad_tercero;

    public Inicio(){

    }

    public Inicio(int cantidad_primero, int cantidad_segundo, int cantidad_tercero) {
        this.cantidad_primero = cantidad_primero;
        this.cantidad_segundo = cantidad_segundo;
        this.cantidad_tercero = cantidad_tercero;
    }

    public int getCantidad_primero() {
        return cantidad_primero;
    }

    public void setCantidad_primero(int cantidad_primero) {
        this.cantidad_primero = cantidad_primero;
    }

    public int getCantidad_segundo() {
        return cantidad_segundo;
    }

    public void setCantidad_segundo(int cantidad_segundo) {
        this.cantidad_segundo = cantidad_segundo;
    }

    public int getCantidad_tercero() {
        return cantidad_tercero;
    }

    public void setCantidad_tercero(int cantidad_tercero) {
        this.cantidad_tercero = cantidad_tercero;
    }
}
