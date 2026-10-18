package controlador;

import static org.junit.jupiter.api.Assertions.*;

import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import modelo.EstadoUnidad;
import modelo.UnidadTransporte;
import persistencia.memoria.DatosSemilla;
import persistencia.memoria.RepositorioUnidades;
import persistencia.memoria.RepositorioUsuarios;
import servicio.ServicioFlota;
import servicio.ServicioUsuarios;
import vista.PlantillaVista.PanelEstadoUnidad;
import vista.PlantillaVista.CuerposDelSistema.CuerpoRegistroUnidad;

/** Selección de filas en Gestión de Flota (sin ventanas: solo componentes Swing en memoria). */
class ControlFlotaTest {

    private CuerpoRegistroUnidad cuerpo;
    private PanelEstadoUnidad panel;

    @BeforeEach
    void preparar() {
        RepositorioUsuarios repoUsuarios = new RepositorioUsuarios();
        DatosSemilla.sembrarUsuarios(repoUsuarios);
        ServicioUsuarios usuarios = new ServicioUsuarios(repoUsuarios);
        usuarios.iniciarSesion("admin@ucv.com", DatosSemilla.CLAVE_DEMO);
        RepositorioUnidades repo = new RepositorioUnidades();
        repo.guardar(new UnidadTransporte("AAA111", "Bus A", 30, EstadoUnidad.ACTIVO));
        repo.guardar(new UnidadTransporte("BBB222", "Bus B", 30, EstadoUnidad.EN_MANTENIMIENTO));
        repo.guardar(new UnidadTransporte("CCC333", "Bus C", 30, EstadoUnidad.FUERA_DE_SERVICIO));

        cuerpo = new CuerpoRegistroUnidad();
        panel = new PanelEstadoUnidad();
        new ControlFlota(new ServicioFlota(repo, usuarios)).conectar(cuerpo, panel);
    }

    private int filaDe(String placa) {
        for (int i = 0; i < cuerpo.getModeloTablaUnidades().getRowCount(); i++) {
            if (placa.equals(cuerpo.getModeloTablaUnidades().getValueAt(i, 0))) return i;
        }
        throw new AssertionError("placa no listada: " + placa);
    }

    @Test
    void seleccionarUnaFilaAutoseleccionaLaPlacaYSuEstadoActual() {
        int fila = filaDe("BBB222");
        cuerpo.getTablaUnidades().setRowSelectionInterval(fila, fila);

        assertEquals("BBB222", panel.getUnidadElegida());
        assertEquals("En mantenimiento", panel.getEstadoElegido());

        fila = filaDe("CCC333");
        cuerpo.getTablaUnidades().setRowSelectionInterval(fila, fila);
        assertEquals("CCC333", panel.getUnidadElegida());
        assertEquals("Fuera de Servicio", panel.getEstadoElegido());

        fila = filaDe("AAA111");
        cuerpo.getTablaUnidades().setRowSelectionInterval(fila, fila);
        assertEquals("AAA111", panel.getUnidadElegida());
        assertEquals("Activo", panel.getEstadoElegido());
    }

    @Test
    void respetaLaConversionEntreIndiceDeVistaYDeModelo() {
        TableRowSorter<?> orden = new TableRowSorter<>(cuerpo.getModeloTablaUnidades());
        cuerpo.getTablaUnidades().setRowSorter(orden);
        orden.setSortKeys(List.of(new javax.swing.RowSorter.SortKey(0, SortOrder.DESCENDING)));

        cuerpo.getTablaUnidades().setRowSelectionInterval(0, 0); // en la vista la primera ahora es CCC333

        assertEquals("CCC333", panel.getUnidadElegida());
        assertEquals("Fuera de Servicio", panel.getEstadoElegido());
    }

    @Test
    void limpiarLaSeleccionNoCambiaLoQueEligioElAdministrador() {
        int fila = filaDe("BBB222");
        cuerpo.getTablaUnidades().setRowSelectionInterval(fila, fila);

        cuerpo.getTablaUnidades().clearSelection();

        assertEquals("BBB222", panel.getUnidadElegida());
    }
}
