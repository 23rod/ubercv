package servicio;

import modelo.Resultado;
import modelo.Rol;

/** Control de acceso por rol. Los servicios de backoffice solo necesitan esto del módulo de usuarios. */
public interface Autorizador {

    /** Éxito si la sesión actual tiene el rol requerido; si no, error con el mensaje de acceso denegado. */
    Resultado<Void> exigirRol(Rol requerido);
}
