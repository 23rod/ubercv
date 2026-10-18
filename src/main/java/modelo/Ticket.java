package modelo;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import utilidades.Dinero;

/** Comprobante inmutable emitido al reservar un puesto. */
public final class Ticket {
    private static final String CARACTERES = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final String codigo;
    private final LocalDate fechaReserva;
    private final String nombreRuta;
    private final BigDecimal importeCobrado;

    /** Constructor compatible con el modelo anterior. */
    public Ticket(String nombreRuta) {
        this(generarCodigo(), nombreRuta, LocalDate.now(), Dinero.CERO);
    }

    public Ticket(String nombreRuta, BigDecimal importeCobrado) {
        this(generarCodigo(), nombreRuta, LocalDate.now(), importeCobrado);
    }

    public Ticket(String nombreRuta, BigDecimal importeCobrado, LocalDate fechaReserva) {
        this(generarCodigo(), nombreRuta, fechaReserva, importeCobrado);
    }

    /** Constructor para reconstruir un ticket persistido; el código no se vuelve a generar. */
    public Ticket(String codigo, String nombreRuta, LocalDate fechaReserva, BigDecimal importeCobrado) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del ticket es obligatorio.");
        }
        if (nombreRuta == null || nombreRuta.isBlank()) {
            throw new IllegalArgumentException("La ruta del ticket es obligatoria.");
        }
        this.codigo = codigo.trim();
        this.nombreRuta = nombreRuta.trim();
        this.fechaReserva = Objects.requireNonNull(fechaReserva, "La fecha del ticket es obligatoria.");
        this.importeCobrado = Dinero.redondear(importeCobrado == null ? Dinero.CERO : importeCobrado);
        if (this.importeCobrado.signum() < 0) {
            throw new IllegalArgumentException("El importe del ticket no puede ser negativo.");
        }
    }

    private static String generarCodigo() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(CARACTERES.charAt(RANDOM.nextInt(CARACTERES.length())));
        }
        return sb.toString();
    }

    public String getCodigo() { return codigo; }

    /** Fecha en el formato que ya esperaba la interfaz. */
    public String getFechaReserva() { return fechaReserva.format(FORMATO_FECHA); }

    public LocalDate getFechaReservaDate() { return fechaReserva; }

    public String getNombreRuta() { return nombreRuta; }

    public BigDecimal getImporteCobrado() { return importeCobrado; }
}
