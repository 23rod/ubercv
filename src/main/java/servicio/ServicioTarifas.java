package servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
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
 * HU11: cobro solidario. El administrador fija la tarifa base municipal (T_base >= 10.00, sin límite superior)
 * de cada ruta extraurbana y el sistema calcula lo que paga cada tipo de usuario como un porcentaje de esa tarifa:
 * Estudiantes entre 10 % y 20 % (por defecto 15 %), Empleados/Profesores entre 30 % y 60 % (por defecto 45 %),
 * Público General 100 %. Conductores y administradores no pagan (0.00).
 */
public class ServicioTarifas {

    private static final BigDecimal CIEN = new BigDecimal("100");
    public static final BigDecimal TARIFA_BASE_MINIMA = new BigDecimal("10.00");
    private static final Pattern FORMATO_DECIMAL_CON_SIGNO = Pattern.compile("^[+-]?\\d+([.,]\\d{1,2})?$");

    private final IRepositorioTarifas tarifas;
    private final IRepositorioRutas rutas;
    private final Autorizador autorizador;

    public ServicioTarifas(IRepositorioTarifas tarifas, IRepositorioRutas rutas, Autorizador autorizador) {
        this.tarifas = tarifas;
        this.rutas = rutas;
        this.autorizador = autorizador;
    }

    // ------------------------------------------------------------ reglas de la escala (porcentajes de la tarifa base)
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
        Resultado<BigDecimal> valor = validarPorcentaje(rol, porcentaje);
        if (!valor.isOk()) {
            return Resultado.error(valor.getMensaje());
        }
        tarifas.guardarPorcentaje(rol, valor.getValor());
        return Resultado.exito(null, Mensajes.PORCENTAJE_ACTUALIZADO);
    }

    /**
     * Cambia a la vez los porcentajes de Estudiantes y Empleados.
     * Si uno de los dos campos se deja vacío, se le asigna su límite inferior (10 % para Estudiante, 30 % para Empleado)
     * y se informa en el mensaje de retorno. Si alguno de los ingresados es inválido, no se guarda ninguno.
     */
    public Resultado<Void> configurarPorcentajes(String estudiante, String empleado) {
        Resultado<Void> acceso = autorizador.exigirRol(Rol.ADMIN_TRANSPORTE);
        if (!acceso.isOk()) {
            return acceso;
        }
        boolean hayEstudiante = Validador.esTextoNoVacio(estudiante);
        boolean hayEmpleado = Validador.esTextoNoVacio(empleado);
        if (!hayEstudiante && !hayEmpleado) {
            return Resultado.error(Mensajes.PORCENTAJES_VACIOS);
        }
        BigDecimal paraEstudiante;
        BigDecimal paraEmpleado;
        if (hayEstudiante) {
            Resultado<BigDecimal> r = validarPorcentaje(Rol.ESTUDIANTE, estudiante);
            if (!r.isOk()) {
                return Resultado.error(r.getMensaje());
            }
            paraEstudiante = r.getValor();
        } else {
            paraEstudiante = Dinero.redondear(minimo(Rol.ESTUDIANTE));
        }

        if (hayEmpleado) {
            Resultado<BigDecimal> r = validarPorcentaje(Rol.EMPLEADO, empleado);
            if (!r.isOk()) {
                return Resultado.error(r.getMensaje());
            }
            paraEmpleado = r.getValor();
        } else {
            paraEmpleado = Dinero.redondear(minimo(Rol.EMPLEADO));
        }

        tarifas.guardarPorcentaje(Rol.ESTUDIANTE, paraEstudiante);
        tarifas.guardarPorcentaje(Rol.EMPLEADO, paraEmpleado);

        if (!hayEstudiante) {
            return Resultado.exito(null, String.format(
                    Mensajes.PORCENTAJE_ACTUALIZADO_CON_MINIMO,
                    nombre(Rol.ESTUDIANTE), minimo(Rol.ESTUDIANTE).toPlainString()));
        }
        if (!hayEmpleado) {
            return Resultado.exito(null, String.format(
                    Mensajes.PORCENTAJE_ACTUALIZADO_CON_MINIMO,
                    nombre(Rol.EMPLEADO), minimo(Rol.EMPLEADO).toPlainString()));
        }
        return Resultado.exito(null, Mensajes.PORCENTAJE_ACTUALIZADO);
    }

    /** Comprueba que el tipo de usuario tenga porcentaje configurable y que el valor esté dentro de su rango. */
    private Resultado<BigDecimal> validarPorcentaje(Rol rol, String porcentaje) {
        if (rol == Rol.PUBLICO_GENERAL) {
            return Resultado.error(Mensajes.PORCENTAJE_PUBLICO_FIJO);
        }
        if (rol != Rol.ESTUDIANTE && rol != Rol.EMPLEADO) {
            return Resultado.error(Mensajes.ROL_SIN_TARIFA);
        }
        BigDecimal valor = parsearDecimalConSigno(porcentaje);
        if (valor == null) {
            return Resultado.error(Mensajes.PORCENTAJE_INVALIDO);
        }
        if (valor.compareTo(minimo(rol)) < 0 || valor.compareTo(maximo(rol)) > 0) {
            return Resultado.error(String.format(Mensajes.PORCENTAJE_FUERA_DE_RANGO,
                    nombre(rol), minimo(rol).toPlainString(), maximo(rol).toPlainString()));
        }
        return Resultado.exito(valor, "");
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
        BigDecimal valor = parsearDecimalConSigno(monto);
        if (valor == null) {
            return Resultado.error(Mensajes.TARIFA_INVALIDA);
        }
        if (valor.compareTo(TARIFA_BASE_MINIMA) < 0) {
            return Resultado.error(Mensajes.TARIFA_MINIMA);
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

    private static BigDecimal parsearDecimalConSigno(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.trim();
        if (limpio.isEmpty() || !FORMATO_DECIMAL_CON_SIGNO.matcher(limpio).matches()) {
            return null;
        }
        return new BigDecimal(limpio.replace(',', '.')).setScale(2, RoundingMode.HALF_UP);
    }

    /** Tarifa base de la ruta, o null si no tiene. */
    public BigDecimal tarifaBase(String nombreRuta) {
        return tarifas.obtenerTarifaBase(nombreRuta);
    }

    // ------------------------------------------------------------ cálculo del cobro
    /**
     * Lo que paga el tipo de usuario por viajar en la ruta (base × porcentaje / 100).
     * Es 0.00 (no se cobra) si la ruta no es extraurbana, si no tiene tarifa configurada o si ese tipo de usuario no paga.
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