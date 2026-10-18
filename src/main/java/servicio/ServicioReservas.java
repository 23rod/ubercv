package servicio;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import modelo.EstadoReserva;
import modelo.EstadoUnidad;
import modelo.Reserva;
import modelo.Resultado;
import modelo.Rol;
import modelo.Ruta;
import modelo.Ticket;
import modelo.UnidadTransporte;
import persistencia.interfaces.IRepositorioReservas;
import persistencia.interfaces.IRepositorioRutas;
import persistencia.interfaces.IRepositorioUnidades;
import utilidades.Dinero;

/** Reglas de negocio para reservar y cancelar puestos. No depende de Swing. */
public class ServicioReservas {
    private static final int INTENTOS_CODIGO_TICKET = 20;
    private static final String MENSAJE_RESERVA_EXITOSA = "Solicitud de cupo realizada correctamente";
    private static final String MENSAJE_CANCELACION_EXITOSA = "Reserva cancelada con éxito";

    private final IRepositorioReservas reservas;
    private final IRepositorioUnidades unidades;
    private final IRepositorioRutas rutas;
    private final ServicioUsuarios usuarios;
    private final ServicioTarifas tarifas;
    private final ServicioMonedero monedero;
    private final Clock reloj;

    public ServicioReservas(IRepositorioReservas reservas,
                            IRepositorioUnidades unidades,
                            IRepositorioRutas rutas,
                            ServicioUsuarios usuarios,
                            ServicioTarifas tarifas,
                            ServicioMonedero monedero) {
        this(reservas, unidades, rutas, usuarios, tarifas, monedero, Clock.systemDefaultZone());
    }

    /** Reloj inyectable para que las pruebas no dependan de la fecha real. */
    public ServicioReservas(IRepositorioReservas reservas,
                            IRepositorioUnidades unidades,
                            IRepositorioRutas rutas,
                            ServicioUsuarios usuarios,
                            ServicioTarifas tarifas,
                            ServicioMonedero monedero,
                            Clock reloj) {
        this.reservas = java.util.Objects.requireNonNull(reservas);
        this.unidades = java.util.Objects.requireNonNull(unidades);
        this.rutas = java.util.Objects.requireNonNull(rutas);
        this.usuarios = java.util.Objects.requireNonNull(usuarios);
        this.tarifas = java.util.Objects.requireNonNull(tarifas);
        this.monedero = java.util.Objects.requireNonNull(monedero);
        this.reloj = java.util.Objects.requireNonNull(reloj);
    }

    /** Reserva un puesto para la ruta y unidad seleccionadas, para la fecha actual del servicio. */
    public synchronized Resultado<Reserva> reservar(String nombreRuta, String placaUnidad) {
        var usuario = usuarios.getUsuarioSesion();
        if (usuario == null) return Resultado.error("Debe iniciar sesión para reservar un cupo.");
        Rol rol = usuario.getRol();
        if (rol != Rol.ESTUDIANTE && rol != Rol.EMPLEADO && rol != Rol.PUBLICO_GENERAL) {
            return Resultado.error("El usuario actual no tiene permiso para reservar puestos.");
        }
        if (nombreRuta == null || nombreRuta.isBlank()) {
            return Resultado.error("Seleccione una ruta antes de reservar.");
        }
        if (placaUnidad == null || placaUnidad.isBlank() || placaUnidad.equalsIgnoreCase("Ninguna")) {
            return Resultado.error("Seleccione una unidad con cupos disponibles.");
        }

        Ruta ruta = rutas.buscarPorNombre(nombreRuta.trim());
        if (ruta == null) return Resultado.error("La ruta seleccionada ya no existe.");
        UnidadTransporte unidad = unidades.buscarPorPlaca(placaUnidad.trim());
        if (unidad == null) return Resultado.error("La unidad seleccionada ya no existe.");
        if (unidad.getEstado() != EstadoUnidad.ACTIVO) {
            return Resultado.error("La unidad seleccionada no está activa.");
        }
        if (unidad.getNombreRuta() == null || !unidad.getNombreRuta().equalsIgnoreCase(ruta.getNombre())) {
            return Resultado.error("La unidad seleccionada no está asignada a la ruta elegida.");
        }

        LocalDate fechaServicio = LocalDate.now(reloj);
        String correo = usuario.getCorreo().trim().toLowerCase(Locale.ROOT);
        boolean duplicada = false;
        for (Reserva activa : reservas.listarActivasPorUsuarioYFecha(correo, fechaServicio)) {
            if (activa.getNombreRuta().equalsIgnoreCase(ruta.getNombre())) {
                duplicada = true;
                break;
            }
        }
        if (duplicada) return Resultado.error("Ruta ya reservada anteriormente");

        if (cuposDisponibles(unidad, fechaServicio) <= 0) {
            return Resultado.error("No quedan cupos disponibles en la unidad seleccionada.");
        }

        BigDecimal importe = Dinero.redondear(tarifas.montoPara(rol, ruta.getNombre()));
        if (importe.signum() < 0) return Resultado.error("La tarifa calculada no es válida.");
        if (importe.signum() > 0 && monedero.saldo(correo).compareTo(importe) < 0) {
            return Resultado.error("Saldo insuficiente, por favor recargue");
        }

        Ticket ticket = generarTicketUnico(ruta.getNombre(), importe, fechaServicio);
        if (ticket == null) return Resultado.error("No se pudo generar un código de ticket único. Intente de nuevo.");
        Reserva nueva = new Reserva(ruta.getNombre(), ruta.getTipo().getEtiqueta(),
                unidad.getEstado().getEtiqueta(), ruta.getInicioJornada(), ruta.getFinJornada(),
                ticket, correo, importe, unidad.getPlaca(), fechaServicio, EstadoReserva.ACTIVA);

        boolean cobrado = false;
        if (importe.signum() > 0) {
            Resultado<Void> cobro;
            try {
                cobro = monedero.debitar(correo, importe, "Reserva de cupo: " + ruta.getNombre()
                        + " (ticket " + ticket.getCodigo() + ")");
            } catch (RuntimeException e) {
                return Resultado.error("No se pudo completar el cobro: " + mensajeExcepcion(e));
            }
            if (!cobro.isOk()) return Resultado.error(cobro.getMensaje());
            cobrado = true;
        }

        try {
            reservas.guardar(nueva);
        } catch (RuntimeException e) {
            if (cobrado) {
                try {
                    Resultado<Void> reintegro = monedero.reintegrar(correo, importe,
                            "Compensación por fallo al guardar la reserva (ticket " + ticket.getCodigo() + ")");
                    if (!reintegro.isOk()) {
                        return Resultado.error("No se pudo guardar la reserva ni completar la compensación del cobro. "
                                + "Se requiere revisar el monedero. Detalle: " + reintegro.getMensaje());
                    }
                } catch (RuntimeException ex) {
                    return Resultado.error("No se pudo guardar la reserva y falló la compensación del cobro. "
                            + "Se requiere revisar el monedero manualmente. Detalle: " + mensajeExcepcion(ex));
                }
            }
            return Resultado.error("No se pudo guardar la reserva: " + mensajeExcepcion(e));
        }
        return Resultado.exito(nueva, MENSAJE_RESERVA_EXITOSA);
    }

    /** Cancela una reserva activa perteneciente a la sesión actual. */
    public synchronized Resultado<Reserva> cancelar(String codigoTicket) {
        var usuario = usuarios.getUsuarioSesion();
        if (usuario == null) return Resultado.error("Debe iniciar sesión para cancelar una reserva.");
        if (codigoTicket == null || codigoTicket.isBlank()) return Resultado.error("No hay un ticket seleccionado para cancelar.");

        Reserva existente = reservas.buscarPorCodigo(codigoTicket.trim());
        if (existente == null || !existente.getCorreoDeUsuario().equalsIgnoreCase(usuario.getCorreo())) {
            return Resultado.error("No se encontró una reserva del usuario actual con ese ticket.");
        }
        if (!existente.estaActiva()) return Resultado.error("La reserva ya fue cancelada o finalizada.");

        Reserva cancelada = existente.conEstado(EstadoReserva.CANCELADA);
        try {
            reservas.actualizar(cancelada);
        } catch (RuntimeException e) {
            return Resultado.error("No se pudo actualizar el estado de la reserva: " + mensajeExcepcion(e));
        }

        BigDecimal importe = existente.getImporteCobrado();
        if (importe.signum() > 0) {
            Resultado<Void> reintegro;
            try {
                reintegro = monedero.reintegrar(existente.getCorreoDeUsuario(), importe,
                        "Reintegro por cancelación de reserva (ticket " + existente.getTicketDeLaReserva().getCodigo() + ")");
            } catch (RuntimeException e) {
                return restaurarTrasFalloReintegro(existente, e);
            }
            if (!reintegro.isOk()) {
                try {
                    reservas.actualizar(existente);
                } catch (RuntimeException e) {
                    return Resultado.error("Falló el reintegro y tampoco se pudo restaurar la reserva activa. "
                            + "Se requiere revisión manual. Detalle: " + mensajeExcepcion(e));
                }
                return Resultado.error("No se pudo reintegrar el dinero; la reserva sigue activa. " + reintegro.getMensaje());
            }
        }
        return Resultado.exito(cancelada, MENSAJE_CANCELACION_EXITOSA);
    }

    /** Cancela la reserva activa más reciente del usuario para el día actual. */
    public synchronized Resultado<Reserva> cancelarReservaActual() {
        Reserva actual = reservaActivaDelDia();
        if (actual == null) return Resultado.error("No tienes una reserva activa para hoy.");
        return cancelar(actual.getTicketDeLaReserva().getCodigo());
    }

    /** Todas las reservas activas del usuario para hoy. La interfaz puede permitir seleccionar cuál cancelar. */
    public synchronized List<Reserva> reservasActivasDelDia() {
        var usuario = usuarios.getUsuarioSesion();
        if (usuario == null) return new ArrayList<>();
        return reservas.listarActivasPorUsuarioYFecha(usuario.getCorreo(), LocalDate.now(reloj));
    }

    /** Reserva activa más reciente del usuario para hoy, o null si no tiene ninguna. */
    public synchronized Reserva reservaActivaDelDia() {
        List<Reserva> activas = reservasActivasDelDia();
        return activas.isEmpty() ? null : activas.get(0);
    }

    /** Placas elegibles: activas, asignadas a la ruta y con capacidad libre para la fecha indicada. */
    public synchronized List<String> placasDisponibles(String nombreRuta, LocalDate fechaServicio) {
        List<String> resultado = new ArrayList<>();
        Ruta ruta = nombreRuta == null ? null : rutas.buscarPorNombre(nombreRuta.trim());
        if (ruta == null || fechaServicio == null) return resultado;
        for (UnidadTransporte unidad : unidades.listarPorRuta(ruta.getNombre())) {
            if (unidad.getEstado() == EstadoUnidad.ACTIVO && cuposDisponibles(unidad, fechaServicio) > 0) {
                resultado.add(unidad.getPlaca());
            }
        }
        return resultado;
    }

    public synchronized List<String> placasDisponiblesHoy(String nombreRuta) {
        return placasDisponibles(nombreRuta, LocalDate.now(reloj));
    }

    public synchronized int cuposDisponiblesRuta(String nombreRuta, LocalDate fechaServicio) {
        Ruta ruta = nombreRuta == null ? null : rutas.buscarPorNombre(nombreRuta.trim());
        if (ruta == null || fechaServicio == null) return 0;
        int total = 0;
        for (UnidadTransporte unidad : unidades.listarPorRuta(ruta.getNombre())) {
            if (unidad.getEstado() == EstadoUnidad.ACTIVO) total += cuposDisponibles(unidad, fechaServicio);
        }
        return total;
    }

    /** Puestos libres de la ruta para la fecha de servicio actual (solo cuentan unidades activas). */
    public synchronized int cuposDisponiblesRutaHoy(String nombreRuta) {
        return cuposDisponiblesRuta(nombreRuta, LocalDate.now(reloj));
    }

    /**
     * Importe final que pagaría la sesión actual por reservar en la ruta, calculado por {@link ServicioTarifas}.
     * Es 0.00 sin sesión, para rutas urbanas, extraurbanas sin tarifa o roles que no pagan.
     */
    public synchronized BigDecimal importeEstimado(String nombreRuta) {
        var usuario = usuarios.getUsuarioSesion();
        if (usuario == null || nombreRuta == null || nombreRuta.isBlank()) return Dinero.CERO;
        return Dinero.redondear(tarifas.montoPara(usuario.getRol(), nombreRuta.trim()));
    }

    /** Estado operativo actual de la unidad (no la instantánea guardada en la reserva); null si la placa ya no existe. */
    public synchronized EstadoUnidad estadoActualDeUnidad(String placa) {
        if (placa == null || placa.isBlank()) return null;
        UnidadTransporte unidad = unidades.buscarPorPlaca(placa.trim());
        return unidad == null ? null : unidad.getEstado();
    }

    private int cuposDisponibles(UnidadTransporte unidad, LocalDate fechaServicio) {
        int ocupados = reservas.listarActivasPorUnidadYFecha(unidad.getPlaca(), fechaServicio).size();
        return Math.max(0, unidad.getCapacidad() - ocupados);
    }

    private Ticket generarTicketUnico(String nombreRuta, BigDecimal importe, LocalDate fecha) {
        for (int i = 0; i < INTENTOS_CODIGO_TICKET; i++) {
            Ticket candidato = new Ticket(nombreRuta, importe, fecha);
            if (!reservas.existeCodigoTicket(candidato.getCodigo())) return candidato;
        }
        return null;
    }

    private Resultado<Reserva> restaurarTrasFalloReintegro(Reserva original, RuntimeException causa) {
        try {
            reservas.actualizar(original);
            return Resultado.error("No se pudo completar el reintegro; la reserva sigue activa. Detalle: " + mensajeExcepcion(causa));
        } catch (RuntimeException e) {
            return Resultado.error("Falló el reintegro y no se pudo restaurar el estado activo de la reserva. "
                    + "Se requiere revisión manual. Detalle: " + mensajeExcepcion(e));
        }
    }

    private static String mensajeExcepcion(RuntimeException e) {
        String mensaje = e.getMessage();
        return mensaje == null || mensaje.isBlank() ? e.getClass().getSimpleName() : mensaje;
    }
}
