package com.sira.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class LoginResponse {
    private String token;
    private String tipo = "Bearer";
    private Integer id;
    private String nombre;
    private String email;
    private String rol;
    private boolean requiereCambioPassword;

    public LoginResponse(String token, Integer id, String nombre, String email, String rol, boolean requiereCambioPassword) {
        this.token = token;
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.requiereCambioPassword = requiereCambioPassword;
    }
}
