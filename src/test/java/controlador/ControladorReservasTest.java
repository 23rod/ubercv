package controlador;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.Component;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import modelo.EstadoUnidad;
import modelo.Ruta;
import modelo.TipoRuta;
import modelo.UnidadTransporte;
import persistencia.memoria.DatosSemilla;
import persistencia.memoria.RepositorioMovimientos;
import persistencia.memoria.RepositorioReservas;
import persistencia.memoria.RepositorioRutas;
import persistencia.memoria.RepositorioTarifas;
import persistencia.memoria.RepositorioUnidades;
import persistencia.memoria.RepositorioUsuarios;
import servicio.ServicioAsignaciones;
import servicio.ServicioFlota;
import servicio.ServicioMonedero;
import servicio.ServicioReservas;
import servicio.ServicioRutas;
import servicio.ServicioTarifas;
import servicio.ServicioUsuarios;
import vista.PlantillaVista.CuerposDelSistema.CuerpoFuncionesUsuario;

/** Flujo de reservas del pasajero con diálogos simulados (sin ventanas reales). */
class ControladorReservasTest {

    private static final String URB1 = "Urbana Norte";
    private static final String URB2 = "Urbana Sur";
    private static final String EXTRA = "Extra Este";

    /** Diálogos falsos: registran los mensajes y responden a las confirmaciones con un valor fijado por la prueba. */
    private static final class DialogosFalsos implements ControladorReservas.Dialogos {
        final List<String> mensajes = new ArrayList<>();
        int confirmaciones;
        boolean respuesta = true;

        @Override
        public void mensaje(Component padre, String texto) {
            mensajes.add(texto);
        }

        @Override
        public boolean confirmar(Component padre, String texto, String titulo) {
            confirmaciones++;
            return respuesta;
        }
    }

    private RepositorioUnidades repoUnidades;
    private ServicioReservas reservas;
    private ServicioUsuarios usuarios;
    private CuerpoFuncionesUsuario vista;
    private DialogosFalsos dialogos;
    private ControladorReservas controlador;
    private int cambiosDeSaldo;

    @BeforeEach
    void preparar() {
        RepositorioUsuarios repoUsuarios = new RepositorioUsuarios();
        DatosSemilla.sembrarUsuarios(repoUsuarios);
        usuarios = new ServicioUsuarios(repoUsuarios);
        usuarios.iniciarSesion("estudiante@ucv.com", DatosSemilla.CLAVE_DEMO);

        RepositorioRutas repoRutas = new RepositorioRutas();
        repoRutas.guardar(new Ruta(URB1, TipoRuta.URBANA, LocalTime.of(6, 0), LocalTime.of(20, 0)));
        repoRutas.guardar(new Ruta(URB2, TipoRuta.URBANA, LocalTime.of(6, 0), LocalTime.of(20, 0)));
        repoRutas.guardar(new Ruta(EXTRA, TipoRuta.EXTRAURBANA, LocalTime.of(6, 0), LocalTime.of(20, 0)));
        repoUnidades = new RepositorioUnidades();
        asignar("URB111", 3, URB1);
        asignar("URB222", 2, URB2);
        asignar("EXT333", 2, EXTRA);

        RepositorioTarifas repoTarifas = new RepositorioTarifas();
        repoTarifas.guardarTarifaBase(EXTRA, new java.math.BigDecimal("100.00"));
        ServicioTarifas tarifas = new ServicioTarifas(repoTarifas, repoRutas, usuarios);
        reservas = new ServicioReservas(new RepositorioReservas(), repoUnidades, repoRutas, usuarios, tarifas,
                new ServicioMonedero(new RepositorioMovimientos()));
        ControlItinerarios itinerarios = new ControlItinerarios(
                new ServicioRutas(repoRutas, repoUnidades, repoTarifas, usuarios),
                new ServicioAsignaciones(repoRutas, repoUnidades, usuarios, usuarios), usuarios,
                new ControlFlota(new ServicioFlota(repoUnidades, usuarios)), reservas);

        vista = new CuerpoFuncionesUsuario();
        dialogos = new DialogosFalsos();
        controlador = new ControladorReservas(reservas, vista,
                () -> itinerarios.llenarTablaRutasPasajero(vista.getModeloTablaRutas()),
                () -> cambiosDeSaldo++, dialogos);
        controlador.conectar();
    }

    private void asignar(String placa, int capacidad, String ruta) {
        UnidadTransporte unidad = new UnidadTransporte(placa, "Bus", capacidad, EstadoUnidad.ACTIVO);
        unidad.setNombreRuta(ruta);
        repoUnidades.guardar(unidad);
    }

    private int filaRuta(String ruta) {
        DefaultTableModel modelo = vista.getModeloTablaRutas();
        for (int i = 0; i < modelo.getRowCount(); i++) {
            if (ruta.equals(modelo.getValueAt(i, 0))) return i;
        }
        throw new AssertionError("ruta no listada: " + ruta);
    }

    private void seleccionarRuta(String ruta) {
        int fila = filaRuta(ruta);
        vista.getTablaRutas().setRowSelectionInterval(fila, fila);
    }

    private Object disponibles(String ruta) {
        return vista.getModeloTablaRutas().getValueAt(filaRuta(ruta), 6);
    }

    private void reservar(String ruta) {
        seleccionarRuta(ruta);
        vista.getBotonConfirmarReserva().doClick();
    }

    private void seleccionarReserva(int fila) {
        vista.getTablaReservaActual().setRowSelectionInterval(fila, fila);
    }

    // ------------------------------------------------------------------------------------ B. disponibilidad
    @Test
    void tablaMuestraCapacidadTotalYDisponiblesPorSeparado() {
        assertEquals("Capacidad total", vista.getModeloTablaRutas().getColumnName(5));
        assertEquals("Puestos disponibles", vista.getModeloTablaRutas().getColumnName(6));
        assertEquals(3, vista.getModeloTablaRutas().getValueAt(filaRuta(URB1), 5));
        assertEquals(3, disponibles(URB1));
    }

    @Test
    void reservarReduceEnUnoLaDisponibilidadVisibleYCancelarLaRestauraConservandoLaRuta() {
        reservar(URB1);

        assertEquals(2, disponibles(URB1));
        assertEquals(3, vista.getModeloTablaRutas().getValueAt(filaRuta(URB1), 5)); // la capacidad total no cambia
        assertEquals(URB1, vista.getModeloTablaRutas().getValueAt(vista.getTablaRutas().getSelectedRow(), 0));
        assertEquals(1, cambiosDeSaldo);

        vista.getBotonEliminarReserva().doClick(); // una sola reserva activa: queda seleccionada sin ambigüedad

        assertEquals(3, disponibles(URB1));
        assertEquals(2, cambiosDeSaldo);
    }

    @Test
    void refrescarVuelveALeerLaDisponibilidadAlRegresarALaPantalla() {
        assertTrue(reservas.reservar(URB2, "URB222").isOk()); // reserva hecha por otra vía mientras la pantalla estaba cerrada
        assertEquals(2, disponibles(URB2)); // todavía no se releyó

        controlador.refrescar();

        assertEquals(1, disponibles(URB2));
    }

    // ------------------------------------------------------------------------------------ D. precio
    @Test
    void laEtiquetaDePrecioSigueLaRutaSeleccionadaConElImporteFinalDelServicio() {
        seleccionarRuta(EXTRA);
        assertEquals("Precio a pagar: 15.00", vista.getLabelPrecioReserva().getText()); // estudiante: 85 % de descuento

        seleccionarRuta(URB1);
        assertEquals("Precio a pagar: 0.00 (sin cobro)", vista.getLabelPrecioReserva().getText());
    }

    // ------------------------------------------------------------------------------------ F. cancelar
    @Test
    void sinSeleccionValidaNoSeCancelaLaPrimeraReservaPorDefecto() {
        reservar(URB1);
        reservar(URB2);
        assertEquals(2, reservas.reservasActivasDelDia().size());
        vista.getTablaReservaActual().clearSelection();
        dialogos.mensajes.clear();

        controlador.refrescar(); // con 2 reservas no hay selección automática
        assertFalse(vista.getBotonEliminarReserva().isEnabled());

        vista.getBotonEliminarReserva().getActionListeners()[0].actionPerformed(null); // incluso forzando el clic
        assertEquals(0, dialogos.confirmaciones);
        assertEquals(2, reservas.reservasActivasDelDia().size());
        assertEquals("Seleccione en la tabla la reserva que desea cancelar.", dialogos.mensajes.get(dialogos.mensajes.size() - 1));
    }

    @Test
    void cancelaSoloLaReservaSeleccionadaYSoloTrasConfirmar() {
        reservar(URB1);
        reservar(URB2);
        controlador.refrescar();
        String segundaRuta = (String) vista.getFilasTablaReservaActual().getValueAt(1, 0);

        seleccionarReserva(1);
        assertTrue(vista.getBotonEliminarReserva().isEnabled());

        dialogos.respuesta = false; // el pasajero se arrepiente
        vista.getBotonEliminarReserva().doClick();
        assertEquals(1, dialogos.confirmaciones);
        assertEquals(2, reservas.reservasActivasDelDia().size());

        dialogos.respuesta = true;
        vista.getBotonEliminarReserva().doClick();
        assertEquals(2, dialogos.confirmaciones);
        assertEquals(1, reservas.reservasActivasDelDia().size());
        assertNotEquals(segundaRuta, reservas.reservasActivasDelDia().get(0).getNombreRuta());
    }

    // ------------------------------------------------------------------------------------ E. estado actual
    @Test
    void siLaUnidadPasaAMantenimientoLaReservaSigueVisibleEnRojoConTextoYSePuedeCancelar() {
        reservar(URB1);
        assertEquals("Activo", vista.getFilasTablaReservaActual().getValueAt(0, 5));

        UnidadTransporte unidad = repoUnidades.buscarPorPlaca("URB111");
        unidad.setEstado(EstadoUnidad.EN_MANTENIMIENTO); // el administrador cambia el estado desde su pantalla
        repoUnidades.actualizar(unidad);
        controlador.refrescar(); // equivale a volver a entrar como pasajero

        assertEquals(1, reservas.reservasActivasDelDia().size()); // no se canceló sola
        String estado = (String) vista.getFilasTablaReservaActual().getValueAt(0, 5);
        assertEquals("En mantenimiento" + CuerpoFuncionesUsuario.MARCA_NO_OPERATIVA, estado); // texto, no solo color
        javax.swing.JTable tabla = vista.getTablaReservaActual();
        Color rojo = tabla.prepareRenderer(tabla.getCellRenderer(0, 5), 0, 5).getForeground();
        assertTrue(rojo.getRed() > 150 && rojo.getGreen() < 50 && rojo.getBlue() < 50);
        assertEquals(0, disponibles(URB1)); // la unidad no activa ya no aporta puestos

        assertTrue(vista.getBotonEliminarReserva().isEnabled());
        vista.getBotonEliminarReserva().doClick();
        assertTrue(reservas.reservasActivasDelDia().isEmpty());
    }

    @Test
    void elEstadoActualSeConsultaPorLaPlacaGuardadaAunqueLaUnidadYaNoEsteAsignadaALaRuta() {
        reservar(URB1);
        UnidadTransporte unidad = repoUnidades.buscarPorPlaca("URB111");
        unidad.setEstado(EstadoUnidad.FUERA_DE_SERVICIO);
        unidad.setNombreRuta(null);
        repoUnidades.actualizar(unidad);

        controlador.refrescar();

        assertEquals("Fuera de Servicio" + CuerpoFuncionesUsuario.MARCA_NO_OPERATIVA,
                vista.getFilasTablaReservaActual().getValueAt(0, 5));
    }

    @Test
    void conUnidadActivaElEstadoSeMuestraSoloEnLaTablaYConColorNormal() {
        reservar(URB1);
        assertEquals("Activo", vista.getFilasTablaReservaActual().getValueAt(0, 5));
        javax.swing.JTable tabla = vista.getTablaReservaActual();
        assertNotEquals(200, tabla.prepareRenderer(tabla.getCellRenderer(0, 5), 0, 5).getForeground().getRed());
    }
}
