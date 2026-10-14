package servicio;

import modelo.Rol;
import modelo.Usuario;

/** Quién tiene la sesión iniciada. Los servicios que actúan "en nombre del usuario" (como recargar su monedero) solo necesitan esto. */
public interface SesionActual {

    /** Usuario con sesión iniciada, o null si no hay sesión. */
    Usuario getUsuarioSesion();

    /** Rol de la sesión; {@code Rol.NINGUNO} si no hay sesión. */
    Rol getRolSesion();
}
