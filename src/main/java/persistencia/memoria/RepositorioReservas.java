package persistencia.memoria;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import modelo.EstadoReserva;
import modelo.Reserva;
import modelo.Ticket;
import persistencia.interfaces.IRepositorioReservas;

/**
 * Repositorio de reservas en memoria o en un archivo de texto. Cada escritura reemplaza el archivo
 * de forma atómica y revierte el estado en memoria si la persistencia falla.
 */
public class RepositorioReservas implements IRepositorioReservas {
    private static final String VERSION = "1";
    private final Path archivo;
    private final Map<String, Reserva> porCodigo = new LinkedHashMap<>();

    /** Constructor en memoria, útil para pruebas. */
    public RepositorioReservas() {
        this(null);
    }

    /** Si el archivo no existe, el repositorio empieza vacío. */
    public RepositorioReservas(Path archivo) {
        this.archivo = archivo;
        cargar();
    }

    @Override
    public synchronized void guardar(Reserva reserva) {
        validarReserva(reserva);
        String codigo = claveCodigo(reserva.getTicketDeLaReserva().getCodigo());
        if (porCodigo.containsKey(codigo)) {
            throw new IllegalArgumentException("Ya existe una reserva con ese código de ticket.");
        }
        porCodigo.put(codigo, reserva);
        try {
            persistir();
        } catch (RuntimeException e) {
            porCodigo.remove(codigo);
            throw e;
        }
    }

    @Override
    public synchronized void actualizar(Reserva reserva) {
        validarReserva(reserva);
        String codigo = claveCodigo(reserva.getTicketDeLaReserva().getCodigo());
        Reserva anterior = porCodigo.get(codigo);
        if (anterior == null) {
            throw new IllegalArgumentException("No existe la reserva que se intenta actualizar.");
        }
        porCodigo.put(codigo, reserva);
        try {
            persistir();
        } catch (RuntimeException e) {
            porCodigo.put(codigo, anterior);
            throw e;
        }
    }

    @Override
    public synchronized Reserva buscarPorCodigo(String codigoTicket) {
        if (codigoTicket == null) return null;
        return porCodigo.get(claveCodigo(codigoTicket));
    }

    @Override
    public synchronized boolean existeCodigoTicket(String codigoTicket) {
        return codigoTicket != null && porCodigo.containsKey(claveCodigo(codigoTicket));
    }

    @Override
    public synchronized List<Reserva> listar() {
        return new ArrayList<>(porCodigo.values());
    }

    @Override
    public synchronized List<Reserva> listarPorUsuario(String correo) {
        List<Reserva> resultado = new ArrayList<>();
        if (correo == null) return resultado;
        String claveCorreo = normalizarCorreo(correo);
        for (Reserva reserva : porCodigo.values()) {
            if (normalizarCorreo(reserva.getCorreoDeUsuario()).equals(claveCorreo)) resultado.add(reserva);
        }
        return resultado;
    }

    @Override
    public synchronized List<Reserva> listarActivasPorUsuarioYFecha(String correo, LocalDate fechaServicio) {
        List<Reserva> resultado = new ArrayList<>();
        if (correo == null || fechaServicio == null) return resultado;
        String claveCorreo = normalizarCorreo(correo);
        for (Reserva reserva : porCodigo.values()) {
            if (reserva.estaActiva()
                    && normalizarCorreo(reserva.getCorreoDeUsuario()).equals(claveCorreo)
                    && reserva.getFechaServicio().equals(fechaServicio)) {
                resultado.add(reserva);
            }
        }
        return resultado;
    }

    @Override
    public synchronized List<Reserva> listarActivasPorRutaYFecha(String nombreRuta, LocalDate fechaServicio) {
        List<Reserva> resultado = new ArrayList<>();
        if (nombreRuta == null || fechaServicio == null) return resultado;
        for (Reserva reserva : porCodigo.values()) {
            if (reserva.estaActiva()
                    && reserva.getNombreRuta().equalsIgnoreCase(nombreRuta.trim())
                    && reserva.getFechaServicio().equals(fechaServicio)) {
                resultado.add(reserva);
            }
        }
        return resultado;
    }

    @Override
    public synchronized List<Reserva> listarActivasPorUnidadYFecha(String placa, LocalDate fechaServicio) {
        List<Reserva> resultado = new ArrayList<>();
        if (placa == null || fechaServicio == null) return resultado;
        for (Reserva reserva : porCodigo.values()) {
            if (reserva.estaActiva()
                    && reserva.getPlacaUnidad().equalsIgnoreCase(placa.trim())
                    && reserva.getFechaServicio().equals(fechaServicio)) {
                resultado.add(reserva);
            }
        }
        return resultado;
    }

    private void cargar() {
        if (archivo == null || !Files.exists(archivo)) return;
        try {
            for (String linea : Files.readAllLines(archivo, StandardCharsets.UTF_8)) {
                if (linea == null || linea.isBlank()) continue;
                try {
                    Reserva reserva = decodificar(linea);
                    String codigo = claveCodigo(reserva.getTicketDeLaReserva().getCodigo());
                    if (porCodigo.putIfAbsent(codigo, reserva) != null) {
                        throw new IllegalArgumentException("Código de ticket duplicado.");
                    }
                } catch (RuntimeException e) {
                    throw new IllegalStateException("Registro de reserva inválido en " + archivo + ": " + linea, e);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudieron leer las reservas desde " + archivo, e);
        }
    }

    private void persistir() {
        if (archivo == null) return;
        try {
            List<String> lineas = new ArrayList<>();
            for (Reserva reserva : porCodigo.values()) lineas.add(codificar(reserva));
            AlmacenArchivo.escribirAtomico(archivo, lineas);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudieron guardar las reservas en " + archivo, e);
        }
    }

    /* Versionado y campos separados por tabulador. Los textos se codifican en Base64 URL-safe. */
    private static String codificar(Reserva r) {
        Ticket t = r.getTicketDeLaReserva();
        return String.join("\t", VERSION,
                codificarTexto(t.getCodigo()),
                codificarTexto(r.getNombreRuta()),
                codificarTexto(r.getTipoRuta()),
                codificarTexto(r.getEstadoUnidad()),
                r.getInicio().toString(),
                r.getFin().toString(),
                codificarTexto(r.getCorreoDeUsuario()),
                codificarTexto(r.getPlacaUnidad()),
                r.getFechaServicio().toString(),
                r.getImporteCobrado().toPlainString(),
                r.getEstado().name(),
                t.getFechaReservaDate().toString(),
                t.getImporteCobrado().toPlainString());
    }

    private static Reserva decodificar(String linea) {
        String[] p = linea.split("\t", -1);
        if (p.length != 14 || !VERSION.equals(p[0])) {
            throw new IllegalArgumentException("Versión o cantidad de campos no válida.");
        }
        String codigo = decodificarTexto(p[1]);
        String nombreRuta = decodificarTexto(p[2]);
        String tipoRuta = decodificarTexto(p[3]);
        String estadoUnidad = decodificarTexto(p[4]);
        LocalTime inicio = LocalTime.parse(p[5]);
        LocalTime fin = LocalTime.parse(p[6]);
        String correo = decodificarTexto(p[7]);
        String placa = decodificarTexto(p[8]);
        LocalDate fechaServicio = LocalDate.parse(p[9]);
        BigDecimal importe = new BigDecimal(p[10]);
        EstadoReserva estado = EstadoReserva.valueOf(p[11]);
        LocalDate fechaTicket = LocalDate.parse(p[12]);
        BigDecimal importeTicket = new BigDecimal(p[13]);
        Ticket ticket = new Ticket(codigo, nombreRuta, fechaTicket, importeTicket);
        return new Reserva(nombreRuta, tipoRuta, estadoUnidad, inicio, fin, ticket,
                correo, importe, placa, fechaServicio, estado);
    }

    private static String codificarTexto(String texto) {
        String valor = texto == null ? "" : texto;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(valor.getBytes(StandardCharsets.UTF_8));
    }

    private static String decodificarTexto(String texto) {
        return new String(Base64.getUrlDecoder().decode(texto), StandardCharsets.UTF_8);
    }

    private static void validarReserva(Reserva reserva) {
        if (reserva == null) throw new IllegalArgumentException("La reserva es obligatoria.");
        if (reserva.getTicketDeLaReserva() == null || reserva.getTicketDeLaReserva().getCodigo().isBlank()) {
            throw new IllegalArgumentException("La reserva necesita un ticket válido.");
        }
    }

    private static String claveCodigo(String codigo) { return codigo.trim().toUpperCase(Locale.ROOT); }
    private static String normalizarCorreo(String correo) { return correo.trim().toLowerCase(Locale.ROOT); }
}
