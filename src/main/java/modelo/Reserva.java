package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import utilidades.Dinero;

/**
 * Reserva persistible. Los datos que identifican el viaje y el importe cobrado se conservan
 * como una instantánea para que una modificación posterior de la tarifa no altere un reintegro.
 */
public final class Reserva {
    private final String nombreRuta;
    private final String tipoRuta;
    private final String estadoUnidad;
    private final LocalTime inicio;
    private final LocalTime fin;
    private final String correoUsuario;
    private final Ticket ticket;
    private final BigDecimal importeCobrado;
    private final String placaUnidad;
    private final LocalDate fechaServicio;
    private final EstadoReserva estado;

    /** Constructor compatible con el prototipo anterior. */
    public Reserva(String nombreRuta, String tipoRuta, String estadoUnidad,
                   LocalTime inicio, LocalTime fin, Ticket ticket,
                   String correoUsuario, BigDecimal importeCobrado) {
        this(nombreRuta, tipoRuta, estadoUnidad, inicio, fin, ticket, correoUsuario,
                importeCobrado, "", LocalDate.now(), EstadoReserva.ACTIVA);
    }

    public Reserva(String nombreRuta, String tipoRuta, String estadoUnidad,
                   LocalTime inicio, LocalTime fin, Ticket ticket,
                   String correoUsuario, BigDecimal importeCobrado,
                   String placaUnidad, LocalDate fechaServicio, EstadoReserva estado) {
        this.nombreRuta = obligatorio(nombreRuta, "El nombre de la ruta es obligatorio.");
        this.tipoRuta = obligatorio(tipoRuta, "El tipo de ruta es obligatorio.");
        this.estadoUnidad = estadoUnidad == null ? "Desconocido" : estadoUnidad;
        this.inicio = Objects.requireNonNull(inicio, "La hora de inicio es obligatoria.");
        this.fin = Objects.requireNonNull(fin, "La hora de fin es obligatoria.");
        this.ticket = Objects.requireNonNull(ticket, "El ticket es obligatorio.");
        this.correoUsuario = obligatorio(correoUsuario, "El correo del pasajero es obligatorio.").toLowerCase();
        this.importeCobrado = Dinero.redondear(importeCobrado == null ? Dinero.CERO : importeCobrado);
        if (this.importeCobrado.signum() < 0) {
            throw new IllegalArgumentException("El importe cobrado no puede ser negativo.");
        }
        this.placaUnidad = placaUnidad == null ? "" : placaUnidad.trim().toUpperCase();
        this.fechaServicio = Objects.requireNonNull(fechaServicio, "La fecha del servicio es obligatoria.");
        this.estado = Objects.requireNonNull(estado, "El estado de la reserva es obligatorio.");
    }

    private static String obligatorio(String texto, String mensaje) {
        if (texto == null || texto.isBlank()) throw new IllegalArgumentException(mensaje);
        return texto.trim();
    }

    public Reserva conEstado(EstadoReserva nuevoEstado) {
        return new Reserva(nombreRuta, tipoRuta, estadoUnidad, inicio, fin, ticket,
                correoUsuario, importeCobrado, placaUnidad, fechaServicio, nuevoEstado);
    }

    public String getNombreRuta() { return nombreRuta; }
    public String getTipoRuta() { return tipoRuta; }
    public String getEstadoUnidad() { return estadoUnidad; }
    public LocalTime getInicio() { return inicio; }
    public LocalTime getFin() { return fin; }
    public Ticket getTicketDeLaReserva() { return ticket; }
    public String getCorreoDeUsuario() { return correoUsuario; }
    public String getFechaDeReserva() { return ticket.getFechaReserva(); }
    public BigDecimal getImporteCobrado() { return importeCobrado; }
    public String getPlacaUnidad() { return placaUnidad; }
    public LocalDate getFechaServicio() { return fechaServicio; }
    public EstadoReserva getEstado() { return estado; }
    public boolean estaActiva() { return estado == EstadoReserva.ACTIVA; }
}
