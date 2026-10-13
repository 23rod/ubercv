package persistencia.interfaces;

import java.math.BigDecimal;
import modelo.Rol;

/** Contrato de almacenamiento de tarifas: la tarifa base de cada ruta y el porcentaje configurado por tipo de usuario. */
public interface IRepositorioTarifas {

    /** Tarifa base de la ruta, o null si aún no tiene. El nombre no distingue mayúsculas. */
    BigDecimal obtenerTarifaBase(String nombreRuta);

    void guardarTarifaBase(String nombreRuta, BigDecimal monto);

    /** Hace que la tarifa de una ruta siga a la ruta cuando esta cambia de nombre. */
    void moverTarifaBase(String nombreAnterior, String nombreNuevo);

    /** Porcentaje configurado para el tipo de usuario, o null si no se ha configurado (se usa el valor por defecto). */
    BigDecimal obtenerPorcentaje(Rol rol);

    void guardarPorcentaje(Rol rol, BigDecimal porcentaje);
}
