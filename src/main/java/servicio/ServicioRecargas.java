package servicio;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import modelo.Bancos;
import modelo.Mensajes;
import modelo.MensajesMonedero;
import modelo.RecargaPagoMovil;
import modelo.Resultado;
import modelo.Rol;
import modelo.Usuario;
import persistencia.interfaces.IRepositorioRecargas;
import utilidades.Dinero;
import utilidades.validadores.Validador;

/**
 * HU04 y HU05: recarga del monedero por pago móvil (simulada). El sistema valida el formato de los datos
 * y, si son correctos y la referencia no se había usado, acredita el monto de inmediato.
 *
 * Cada operación, exitosa o fallida, queda registrada a nombre del usuario para que el administrador
 * pueda consultar las operaciones de cada uno. Un usuario solo puede recargar SU propio monedero: la cuenta
 * sale de la sesión, nunca de un dato que escriba el usuario.
 */
public class ServicioRecargas {

    private static final int REFERENCIA_MIN = 6;
    private static final int REFERENCIA_MAX = 12;

    private final IRepositorioRecargas registro;
    private final ServicioMonedero monedero;
    private final SesionActual sesion;
    private final Autorizador autorizador;
    private final Clock reloj;

    public ServicioRecargas(IRepositorioRecargas registro, ServicioMonedero monedero, SesionActual sesion, Autorizador autorizador) {
        this(registro, monedero, sesion, autorizador, Clock.systemDefaultZone());
    }

    /** El reloj se puede inyectar para que las pruebas controlen la fecha de las operaciones. */
    public ServicioRecargas(IRepositorioRecargas registro, ServicioMonedero monedero, SesionActual sesion,
                            Autorizador autorizador, Clock reloj) {
        this.registro = registro;
        this.monedero = monedero;
        this.sesion = sesion;
        this.autorizador = autorizador;
        this.reloj = reloj;
    }

    /** Solo los usuarios que viajan pagando pueden recargar: estudiantes, empleados y público general. */
    private static boolean puedeRecargar(Rol rol) {
        return rol == Rol.ESTUDIANTE || rol == Rol.EMPLEADO || rol == Rol.PUBLICO_GENERAL;
    }

    // ------------------------------------------------------------ HU04: recargar
    public Resultado<Void> recargar(String bancoOrigen, String bancoDestino, String cedula,
                                    String telefono, String referencia, String monto) {
        Usuario usuario = sesion.getUsuarioSesion();
        if (usuario == null || !puedeRecargar(sesion.getRolSesion())) {
            return Resultado.error(Mensajes.ACCESO_DENEGADO);
        }

        List<String> errores = new ArrayList<>();
        if (!Bancos.esOrigenValido(bancoOrigen)) {
            errores.add(MensajesMonedero.BANCO_ORIGEN_INVALIDO);
        }
        if (!Bancos.esDestinoValido(bancoDestino)) {
            errores.add(MensajesMonedero.BANCO_DESTINO_INVALIDO);
        }
        if (!Validador.esCedulaValida(texto(cedula))) {
            errores.add(MensajesMonedero.CEDULA_PAGO_INVALIDA);
        }
        if (!texto(telefono).matches("\\d{11}")) {
            errores.add(MensajesMonedero.TELEFONO_INVALIDO);
        }
        if (!texto(referencia).matches("\\d{" + REFERENCIA_MIN + "," + REFERENCIA_MAX + "}")) {
            errores.add(MensajesMonedero.REFERENCIA_INVALIDA);
        }
        BigDecimal valor = Dinero.parsearDecimal(monto);
        if (valor == null || valor.signum() <= 0) {
            errores.add(MensajesMonedero.MONTO_PAGO_INVALIDO);
        }

        if (!errores.isEmpty()) {
            anotar(usuario, bancoOrigen, bancoDestino, cedula, telefono, referencia, monto, false, MensajesMonedero.RECARGA_FALLIDA);
            return Resultado.error(MensajesMonedero.DATOS_PAGO_INCORRECTOS + "\n" + String.join("\n", errores));
        }

        Resultado<Void> abono = monedero.acreditar(usuario.getCorreo(), valor,
                "Recarga por pago móvil (ref. " + texto(referencia) + ")", texto(referencia));
        if (!abono.isOk()) { // referencia ya usada
            anotar(usuario, bancoOrigen, bancoDestino, cedula, telefono, referencia, monto, false, abono.getMensaje());
            return Resultado.error(abono.getMensaje());
        }

        anotar(usuario, bancoOrigen, bancoDestino, cedula, telefono, referencia, Dinero.formatear(valor), true, MensajesMonedero.RECARGA_EXITOSA);
        return Resultado.exito(null, MensajesMonedero.SOLICITUD_ACEPTADA + ". " + MensajesMonedero.RECARGA_EXITOSA
                + ". Nuevo saldo: " + Dinero.formatear(monedero.saldo(usuario.getCorreo())));
    }

    // ------------------------------------------------------------ consulta del propio usuario
    /** Operaciones de pago móvil del usuario con sesión, de la más reciente a la más antigua, hasta {@code maximo}. */
    public Resultado<List<RecargaPagoMovil>> misRecargas(int maximo) {
        Usuario usuario = sesion.getUsuarioSesion();
        if (usuario == null || !puedeRecargar(sesion.getRolSesion())) {
            return Resultado.error(Mensajes.ACCESO_DENEGADO);
        }
        return Resultado.exito(masRecientesPrimero(registro.listarPorUsuario(usuario.getCorreo()), maximo), "");
    }

    // ------------------------------------------------------------ HU05: consulta del administrador por usuario
    /** Correos de los usuarios que tienen operaciones de pago móvil. Solo para el administrador. */
    public Resultado<List<String>> usuariosConRecargas() {
        Resultado<Void> acceso = autorizador.exigirRol(Rol.ADMIN_TRANSPORTE);
        if (!acceso.isOk()) {
            return Resultado.error(acceso.getMensaje());
        }
        return Resultado.exito(registro.listarCorreos(), "");
    }

    /** Todas las operaciones de un usuario, de la más reciente a la más antigua. Solo para el administrador. */
    public Resultado<List<RecargaPagoMovil>> recargasDe(String correo) {
        Resultado<Void> acceso = autorizador.exigirRol(Rol.ADMIN_TRANSPORTE);
        if (!acceso.isOk()) {
            return Resultado.error(acceso.getMensaje());
        }
        return Resultado.exito(masRecientesPrimero(registro.listarPorUsuario(correo), Integer.MAX_VALUE), "");
    }

    // ------------------------------------------------------------ internos
    private static List<RecargaPagoMovil> masRecientesPrimero(List<RecargaPagoMovil> cronologicas, int maximo) {
        List<RecargaPagoMovil> copia = new ArrayList<>(cronologicas);
        Collections.reverse(copia);
        if (maximo <= 0) {
            return new ArrayList<>();
        }
        return new ArrayList<>(copia.subList(0, Math.min(maximo, copia.size())));
    }

    private static String texto(String valor) {
        return (valor == null) ? "" : valor.trim();
    }

    private void anotar(Usuario usuario, String bancoOrigen, String bancoDestino, String cedula, String telefono,
                        String referencia, String monto, boolean exitosa, String detalle) {
        registro.agregar(new RecargaPagoMovil(usuario.getCorreo(), LocalDateTime.now(reloj), texto(bancoOrigen), texto(bancoDestino),
                texto(cedula), texto(telefono), texto(referencia), texto(monto), exitosa, detalle));
    }
}
