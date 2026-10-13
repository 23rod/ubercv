package persistencia.interfaces;

import java.util.List;
import modelo.Movimiento;

/** Contrato de almacenamiento del historial del monedero. Solo anota y consulta: las reglas viven en el servicio. */
public interface IRepositorioMovimientos {

    /** Anota un movimiento al final del historial. */
    void agregar(Movimiento movimiento);

    /** Movimientos del usuario (el correo no distingue mayúsculas), del más antiguo al más reciente. */
    List<Movimiento> listarPorUsuario(String correo);

    /** Indica si alguna recarga ya usó ese número de referencia de pago móvil. */
    boolean existeReferencia(String referencia);
}
