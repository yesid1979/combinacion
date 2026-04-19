package com.combinacion.services;

import com.combinacion.models.Usuario;

/**
 * Clase para el resultado de autenticación.
 */
public class LoginResult {
    private Usuario usuario;
    private String mensaje;
    private boolean exitoso;

    public LoginResult(Usuario usuario, String mensaje, boolean exitoso) {
        this.usuario = usuario;
        this.mensaje = mensaje;
        this.exitoso = exitoso;
    }

    public Usuario getUsuario() { return usuario; }
    public String getMensaje() { return mensaje; }
    public boolean isExitoso() { return exitoso; }
}
