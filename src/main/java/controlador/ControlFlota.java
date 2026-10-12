package controlador;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import modelo.EstadoUnidad;
import modelo.Resultado;
import modelo.UnidadTransporte;
import servicio.ServicioFlota;
import vista.PlantillaVista.PanelEstadoUnidad;
import vista.PlantillaVista.CuerposDelSistema.CuerpoRegistroUnidad;

/** HU09: Gestión de Flota (registrar unidades y cambiar su estado operativo). */
class ControlFlota {

    private final ServicioFlota servicioFlota;

    ControlFlota(ServicioFlota servicioFlota) {
        this.servicioFlota = servicioFlota;
    }

    void conectar(CuerpoRegistroUnidad cuerpo, PanelEstadoUnidad panel) {
        Runnable refrescar = () -> {
            llenarTablaUnidades(cuerpo.getModeloTablaUnidades());
            panel.actualizarUnidades(placasDeUnidades());
        };
        refrescar.run();

        // HU09: registrar unidad
        cuerpo.getBotonConfirmar().addActionListener(e -> {
            Resultado<Void> r = servicioFlota.registrarUnidad(
                    cuerpo.getcampoPlaca().getText(),
                    cuerpo.getcampoModelo().getText(),
                    cuerpo.getcampoCapacidad().getText(),
                    EstadoUnidad.desdeTexto(cuerpo.getEstadoSeleccionado())); // "Ninguno" llega como null
            JOptionPane.showMessageDialog(cuerpo, r.getMensaje());
            if (r.isOk()) {
                cuerpo.getcampoPlaca().setText("");
                cuerpo.getcampoModelo().setText("");
                cuerpo.getcampoCapacidad().setText("");
                refrescar.run();
            }
        });

        // Gestión de flota: cambiar el estado operativo de una unidad registrada
        panel.getBotonCambiarEstado().addActionListener(e -> {
            Resultado<Void> r = servicioFlota.cambiarEstado(
                    panel.getUnidadElegida(), EstadoUnidad.desdeTexto(panel.getEstadoElegido()));
            JOptionPane.showMessageDialog(cuerpo, r.getMensaje());
            if (r.isOk()) {
                refrescar.run();
            }
        });
    }

    List<String> placasDeUnidades() {
        List<String> placas = new ArrayList<>();
        for (UnidadTransporte unidad : servicioFlota.listarUnidades()) {
            placas.add(unidad.getPlaca());
        }
        return placas;
    }

    private void llenarTablaUnidades(DefaultTableModel modelo) {
        modelo.setRowCount(0);
        for (UnidadTransporte u : servicioFlota.listarUnidades()) {
            modelo.addRow(new Object[] {u.getPlaca(), u.getModelo(), u.getCapacidad(), u.getEstado().getEtiqueta()});
        }
    }
}
