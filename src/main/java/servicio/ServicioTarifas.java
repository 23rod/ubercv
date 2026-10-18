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
 * de cada ruta extraurbana y configura los porcentajes de descuento por rol:
 * - Descuento para estudiantes: entre 10 % y 20 % (por defecto 15 %, paga 85 % de T_base).
 * - Descuento para empleados: entre 30 % y 60 % (por defecto 45 %, paga 55 % de T_base).
 * - Público General: sin descuento (paga el 100 % de T_base).
 * - Conductores y administradores: no pagan tarifa (0.00).
 */
public class ServicioTarifas {

    public static final BigDecimal TARIFA_BASE_MINIMA = new BigDecimal("10.00");
    /** Acepta números enteros o con hasta 2 decimales (punto o coma), incluyendo signo para distinguir negativos de formato inválido y sin límite artificial de dígitos enteros. */
    private static final Pattern FORMATO_DECIMAL_CON_SIGNO = Pattern.compile("^[+-]?\\d+([.,]\\d{1,2})?$");

    private final IRepositorioTarifas tarifas;
    private final IRepositorioRutas rutas;
    private final Autorizador autorizador;

    public ServicioTarifas(IRepositorioTarifas tarifas, IRepositorioRutas rutas, Autorizador autorizador) {
        this.tarifas = tarifas;
        this.rutas = rutas;
        this.autorizador = autorizador;
    }

    // ------------------------------------------------------------ reglas de la escala (porcentajes de descuento)
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

    /**
     * Porcentaje de descuento aplicable al tipo de usuario:
     * Estudiante (10-20 %, defecto 15 %), Empleado (30-60 %, defecto 45 %),
     * Público General y demás roles tienen 0 % de descuento configurable.
     */
    public BigDecimal porcentajeDe(Rol rol) {
        if (rol != Rol.ESTUDIANTE && rol != Rol.EMPLEADO) {
            return BigDecimal.ZERO;
        }
        BigDecimal configurado = tarifas.obtenerPorcentaje(rol);
        return (configurado != null) ? configurado : porDefecto(rol);
    }

    /** Cambia el porcentaje de descuento de Estudiantes o Empleados dentro de su rango permitido. */
    public Resultado<Void> configurarPorcentaje(Rol rol, String porcentajeDescuento) {
        Resultado<Void> acceso = autorizador.exigirRol(Rol.ADMIN_TRANSPORTE);
        if (!acceso.isOk()) {
            return acceso;
        }
        Resultado<BigDecimal> valor = validarPorcentaje(rol, porcentajeDescuento);
        if (!valor.isOk()) {
            return Resultado.error(valor.getMensaje());
        }
        tarifas.guardarPorcentaje(rol, valor.getValor());
        return Resultado.exito(null, Mensajes.PORCENTAJE_ACTUALIZADO);
    }

    /**
     * Cambia a la vez los porcentajes de descuento de Estudiantes y Empleados (un campo en blanco significa "dejar como está").
     * Se valida todo antes de guardar: si uno de los dos es inválido no se guarda ninguno.
     */
    public Resultado<Void> configurarPorcentajes(String descuentoEstudiante, String descuentoEmpleado) {
        Resultado<Void> acceso = autorizador.exigirRol(Rol.ADMIN_TRANSPORTE);
        if (!acceso.isOk()) {
            return acceso;
        }
        boolean hayEstudiante = Validador.esTextoNoVacio(descuentoEstudiante);
        boolean hayEmpleado = Validador.esTextoNoVacio(descuentoEmpleado);
        if (!hayEstudiante && !hayEmpleado) {
            return Resultado.error(Mensajes.PORCENTAJES_VACIOS);
        }
        BigDecimal paraEstudiante = null;
        BigDecimal paraEmpleado = null;
        if (hayEstudiante) {
            Resultado<BigDecimal> r = validarPorcentaje(Rol.ESTUDIANTE, descuentoEstudiante);
            if (!r.isOk()) {
                return Resultado.error(r.getMensaje());
            }
            paraEstudiante = r.getValor();
        }
        if (hayEmpleado) {
            Resultado<BigDecimal> r = validarPorcentaje(Rol.EMPLEADO, descuentoEmpleado);
            if (!r.isOk()) {
                return Resultado.error(r.getMensaje());
            }
            paraEmpleado = r.getValor();
        }
        if (paraEstudiante != null) {
            tarifas.guardarPorcentaje(Rol.ESTUDIANTE, paraEstudiante);
        }
        if (paraEmpleado != null) {
            tarifas.guardarPorcentaje(Rol.EMPLEADO, paraEmpleado);
        }
        return Resultado.exito(null, Mensajes.PORCENTAJE_ACTUALIZADO);
    }

    /** Comprueba que el tipo de usuario tenga descuento configurable y que el valor numérico esté dentro de su rango. */
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

    /** Convierte texto numérico con hasta 2 decimales (sin tope superior artificial y admitiendo signo) a BigDecimal. */
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
     * Importe final que paga el tipo de usuario por viajar en la ruta tras aplicar su descuento.
     * Es 0.00 (no se cobra) si la ruta no es extraurbana, si no tiene tarifa configurada o si ese rol no paga.
     */
    public BigDecimal montoPara(Rol rol, String nombreRuta) {
        Ruta ruta = (nombreRuta == null) ? null : rutas.buscarPorNombre(nombreRuta);
        if (ruta == null || ruta.getTipo() != TipoRuta.EXTRAURBANA) {
            return Dinero.CERO;
        }
        BigDecimal base = tarifas.obtenerTarifaBase(ruta.getNombre());
        if (base == null || base.signum() <= 0) {
            return Dinero.CERO;
        }
        if (rol == Rol.PUBLICO_GENERAL) {
            return Dinero.redondear(base);
        }
        if (rol != Rol.ESTUDIANTE && rol != Rol.EMPLEADO) {
            return Dinero.CERO;
        }
        BigDecimal porcentajeDescuento = porcentajeDe(rol);
        BigDecimal montoDescuento = Dinero.porcentajeDe(base, porcentajeDescuento);
        return Dinero.redondear(base.subtract(montoDescuento));
    }

    /** Importes finales a pagar de la ruta por tipo de usuario, en el orden de la tabla: Estudiante, Empleado, Público General. */
    public Map<Rol, BigDecimal> desglose(String nombreRuta) {
        Map<Rol, BigDecimal> montos = new LinkedHashMap<>();
        montos.put(Rol.ESTUDIANTE, montoPara(Rol.ESTUDIANTE, nombreRuta));
        montos.put(Rol.EMPLEADO, montoPara(Rol.EMPLEADO, nombreRuta));
        montos.put(Rol.PUBLICO_GENERAL, montoPara(Rol.PUBLICO_GENERAL, nombreRuta));
        return montos;
    }
}