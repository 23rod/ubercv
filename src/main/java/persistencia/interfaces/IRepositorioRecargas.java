package persistencia.interfaces;

import java.util.List;
import modelo.RecargaPagoMovil;

/** Contrato de almacenamiento del registro de operaciones de pago móvil (exitosas y fallidas) de cada usuario. */
public interface IRepositorioRecargas {

    /** Anota una operación al final del registro. */
    void agregar(RecargaPagoMovil recarga);

    /** Operaciones del usuario (el correo no distingue mayúsculas), de la más antigua a la más reciente. */
    List<RecargaPagoMovil> listarPorUsuario(String correo);

    /** Correos de los usuarios que tienen al menos una operación, sin repetir y en orden alfabético. */
    List<String> listarCorreos();
}
