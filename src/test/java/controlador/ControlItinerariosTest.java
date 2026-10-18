package controlador;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;
import java.util.List;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;
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
import vista.PlantillaVista.PanelEstadoUnidad;
import vista.PlantillaVista.PanelGestionRutas;
import vista.PlantillaVista.CuerposDelSistema.CuerpoRegistroRuta;

/** Selección de filas en Control de Itinerarios y tabla de disponibilidad del pasajero (sin ventanas). */
class ControlItinerariosTest {

    private ServicioUsuarios usuarios;
    private ServicioReservas reservas;
    private RepositorioUnidades repoUnidades;
    private ControlItinerarios control;
    private CuerpoRegistroRuta cuerpo;
    private PanelGestionRutas panel;

    @BeforeEach
    void preparar() {
        RepositorioUsuarios repoUsuarios = new RepositorioUsuarios();
        DatosSemilla.sembrarUsuarios(repoUsuarios);
        usuarios = new ServicioUsuarios(repoUsuarios);
        usuarios.iniciarSesion("admin@ucv.com", DatosSemilla.CLAVE_DEMO);

        RepositorioRutas repoRutas = new RepositorioRutas();
        repoRutas.guardar(new Ruta("Urbana Norte", TipoRuta.URBANA, LocalTime.of(6, 0), LocalTime.of(20, 0)));
        repoRutas.guardar(new Ruta("Extra Sur", TipoRuta.EXTRAURBANA, LocalTime.of(7, 30), LocalTime.of(18, 45)));
        repoUnidades = new RepositorioUnidades();
        UnidadTransporte activa = new UnidadTransporte("URB111", "Bus", 5, EstadoUnidad.ACTIVO);
        activa.setNombreRuta("Urbana Norte");
        repoUnidades.guardar(activa);
        UnidadTransporte enTaller = new UnidadTransporte("URB222", "Bus", 4, EstadoUnidad.EN_MANTENIMIENTO);
        enTaller.setNombreRuta("Urbana Norte");
        repoUnidades.guardar(enTaller);

        RepositorioTarifas repoTarifas = new RepositorioTarifas();
        ServicioTarifas tarifas = new ServicioTarifas(repoTarifas, repoRutas, usuarios);
        ServicioMonedero monedero = new ServicioMonedero(new RepositorioMovimientos());
        reservas = new ServicioReservas(new RepositorioReservas(), repoUnidades, repoRutas, usuarios, tarifas, monedero);
        ControlFlota controlFlota = new ControlFlota(new ServicioFlota(repoUnidades, usuarios));
        control = new ControlItinerarios(new ServicioRutas(repoRutas, repoUnidades, repoTarifas, usuarios),
                new ServicioAsignaciones(repoRutas, repoUnidades, usuarios, usuarios), usuarios, controlFlota, reservas);

        cuerpo = new CuerpoRegistroRuta();
        panel = new PanelGestionRutas();
        control.conectar(cuerpo, panel);
    }

    private int filaDe(javax.swing.table.DefaultTableModel modelo, String ruta) {
        for (int i = 0; i < modelo.getRowCount(); i++) {
            if (ruta.equals(modelo.getValueAt(i, 0))) return i;
        }
        throw new AssertionError("ruta no listada: " + ruta);
    }

    @Test
    void seleccionarUnaFilaRellenaLaRedefinicionYSincronizaElPanelDeAsignacion() {
        int fila = filaDe(cuerpo.getModeloTablaRutas(), "Extra Sur");
        cuerpo.getTablaRutas().setRowSelectionInterval(fila, fila);

        assertEquals("Extra Sur", cuerpo.getRutaRedefinirElegida());
        assertEquals("Extra Sur", cuerpo.getcampoNombreRuta().getText());
        assertEquals("07:30", cuerpo.getInicioJornadaSeleccionada());
        assertEquals("18:45", cuerpo.getFinalJornadaSeleccionada());
        assertEquals("Extraurbana", cuerpo.getTipoRutaSeleccionada());
        assertEquals("Extra Sur", panel.getRutaElegida());
    }

    @Test
    void volverASeleccionarLaMismaRutaRestauraLosCamposEditados() {
        int fila = filaDe(cuerpo.getModeloTablaRutas(), "Extra Sur");
        cuerpo.getTablaRutas().setRowSelectionInterval(fila, fila);
        cuerpo.getcampoNombreRuta().setText("texto editado a mano");

        cuerpo.getTablaRutas().clearSelection();
        cuerpo.getTablaRutas().setRowSelectionInterval(fila, fila);

        assertEquals("Extra Sur", cuerpo.getcampoNombreRuta().getText());
    }

    @Test
    void respetaLaConversionEntreIndiceDeVistaYDeModelo() {
        TableRowSorter<?> orden = new TableRowSorter<>(cuerpo.getModeloTablaRutas());
        cuerpo.getTablaRutas().setRowSorter(orden);
        orden.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.DESCENDING)));

        cuerpo.getTablaRutas().setRowSelectionInterval(0, 0); // primera fila de la vista: "Urbana Norte"

        assertEquals("Urbana Norte", cuerpo.getRutaRedefinirElegida());
        assertEquals("Urbana Norte", panel.getRutaElegida());
        assertEquals("Urbana", cuerpo.getTipoRutaSeleccionada());
    }

    @Test
    void elBotonLimpiarDejaElFormularioYLaSeleccionEnBlanco() {
        int fila = filaDe(cuerpo.getModeloTablaRutas(), "Extra Sur");
        cuerpo.getTablaRutas().setRowSelectionInterval(fila, fila);
        assertEquals("Extra Sur", cuerpo.getcampoNombreRuta().getText());

        cuerpo.getBotonLimpiar().doClick();

        assertEquals("", cuerpo.getcampoNombreRuta().getText());
        assertEquals("00:00", cuerpo.getInicioJornadaSeleccionada());
        assertEquals("00:00", cuerpo.getFinalJornadaSeleccionada());
        assertEquals("Ninguno", cuerpo.getTipoRutaSeleccionada());
        assertNull(cuerpo.getRutaRedefinirElegida());
        assertEquals(-1, cuerpo.getTablaRutas().getSelectedRow());
    }

    @Test
    void conectarOtraPantallaNoDuplicaLosListenersDeLaAnterior() {
        CuerpoRegistroRuta otro = new CuerpoRegistroRuta();
        PanelGestionRutas otroPanel = new PanelGestionRutas();
        control.conectar(otro, otroPanel);

        int fila = filaDe(cuerpo.getModeloTablaRutas(), "Extra Sur");
        cuerpo.getTablaRutas().setRowSelectionInterval(fila, fila);

        assertEquals("Extra Sur", cuerpo.getRutaRedefinirElegida());
        assertNull(otro.getRutaRedefinirElegida()); // la pantalla nueva no reacciona a la tabla de la vieja
        assertNull(otroPanel.getRutaElegida());
    }

    @Test
    void tablaDelPasajeroSeparaCapacidadTotalDeDisponiblesYSoloCuentaUnidadesActivas() {
        vista.PlantillaVista.CuerposDelSistema.CuerpoFuncionesUsuario pasajero =
                new vista.PlantillaVista.CuerposDelSistema.CuerpoFuncionesUsuario();
        usuarios.cerrarSesion();
        usuarios.iniciarSesion("estudiante@ucv.com", DatosSemilla.CLAVE_DEMO);

        control.llenarTablaRutasPasajero(pasajero.getModeloTablaRutas());
        var tabla = pasajero.getModeloTablaRutas();
        int fila = filaDe(tabla, "Urbana Norte");

        assertEquals("Capacidad total", tabla.getColumnName(5));
        assertEquals("Puestos disponibles", tabla.getColumnName(6));
        assertEquals(5, tabla.getValueAt(fila, 5)); // capacidad total solo de unidades activas (la de mantenimiento no cuenta)
        assertEquals(5, tabla.getValueAt(fila, 6)); // solo la unidad activa aporta

        assertTrue(reservas.reservar("Urbana Norte", "URB111").isOk());
        control.llenarTablaRutasPasajero(tabla);
        fila = filaDe(tabla, "Urbana Norte");
        assertEquals(5, tabla.getValueAt(fila, 5));
        assertEquals(4, tabla.getValueAt(fila, 6));

        assertEquals(0, tabla.getValueAt(filaDe(tabla, "Extra Sur"), 6)); // ruta sin unidades
    }
}
