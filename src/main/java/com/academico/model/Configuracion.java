package com.academico.model;

public class Configuracion {
    private String clave;
    private String valor;
    private String descripcion;

    public Configuracion() {}

    public Configuracion(String clave, String valor, String descripcion) {
        this.clave = clave;
        this.valor = valor;
        this.descripcion = descripcion;
    }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }

    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}