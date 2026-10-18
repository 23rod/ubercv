package servicio;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import modelo.EstadoReserva;
import modelo.EstadoUnidad;
import modelo.Reserva;
import modelo.Resultado;
import modelo.Ruta;
import modelo.TipoRuta;
import modelo.UnidadTransporte;
import persistencia.interfaces.IRepositorioReservas;
import persistencia.memoria.DatosSemilla;
import persistencia.memoria.RepositorioMovimientos;
import persistencia.memoria.RepositorioReservas;
import persistencia.memoria.RepositorioRutas;
import persistencia.memoria.RepositorioTarifas;
import persistencia.memoria.RepositorioUnidades;
import persistencia.memoria.RepositorioUsuarios;

/** Pruebas de las reglas de negocio de HU07, HU07b y HU11b. */
class ServicioReservasTest {
    private static final String EXTRA = "Ruta Extra";
    private static final String URBANA = "Ruta Urbana";
    private static final String PLACA_EXTRA = "EXT123";
    private static final String PLACA_URBANA = "URB123";
    private static final ZoneId ZONA = ZoneId.of("America/Caracas");
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-10-17T14:00:00Z"), ZONA);
    private static final LocalDate HOY = LocalDate.now(RELOJ);

    @TempDir Path tempDir;

    private RepositorioUsuarios repoUsuarios;
    private ServicioUsuarios usuarios;
    private RepositorioUnidades repoUnidades;
    private RepositorioRutas repoRutas;
    private RepositorioTarifas repoTarifas;
    private RepositorioReservas repoReservas;
    private RepositorioMovimientos repoMovimientos;
    private ServicioMonedero monedero;
    private ServicioReservas servicio;

    private static BigDecimal d(String valor) { return new BigDecimal(valor); }

    @BeforeEach
    void preparar() {
        repoUsuarios = new RepositorioUsuarios();
        DatosSemilla.sembrarUsuarios(repoUsuarios);
        usuarios = new ServicioUsuarios(repoUsuarios);
        assertTrue(usuarios.iniciarSesion("estudiante@ucv.com", DatosSemilla.CLAVE_DEMO).isOk());

        repoUnidades = new RepositorioUnidades();
        repoRutas = new RepositorioRutas();
        repoRutas.guardar(new Ruta(EXTRA, TipoRuta.EXTRAURBANA, LocalTime.of(6, 0), LocalTime.of(20, 0)));
        repoRutas.guardar(new Ruta(URBANA, TipoRuta.URBANA, LocalTime.of(6, 0), LocalTime.of(20, 0)));
        UnidadTransporte extra = new UnidadTransporte(PLACA_EXTRA, "Bus extraurbano", 1, EstadoUnidad.ACTIVO);
        extra.setNombreRuta(EXTRA);
        repoUnidades.guardar(extra);
        UnidadTransporte urbana = new UnidadTransporte(PLACA_URBANA, "Bus urbano", 2, EstadoUnidad.ACTIVO);
        urbana.setNombreRuta(URBANA);
        repoUnidades.guardar(urbana);

        repoTarifas = new RepositorioTarifas();
        repoTarifas.guardarTarifaBase(EXTRA, d("100.00"));
        ServicioTarifas tarifas = new ServicioTarifas(repoTarifas, repoRutas, usuarios);

        repoReservas = new RepositorioReservas();
        repoMovimientos = new RepositorioMovimientos();
        monedero = new ServicioMonedero(repoMovimientos, RELOJ);
        servicio = new ServicioReservas(repoReservas, repoUnidades, repoRutas, usuarios, tarifas, monedero, RELOJ);
    }

    private void acreditarEstudiante(String monto) {
        assertTrue(monedero.acreditar("estudiante@ucv.com", d(monto), "Recarga de prueba", "REF-" + monto).isOk());
    }

    @Test
    @DisplayName("Reserva extraurbana aplica el descuento configurado por ServicioTarifas y emite ticket con el importe final")
    void reservaExtraurbanaCobraTarifaSolidaria() {
        acreditarEstudiante("100.00");

        var resultado = servicio.reservar(EXTRA, PLACA_EXTRA);

        assertTrue(resultado.isOk(), resultado.getMensaje());
        assertEquals("Solicitud de cupo realizada correctamente", resultado.getMensaje());
        assertEquals(d("85.00"), resultado.getValor().getImporteCobrado());
        assertEquals(d("85.00"), resultado.getValor().getTicketDeLaReserva().getImporteCobrado());
        assertEquals(d("15.00"), monedero.saldo("estudiante@ucv.com"));
        assertEquals(0, servicio.cuposDisponiblesRuta(EXTRA, HOY));
        assertFalse(resultado.getValor().getTicketDeLaReserva().getCodigo().isBlank());
    }

    @Test
    @DisplayName("Ruta urbana emite reserva y ticket sin cobrar")
    void rutaUrbanaNoCobra() {
        var resultado = servicio.reservar(URBANA, PLACA_URBANA);

        assertTrue(resultado.isOk(), resultado.getMensaje());
        assertEquals(d("0.00"), resultado.getValor().getImporteCobrado());
        assertEquals(d("0.00"), monedero.saldo("estudiante@ucv.com"));
        assertEquals(1, servicio.cuposDisponiblesRuta(URBANA, HOY));
    }

    @Test
    @DisplayName("Saldo insuficiente no crea reserva ni consume cupos")
    void saldoInsuficienteNoModificaEstado() {
        var resultado = servicio.reservar(EXTRA, PLACA_EXTRA);

        assertFalse(resultado.isOk());
        assertEquals("Saldo insuficiente, por favor recargue", resultado.getMensaje());
        assertTrue(repoReservas.listar().isEmpty());
        assertEquals(1, servicio.cuposDisponiblesRuta(EXTRA, HOY));
        assertEquals(d("0.00"), monedero.saldo("estudiante@ucv.com"));
    }

    @Test
    @DisplayName("No permite dos reservas activas del mismo usuario en la misma ruta y fecha")
    void rechazaReservaDuplicada() {
        acreditarEstudiante("100.00");
        var primera = servicio.reservar(EXTRA, PLACA_EXTRA);
        UnidadTransporte segundaUnidad = new UnidadTransporte("EXT456", "Bus extra alternativo", 3, EstadoUnidad.ACTIVO);
        segundaUnidad.setNombreRuta(EXTRA);
        repoUnidades.guardar(segundaUnidad);

        var segunda = servicio.reservar(EXTRA, "EXT456");

        assertTrue(primera.isOk());
        assertFalse(segunda.isOk());
        assertEquals("Ruta ya reservada anteriormente", segunda.getMensaje());
        assertEquals(1, repoReservas.listar().size());
        assertEquals(d("15.00"), monedero.saldo("estudiante@ucv.com"));
    }

    @Test
    @DisplayName("Cancelar reintegra exactamente lo cobrado y una segunda cancelación no reintegra otra vez")
    void cancelarReintegraUnaSolaVez() {
        acreditarEstudiante("100.00");
        var creado = servicio.reservar(EXTRA, PLACA_EXTRA);
        assertTrue(creado.isOk());
        String codigo = creado.getValor().getTicketDeLaReserva().getCodigo();

        var cancelacion = servicio.cancelar(codigo);
        var segundaCancelacion = servicio.cancelar(codigo);

        assertTrue(cancelacion.isOk(), cancelacion.getMensaje());
        assertEquals("Reserva cancelada con éxito", cancelacion.getMensaje());
        assertFalse(segundaCancelacion.isOk());
        assertEquals(d("100.00"), monedero.saldo("estudiante@ucv.com"));
        assertEquals(EstadoReserva.CANCELADA, repoReservas.buscarPorCodigo(codigo).getEstado());
        assertEquals(1, servicio.cuposDisponiblesRuta(EXTRA, HOY));
        assertEquals(3, monedero.historial("estudiante@ucv.com", 10).size());
    }

    @Test
    @DisplayName("Una unidad llena o fuera de servicio no aparece como disponible")
    void disponibilidadSeCalculaDesdeReservasYEstado() {
        acreditarEstudiante("100.00");
        assertTrue(servicio.reservar(EXTRA, PLACA_EXTRA).isOk());
        assertTrue(servicio.placasDisponiblesHoy(EXTRA).isEmpty());

        assertTrue(usuarios.iniciarSesion("empleado@ucv.com", DatosSemilla.CLAVE_DEMO).isOk());
        var sinPuesto = servicio.reservar(EXTRA, PLACA_EXTRA);

        assertFalse(sinPuesto.isOk());
        assertEquals(0, servicio.cuposDisponiblesRuta(EXTRA, HOY));
    }

    @Test
    @DisplayName("Público general paga el 100 por ciento de la tarifa extraurbana")
    void publicoGeneralPagaTarifaCompleta() {
        assertTrue(usuarios.iniciarSesion("publico@ucv.com", DatosSemilla.CLAVE_DEMO).isOk());
        assertTrue(monedero.acreditar("publico@ucv.com", d("120.00"), "Recarga", "REF-PUBLICO").isOk());

        var resultado = servicio.reservar(EXTRA, PLACA_EXTRA);

        assertTrue(resultado.isOk(), resultado.getMensaje());
        assertEquals(d("100.00"), resultado.getValor().getImporteCobrado());
        assertEquals(d("20.00"), monedero.saldo("publico@ucv.com"));
    }

    @Test
    @DisplayName("Una ruta extraurbana sin tarifa configurada no genera cobro")
    void rutaSinTarifaNoCobra() {
        String rutaSinTarifa = "Extra sin tarifa";
        repoRutas.guardar(new modelo.Ruta(rutaSinTarifa, modelo.TipoRuta.EXTRAURBANA,
                java.time.LocalTime.of(7, 0), java.time.LocalTime.of(19, 0)));
        UnidadTransporte unidad = new UnidadTransporte("EXT999", "Bus sin tarifa", 2, EstadoUnidad.ACTIVO);
        unidad.setNombreRuta(rutaSinTarifa);
        repoUnidades.guardar(unidad);

        var resultado = servicio.reservar(rutaSinTarifa, "EXT999");

        assertTrue(resultado.isOk(), resultado.getMensaje());
        assertEquals(d("0.00"), resultado.getValor().getImporteCobrado());
        assertEquals(d("0.00"), monedero.saldo("estudiante@ucv.com"));
    }

    @Test
    @DisplayName("Si falla el reintegro, la cancelación se revierte y no libera el cupo")
    void falloDeReintegroMantieneReservaActiva() {
        acreditarEstudiante("100.00");
        var creada = servicio.reservar(EXTRA, PLACA_EXTRA);
        assertTrue(creada.isOk());
        ServicioMonedero monederoConReintegroFallido = new ServicioMonedero(repoMovimientos, RELOJ) {
            @Override
            Resultado<Void> reintegrar(String correo, BigDecimal monto, String descripcion) {
                return Resultado.error("Fallo de reintegro simulado");
            }
        };
        ServicioReservas servicioCancelacion = new ServicioReservas(repoReservas, repoUnidades, repoRutas,
                usuarios, new ServicioTarifas(repoTarifas, repoRutas, usuarios), monederoConReintegroFallido, RELOJ);

        var resultado = servicioCancelacion.cancelar(creada.getValor().getTicketDeLaReserva().getCodigo());

        assertFalse(resultado.isOk());
        assertTrue(repoReservas.buscarPorCodigo(creada.getValor().getTicketDeLaReserva().getCodigo()).estaActiva());
        assertEquals(d("15.00"), monedero.saldo("estudiante@ucv.com"));
        assertEquals(0, servicio.cuposDisponiblesRuta(EXTRA, HOY));
    }

    @Test
    @DisplayName("El repositorio conserva reservas y estado de cancelación al recrearse")
    void reservasPersistenEntreInstancias() {
        acreditarEstudiante("100.00");
        var creado = servicio.reservar(EXTRA, PLACA_EXTRA);
        assertTrue(creado.isOk());
        Path archivo = tempDir.resolve("reservas.txt");
        RepositorioReservas persistente = new RepositorioReservas(archivo);
        persistente.guardar(creado.getValor());

        RepositorioReservas recuperado = new RepositorioReservas(archivo);
        Reserva reserva = recuperado.buscarPorCodigo(creado.getValor().getTicketDeLaReserva().getCodigo());

        assertNotNull(reserva);
        assertEquals(EXTRA, reserva.getNombreRuta());
        assertEquals(PLACA_EXTRA, reserva.getPlacaUnidad());
        assertEquals(HOY, reserva.getFechaServicio());
        assertEquals(d("85.00"), reserva.getImporteCobrado());
        assertEquals(EstadoReserva.ACTIVA, reserva.getEstado());
        recuperado.actualizar(reserva.conEstado(EstadoReserva.CANCELADA));
        assertEquals(EstadoReserva.CANCELADA,
                new RepositorioReservas(archivo).buscarPorCodigo(reserva.getTicketDeLaReserva().getCodigo()).getEstado());
    }

    @Test
    @DisplayName("Si no se puede persistir la reserva, el cobro exitoso se compensa")
    void falloAlGuardarCompensaElCobro() {
        acreditarEstudiante("100.00");
        IRepositorioReservas repositorioQueFalla = new RepositorioQueFallaAlGuardar(repoReservas);
        ServicioTarifas tarifas = new ServicioTarifas(repoTarifas, repoRutas, usuarios);
        ServicioReservas conFallo = new ServicioReservas(repositorioQueFalla, repoUnidades, repoRutas,
                usuarios, tarifas, monedero, RELOJ);

        var resultado = conFallo.reservar(EXTRA, PLACA_EXTRA);

        assertFalse(resultado.isOk());
        assertEquals(d("100.00"), monedero.saldo("estudiante@ucv.com"));
        assertTrue(repoReservas.listar().isEmpty());
        assertEquals(1, conFallo.cuposDisponiblesRuta(EXTRA, HOY));
    }

    @Test
    @DisplayName("Permite múltiples reservas activas en rutas diferentes el mismo día y cancelación selectiva")
    void multiplesReservasYCancelacionSelectiva() {
        acreditarEstudiante("150.00");

        var res1 = servicio.reservar(EXTRA, PLACA_EXTRA);
        assertTrue(res1.isOk(), res1.getMensaje());

        UnidadTransporte urbana2 = new UnidadTransporte("URB456", "Bus urbano 2", 5, EstadoUnidad.ACTIVO);
        urbana2.setNombreRuta(URBANA);
        repoUnidades.guardar(urbana2);

        var res2 = servicio.reservar(URBANA, "URB456");
        assertTrue(res2.isOk(), res2.getMensaje());

        List<Reserva> activas = servicio.reservasActivasDelDia();
        assertEquals(2, activas.size());

        var cancelacion = servicio.cancelar(res1.getValor().getTicketDeLaReserva().getCodigo());
        assertTrue(cancelacion.isOk(), cancelacion.getMensaje());

        assertEquals(1, servicio.reservasActivasDelDia().size());
        assertEquals(URBANA, servicio.reservasActivasDelDia().get(0).getNombreRuta());
        assertEquals(EstadoReserva.CANCELADA, repoReservas.buscarPorCodigo(res1.getValor().getTicketDeLaReserva().getCodigo()).getEstado());
    }

    @Test
    @DisplayName("Reservar 1 cupo reduce en 1 la disponibilidad de la ruta y cancelar la restaura")
    void disponibilidadRutaBajaAlReservarYSubeAlCancelar() {
        repoUnidades.guardar(unidadExtra("EXT456", 3, EstadoUnidad.ACTIVO)); // ruta con 1 + 3 cupos
        acreditarEstudiante("100.00");
        assertEquals(4, servicio.cuposDisponiblesRutaHoy(EXTRA));

        var reserva = servicio.reservar(EXTRA, "EXT456");
        assertTrue(reserva.isOk(), reserva.getMensaje());
        assertEquals(3, servicio.cuposDisponiblesRutaHoy(EXTRA));

        assertTrue(servicio.cancelar(reserva.getValor().getTicketDeLaReserva().getCodigo()).isOk());
        assertEquals(4, servicio.cuposDisponiblesRutaHoy(EXTRA));
    }

    @Test
    @DisplayName("Una unidad no activa no aporta disponibilidad a la ruta")
    void unidadInactivaNoAportaDisponibilidad() {
        repoUnidades.guardar(unidadExtra("EXT456", 3, EstadoUnidad.EN_MANTENIMIENTO));
        repoUnidades.guardar(unidadExtra("EXT789", 5, EstadoUnidad.FUERA_DE_SERVICIO));

        assertEquals(1, servicio.cuposDisponiblesRutaHoy(EXTRA)); // solo EXT123 (capacidad 1) está activa
        assertEquals(List.of(PLACA_EXTRA), servicio.placasDisponiblesHoy(EXTRA));
    }

    @Test
    @DisplayName("El importe estimado coincide con lo que cobra la reserva: descuento por rol, 0.00 en urbana, sin tarifa o sin sesión")
    void importeEstimadoUsaLaLogicaDeTarifas() {
        assertEquals(d("85.00"), servicio.importeEstimado(EXTRA));          // estudiante: 15 % de descuento sobre 100.00
        assertEquals(d("0.00"), servicio.importeEstimado(URBANA));
        assertEquals(d("0.00"), servicio.importeEstimado(null));
        assertEquals(d("0.00"), servicio.importeEstimado("Inventada"));

        acreditarEstudiante("100.00");
        var reserva = servicio.reservar(EXTRA, PLACA_EXTRA);
        assertEquals(servicio.importeEstimado(EXTRA), reserva.getValor().getImporteCobrado());

        assertTrue(usuarios.iniciarSesion("publico@ucv.com", DatosSemilla.CLAVE_DEMO).isOk());
        assertEquals(d("100.00"), servicio.importeEstimado(EXTRA));
        assertTrue(usuarios.iniciarSesion("empleado@ucv.com", DatosSemilla.CLAVE_DEMO).isOk());
        assertEquals(d("55.00"), servicio.importeEstimado(EXTRA));
        assertTrue(usuarios.iniciarSesion("conductor@ucv.com", DatosSemilla.CLAVE_DEMO).isOk());
        assertEquals(d("0.00"), servicio.importeEstimado(EXTRA));

        usuarios.cerrarSesion();
        assertEquals(d("0.00"), servicio.importeEstimado(EXTRA));
    }

    @Test
    @DisplayName("Si la unidad pasa a mantenimiento tras reservar, la reserva sigue activa, se ve el estado actual y se puede cancelar con reintegro")
    void cambioDeEstadoPosteriorNoCancelaLaReservaPeroSeVeElEstadoActual() {
        acreditarEstudiante("100.00");
        var reserva = servicio.reservar(EXTRA, PLACA_EXTRA);
        assertTrue(reserva.isOk(), reserva.getMensaje());
        assertEquals(EstadoUnidad.ACTIVO, servicio.estadoActualDeUnidad(PLACA_EXTRA));

        UnidadTransporte unidad = repoUnidades.buscarPorPlaca(PLACA_EXTRA);
        unidad.setEstado(EstadoUnidad.EN_MANTENIMIENTO);
        repoUnidades.actualizar(unidad);
        unidad.setNombreRuta(null); // incluso desasignada de la ruta se consulta por la placa guardada en la reserva
        repoUnidades.actualizar(unidad);

        assertEquals(EstadoUnidad.EN_MANTENIMIENTO, servicio.estadoActualDeUnidad(PLACA_EXTRA));
        List<Reserva> activas = servicio.reservasActivasDelDia();
        assertEquals(1, activas.size());                       // no se canceló sola
        assertEquals(PLACA_EXTRA, activas.get(0).getPlacaUnidad());
        assertEquals(d("15.00"), monedero.saldo("estudiante@ucv.com")); // sin reintegro automático
        assertEquals(0, servicio.cuposDisponiblesRutaHoy(EXTRA));       // unidad no activa: sin disponibilidad

        var cancelacion = servicio.cancelar(activas.get(0).getTicketDeLaReserva().getCodigo());
        assertTrue(cancelacion.isOk(), cancelacion.getMensaje());
        assertEquals(d("100.00"), monedero.saldo("estudiante@ucv.com")); // reintegro por el flujo seguro del servicio
        assertTrue(servicio.reservasActivasDelDia().isEmpty());
    }

    @Test
    @DisplayName("El estado actual de una placa inexistente o vacía es null")
    void estadoActualDePlacaDesconocida() {
        assertNull(servicio.estadoActualDeUnidad("NOEXISTE"));
        assertNull(servicio.estadoActualDeUnidad(null));
        assertNull(servicio.estadoActualDeUnidad("  "));
    }

    private static UnidadTransporte unidadExtra(String placa, int capacidad, EstadoUnidad estado) {
        UnidadTransporte unidad = new UnidadTransporte(placa, "Bus extraurbano", capacidad, estado);
        unidad.setNombreRuta(EXTRA);
        return unidad;
    }

    private static final class RepositorioQueFallaAlGuardar implements IRepositorioReservas {
        private final IRepositorioReservas delegado;
        private RepositorioQueFallaAlGuardar(IRepositorioReservas delegado) { this.delegado = delegado; }
        @Override public void guardar(Reserva reserva) { throw new IllegalStateException("Fallo de persistencia simulado"); }
        @Override public void actualizar(Reserva reserva) { delegado.actualizar(reserva); }
        @Override public Reserva buscarPorCodigo(String codigo) { return delegado.buscarPorCodigo(codigo); }
        @Override public boolean existeCodigoTicket(String codigo) { return delegado.existeCodigoTicket(codigo); }
        @Override public List<Reserva> listar() { return delegado.listar(); }
        @Override public List<Reserva> listarPorUsuario(String correo) { return delegado.listarPorUsuario(correo); }
        @Override public List<Reserva> listarActivasPorUsuarioYFecha(String correo, LocalDate fecha) {
            return delegado.listarActivasPorUsuarioYFecha(correo, fecha);
        }
        @Override public List<Reserva> listarActivasPorRutaYFecha(String ruta, LocalDate fecha) {
            return delegado.listarActivasPorRutaYFecha(ruta, fecha);
        }
        @Override public List<Reserva> listarActivasPorUnidadYFecha(String placa, LocalDate fecha) {
            return delegado.listarActivasPorUnidadYFecha(placa, fecha);
        }
    }
}