package servicio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import modelo.Mensajes;
import modelo.Resultado;
import modelo.Rol;
import modelo.Ruta;
import modelo.TipoRuta;
import persistencia.interfaces.IRepositorioRutas;
import persistencia.interfaces.IRepositorioTarifas;
import utilidades.Dinero;
import utilidades.validadores.Validador;

/**
 * HU11: cobro solidario. El administrador fija la tarifa base municipal (T_base) de cada ruta extraurbana
 * y el sistema calcula lo que paga cada tipo de usuario como un porcentaje de esa tarifa:
 * Estudiantes entre 10 % y 20 %, Empleados/Profesores entre 30 % y 60 %, Público General 100 %.
 * Si el administrador no configura porcentajes se usan los valores por defecto (15 %, 45 % y 100 %).
 */
public class ServicioTarifas {

    private static final BigDecimal CIEN = new BigDecimal("100");

    private final IRepositorioTarifas tarifas;
    private final IRepositorioRutas rutas;
    private final Autorizador autorizador;

    public ServicioTarifas(IRepositorioTarifas tarifas, IRepositorioRutas rutas, Autorizador autorizador) {
        this.tarifas = tarifas;
        this.rutas = rutas;
        this.autorizador = autorizador;
    }

    // ------------------------------------------------------------ reglas de la escala (porcentajes)
    public static BigDecimal minimo(Rol rol) {
        return (rol == Rol.ESTUDIANTE) ? new BigDecimal("10") : new BigDecimal("30");
    }

    public static BigDecimal maximo(Rol rol) {
        return (rol == Rol.ESTUDIANTE) ? new BigDecimal("20") : new BigDecimal("60");
    }

    private static BigDecimal porDefecto(Rol rol) {
        return (rol == Rol.ESTUDIANTE) ? new BigDecimal("15") : new BigDecimal("45");
    }

    private static String nombre(Rol rol) {
        return (rol == Rol.ESTUDIANTE) ? "Estudiante" : "Empleado";
    }

    /** Porcentaje que paga el tipo de usuario (cero si ese tipo de usuario no paga tarifa, como conductores y administradores). */
    public BigDecimal porcentajeDe(Rol rol) {
        if (rol == Rol.PUBLICO_GENERAL) {
            return CIEN;
        }
        if (rol != Rol.ESTUDIANTE && rol != Rol.EMPLEADO) {
            return BigDecimal.ZERO;
        }
        BigDecimal configurado = tarifas.obtenerPorcentaje(rol);
        return (configurado != null) ? configurado : porDefecto(rol);
    }

    /** Cambia el porcentaje de Estudiantes o Empleados dentro de su rango permitido. */
    public Resultado<Void> configurarPorcentaje(Rol rol, String porcentaje) {
        Resultado<Void> acceso = autorizador.exigirRol(Rol.ADMIN_TRANSPORTE);
        if (!acceso.isOk()) {
            return acceso;
        }
        if (rol == Rol.PUBLICO_GENERAL) {
            return Resultado.error(Mensajes.PORCENTAJE_PUBLICO_FIJO);
        }
        if (rol != Rol.ESTUDIANTE && rol != Rol.EMPLEADO) {
            return Resultado.error(Mensajes.ROL_SIN_TARIFA);
        }
        BigDecimal valor = Dinero.parsearDecimal(porcentaje);
        if (valor == null) {
            return Resultado.error(Mensajes.PORCENTAJE_INVALIDO);
        }
        if (valor.compareTo(minimo(rol)) < 0 || valor.compareTo(maximo(rol)) > 0) {
            return Resultado.error(String.format(Mensajes.PORCENTAJE_FUERA_DE_RANGO,
                    nombre(rol), minimo(rol).toPlainString(), maximo(rol).toPlainString()));
        }
        tarifas.guardarPorcentaje(rol, valor);
        return Resultado.exito(null, Mensajes.PORCENTAJE_ACTUALIZADO);
    }

    // ------------------------------------------------------------ tarifa base por ruta
    public List<Ruta> rutasExtraurbanas() {
        List<Ruta> resultado = new ArrayList<>();
        for (Ruta ruta : rutas.listar()) {
            if (ruta.getTipo() == TipoRuta.EXTRAURBANA) {
                resultado.add(ruta);
            }
        }
        return resultado;
    }

    /** HU11 escenario 1: registra la tarifa base de una ruta extraurbana que aún no tiene. */
    public Resultado<Void> registrarTarifaBase(String nombreRuta, String monto) {
        return guardarTarifaBase(nombreRuta, monto, false);
    }

    /** HU11 escenario 2: actualiza la tarifa base; los montos de las reservas futuras se calculan con el valor nuevo. */
    public Resultado<Void> actualizarTarifaBase(String nombreRuta, String monto) {
        return guardarTarifaBase(nombreRuta, monto, true);
    }

    private Resultado<Void> guardarTarifaBase(String nombreRuta, String monto, boolean actualizar) {
        Resultado<Void> acceso = autorizador.exigirRol(Rol.ADMIN_TRANSPORTE);
        if (!acceso.isOk()) {
            return acceso;
        }
        if (rutasExtraurbanas().isEmpty()) {
            return Resultado.error(Mensajes.SIN_RUTAS_EXTRAURBANAS);
        }
        if (!Validador.esTextoNoVacio(nombreRuta)) {
            return Resultado.error(Mensajes.RUTA_REQUERIDA);
        }
        Ruta ruta = rutas.buscarPorNombre(nombreRuta);
        if (ruta == null) {
            return Resultado.error(Mensajes.RUTA_NO_ENCONTRADA);
        }
        if (ruta.getTipo() != TipoRuta.EXTRAURBANA) {
            return Resultado.error(Mensajes.RUTA_NO_EXTRAURBANA);
        }
        BigDecimal valor = Dinero.parsearDecimal(monto);
        if (valor == null || valor.signum() <= 0) {
            return Resultado.error(Mensajes.TARIFA_INVALIDA);
        }
        boolean existe = tarifas.obtenerTarifaBase(ruta.getNombre()) != null;
        if (actualizar && !existe) {
            return Resultado.error(Mensajes.TARIFA_NO_REGISTRADA);
        }
        if (!actualizar && existe) {
            return Resultado.error(Mensajes.TARIFA_YA_REGISTRADA);
        }
        tarifas.guardarTarifaBase(ruta.getNombre(), valor);
        return Resultado.exito(null, actualizar ? Mensajes.TARIFA_ACTUALIZADA : Mensajes.TARIFA_REGISTRADA);
    }

    /** Tarifa base de la ruta, o null si no tiene. */
    public BigDecimal tarifaBase(String nombreRuta) {
        return tarifas.obtenerTarifaBase(nombreRuta);
    }

    // ------------------------------------------------------------ cálculo del cobro
    /**
     * Lo que paga el tipo de usuario por viajar en la ruta. Es 0.00 (no se cobra) si la ruta no es extraurbana,
     * si no tiene tarifa configurada o si ese tipo de usuario no paga.
     */
    public BigDecimal montoPara(Rol rol, String nombreRuta) {
        Ruta ruta = (nombreRuta == null) ? null : rutas.buscarPorNombre(nombreRuta);
        if (ruta == null || ruta.getTipo() != TipoRuta.EXTRAURBANA) {
            return Dinero.CERO;
        }
        BigDecimal base = tarifas.obtenerTarifaBase(ruta.getNombre());
        BigDecimal porcentaje = porcentajeDe(rol);
        if (base == null || porcentaje.signum() == 0) {
            return Dinero.CERO;
        }
        return Dinero.porcentajeDe(base, porcentaje);
    }

    /** Montos de la ruta por tipo de usuario, en el orden de la tabla: Estudiante, Empleado, Público General. */
    public Map<Rol, BigDecimal> desglose(String nombreRuta) {
        Map<Rol, BigDecimal> montos = new LinkedHashMap<>();
        montos.put(Rol.ESTUDIANTE, montoPara(Rol.ESTUDIANTE, nombreRuta));
        montos.put(Rol.EMPLEADO, montoPara(Rol.EMPLEADO, nombreRuta));
        montos.put(Rol.PUBLICO_GENERAL, montoPara(Rol.PUBLICO_GENERAL, nombreRuta));
        return montos;
    }
}
