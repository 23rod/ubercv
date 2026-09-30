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
 * Panel inferior de la pantalla de rutas (HU10 redefinir y HU12 asignar unidades).
 * Solo dibuja los controles y expone getters; la lógica la maneja el Control.
 */
public class PanelGestionRutas extends JPanel {

    private final JComboBox<String> comboRutas = new JComboBox<>();
    private final JComboBox<String> comboUnidades = new JComboBox<>();
    private final JButton botonAsignar = new JButton("Asignar unidad a la ruta");
    private final JButton botonRedefinir = new JButton("Redefinir ruta");

    public PanelGestionRutas() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 8));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createTitledBorder("Asignar unidades y modificar rutas"));

        botonRedefinir.setToolTipText("Cambia la ruta elegida con el nombre, horario y tipo escritos en el formulario de arriba");

        add(new JLabel("Ruta:"));
        add(comboRutas);
        add(new JLabel("Unidad (placa):"));
        add(comboUnidades);
        add(botonAsignar);
        add(botonRedefinir);
    }

    // ---------------------------------------------------- Acceso para los controladores
    public JComboBox<String> getComboRutas() {
        return comboRutas;
    }

    public JComboBox<String> getComboUnidades() {
        return comboUnidades;
    }

    public String getRutaElegida() {
        return (String) comboRutas.getSelectedItem();
    }

    public String getUnidadElegida() {
        return (String) comboUnidades.getSelectedItem();
    }

    public JButton getBotonAsignar() {
        return botonAsignar;
    }

    public JButton getBotonRedefinir() {
        return botonRedefinir;
    }

    // ---------------------------------------------------- Métodos del panel
    public void actualizarRutas(List<String> nombres) {
        actualizar(comboRutas, nombres);
    }

    public void actualizarUnidades(List<String> placas) {
        actualizar(comboUnidades, placas);
    }

    /** Recarga un combo conservando la selección anterior si todavía existe. */
    private void actualizar(JComboBox<String> combo, List<String> valores) {
        Object anterior = combo.getSelectedItem();
        combo.removeAllItems();
        for (String valor : valores) {
            combo.addItem(valor);
        }
        if (anterior != null && valores.contains(anterior)) {
            combo.setSelectedItem(anterior);
        }
    }
}
