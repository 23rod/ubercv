package servicio;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import modelo.Movimiento;
import modelo.MensajesMonedero;
import modelo.Resultado;
import modelo.TipoMovimiento;
import persistencia.interfaces.IRepositorioMovimientos;
import utilidades.Dinero;

/**
 * Núcleo del monedero virtual. El saldo de un usuario es la suma de sus recargas y reintegros menos sus cobros,
 * así que se calcula siempre a partir del historial y no puede desincronizarse de él.
 *
 * Quién puede hacer qué: cualquier pantalla puede CONSULTAR ({@link #saldo} y {@link #historial}), pero solo los
 * servicios del mismo paquete pueden MOVER dinero ({@code acreditar}, {@code debitar}, {@code reintegrar} no son
 * públicos): la recarga por pago móvil, la reserva y la cancelación son las únicas que las llaman, así una pantalla
 * nunca puede sumar saldo por su cuenta.
 *
 * Integridad: cada operación verifica y anota dentro de una misma sección sincronizada, de modo que
 * 100 solicitudes simultáneas nunca dejan un saldo negativo ni gastan el mismo dinero dos veces. La garantía vale
 * dentro de un único proceso (la aplicación de escritorio); un sistema con varios servidores necesitaría transacciones
 * de base de datos.
 */
public class ServicioMonedero {

    private final IRepositorioMovimientos repositorio;
    private final Clock reloj;
    private final Object cerradura = new Object();

    public ServicioMonedero(IRepositorioMovimientos repositorio) {
        this(repositorio, Clock.systemDefaultZone());
    }

    /** El reloj se puede inyectar para que las pruebas controlen la fecha de los movimientos. */
    public ServicioMonedero(IRepositorioMovimientos repositorio, Clock reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    // ------------------------------------------------------------ consultas (públicas)
    /** Saldo actual con 2 decimales; 0.00 para un usuario sin movimientos. */
    public BigDecimal saldo(String correo) {
        synchronized (cerradura) {
            return calcularSaldo(correo);
        }
    }

    /** Hasta {@code maximo} movimientos del usuario, del más reciente al más antiguo. */
    public List<Movimiento> historial(String correo, int maximo) {
        synchronized (cerradura) {
            List<Movimiento> todos = new ArrayList<>(repositorio.listarPorUsuario(correo));
            Collections.reverse(todos);
            if (maximo <= 0) {
                return new ArrayList<>();
            }
            return new ArrayList<>(todos.subList(0, Math.min(maximo, todos.size())));
        }
    }

    // ------------------------------------------------------------ movimientos de dinero (solo servicios del paquete)
    /** Suma saldo por una recarga ya validada. La referencia es la del pago móvil y no puede repetirse. */
    Resultado<Void> acreditar(String correo, BigDecimal monto, String descripcion, String referencia) {
        Resultado<Void> invalido = validar(correo, monto);
        if (invalido != null) {
            return invalido;
        }
        synchronized (cerradura) {
            // La comprobación y el abono van juntos: dos recargas simultáneas con la misma referencia no pasan las dos
            if (referencia != null && !referencia.trim().isEmpty() && repositorio.existeReferencia(referencia)) {
                return Resultado.error(MensajesMonedero.REFERENCIA_REPETIDA);
            }
            try {
                anotar(correo, TipoMovimiento.RECARGA, monto, descripcion, referencia == null ? null : referencia.trim());
            } catch (RuntimeException e) {
                return Resultado.error("No se pudo guardar el movimiento de recarga: " + mensajeExcepcion(e));
            }
        }
        return Resultado.exito(null, "Recarga acreditada");
    }

    /** Descuenta saldo por un cobro. Falla, sin tocar nada, si el saldo no alcanza. */
    Resultado<Void> debitar(String correo, BigDecimal monto, String descripcion) {
        Resultado<Void> invalido = validar(correo, monto);
        if (invalido != null) {
            return invalido;
        }
        synchronized (cerradura) {
            if (calcularSaldo(correo).compareTo(Dinero.redondear(monto)) < 0) {
                return Resultado.error(MensajesMonedero.SALDO_INSUFICIENTE);
            }
            try {
                anotar(correo, TipoMovimiento.COBRO, monto, descripcion, null);
            } catch (RuntimeException e) {
                return Resultado.error("No se pudo guardar el cobro: " + mensajeExcepcion(e));
            }
        }
        return Resultado.exito(null, "Cobro realizado");
    }

    /** Devuelve saldo al usuario (por ejemplo, al cancelar una reserva ya cobrada). */
    Resultado<Void> reintegrar(String correo, BigDecimal monto, String descripcion) {
        Resultado<Void> invalido = validar(correo, monto);
        if (invalido != null) {
            return invalido;
        }
        synchronized (cerradura) {
            try {
                anotar(correo, TipoMovimiento.REINTEGRO, monto, descripcion, null);
            } catch (RuntimeException e) {
                return Resultado.error("No se pudo guardar el reintegro: " + mensajeExcepcion(e));
            }
        }
        return Resultado.exito(null, "Reintegro realizado");
    }

    // ------------------------------------------------------------ internos
    private Resultado<Void> validar(String correo, BigDecimal monto) {
        if (correo == null || correo.trim().isEmpty()) {
            return Resultado.error(MensajesMonedero.CUENTA_INVALIDA);
        }
        if (monto == null || monto.signum() <= 0) {
            return Resultado.error(MensajesMonedero.MONTO_INVALIDO);
        }
        return null;
    }

    private void anotar(String correo, TipoMovimiento tipo, BigDecimal monto, String descripcion, String referencia) {
        repositorio.agregar(new Movimiento(
                correo.trim().toLowerCase(), tipo, Dinero.redondear(monto),
                LocalDateTime.now(reloj), descripcion, referencia));
    }

    private static String mensajeExcepcion(RuntimeException e) {
        String mensaje = e.getMessage();
        return mensaje == null || mensaje.isBlank() ? e.getClass().getSimpleName() : mensaje;
    }

    /** Debe llamarse dentro de la sección sincronizada. */
    private BigDecimal calcularSaldo(String correo) {
        BigDecimal saldo = Dinero.CERO;
        for (Movimiento m : repositorio.listarPorUsuario(correo)) {
            saldo = m.getTipo().aumentaElSaldo() ? saldo.add(m.getMonto()) : saldo.subtract(m.getMonto());
        }
        return Dinero.redondear(saldo);
    }
}
