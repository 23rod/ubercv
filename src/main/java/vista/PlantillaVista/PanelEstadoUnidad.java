package vista.PlantillaVista;

import java.awt.Color;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Panel inferior de la pantalla de flota: cambiar el estado operativo de una unidad ya registrada.
 * Solo dibuja los controles y expone getters; la lógica la maneja el Control.
 */
public class PanelEstadoUnidad extends JPanel {

    private final JComboBox<String> comboUnidades = new JComboBox<>();
    private final JComboBox<String> comboEstados =
            new JComboBox<>(new String[] {"Activo", "En mantenimiento", "Fuera de Servicio"});
    private final JButton botonCambiarEstado = new JButton("Cambiar estado");

    public PanelEstadoUnidad() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 8));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createTitledBorder("Cambiar el estado de una unidad"));

        add(new JLabel("Unidad (placa):"));
        add(comboUnidades);
        add(new JLabel("Nuevo estado:"));
        add(comboEstados);
        add(botonCambiarEstado);
    }

    // ---------------------------------------------------- Acceso para los controladores
    public JComboBox<String> getComboUnidades() {
        return comboUnidades;
    }

    public JComboBox<String> getComboEstados() {
        return comboEstados;
    }

    public String getUnidadElegida() {
        return (String) comboUnidades.getSelectedItem();
    }

    public String getEstadoElegido() {
        return (String) comboEstados.getSelectedItem();
    }

    public JButton getBotonCambiarEstado() {
        return botonCambiarEstado;
    }

    /** Recarga las placas conservando la selección anterior si todavía existe. */
    public void actualizarUnidades(List<String> placas) {
        Object anterior = comboUnidades.getSelectedItem();
        comboUnidades.removeAllItems();
        for (String placa : placas) {
            comboUnidades.addItem(placa);
        }
        if (anterior != null && placas.contains(anterior)) {
            comboUnidades.setSelectedItem(anterior);
        }
    }
}
