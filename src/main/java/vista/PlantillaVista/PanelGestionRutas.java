package vista.PlantillaVista;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class PanelGestionRutas extends JPanel {

    private static final String NINGUNO = "Ninguno";

    private final JComboBox<String> comboRutas = new JComboBox<>();
    private final JComboBox<String> comboUnidades = new JComboBox<>();
    private final JComboBox<String> comboConductores = new JComboBox<>();

    private final JButton botonAsignar = new JButton("Asignar unidad a la ruta");
    private final JButton botonAsignarConductor = new JButton("Asignar conductor a la unidad");
    private final JButton botonRedefinir = new JButton("Redefinir ruta");

    private final JLabel etiquetaGuia = new JLabel("", SwingConstants.CENTER);

    public PanelGestionRutas() {
        setLayout(new GridLayout(3, 1, 0, 2));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(6, 10, 10, 10));

        etiquetaGuia.setFont(new Font("Arial", Font.ITALIC, 12));
        etiquetaGuia.setForeground(new Color(100, 100, 100));

        // Fila 1: Los 3 desplegables juntos
        JPanel filaCombos = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));
        filaCombos.setBackground(Color.WHITE);
        filaCombos.add(new JLabel("Ruta:"));
        filaCombos.add(comboRutas);
        filaCombos.add(new JLabel("Unidad (placa):"));
        filaCombos.add(comboUnidades);
        filaCombos.add(new JLabel("Conductor (cédula):"));
        filaCombos.add(comboConductores);

        // Fila 2: Los 3 botones de acción juntos
        JPanel filaBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));
        filaBotones.setBackground(Color.WHITE);
        filaBotones.add(botonAsignar);
        filaBotones.add(botonAsignarConductor);
        filaBotones.add(botonRedefinir);

        ActionListener evaluador = e -> actualizarEstadoVisual();
        comboRutas.addActionListener(evaluador);
        comboUnidades.addActionListener(evaluador);
        comboConductores.addActionListener(evaluador);

        add(etiquetaGuia);
        add(filaCombos);
        add(filaBotones);

        actualizarEstadoVisual();
    }

    private void actualizarEstadoVisual() {
        boolean hayRuta = getRutaElegida() != null;
        boolean hayUnidad = getUnidadElegida() != null;
        boolean hayConductor = getConductorElegido() != null;

        botonRedefinir.setEnabled(hayRuta);
        botonAsignar.setEnabled(hayRuta && hayUnidad);
        botonAsignarConductor.setEnabled(hayRuta && hayUnidad && hayConductor);

        if (!hayRuta) {
            etiquetaGuia.setText("Paso 1: Seleccione una Ruta para habilitar las opciones de gestión.");
        } else if (!hayUnidad) {
            etiquetaGuia.setText("Puede redefinir la ruta con los datos de arriba, o elegir una Unidad para asignarla.");
        } else if (!hayConductor) {
            etiquetaGuia.setText("Puede asignar la unidad a la ruta, o elegir un Conductor para asignarlo a esa unidad.");
        } else {
            etiquetaGuia.setText("Todo seleccionado: puede asignar la unidad a la ruta o asignarle el conductor elegido.");
        }
    }

    // ---------------------------------------------------- Acceso para el Control
    public JComboBox<String> getComboRutas() {
        return comboRutas;
    }

    public JComboBox<String> getComboUnidades() {
        return comboUnidades;
    }

    public JComboBox<String> getComboConductores() {
        return comboConductores;
    }

    public String getRutaElegida() {
        return valorValido(comboRutas);
    }

    public String getUnidadElegida() {
        return valorValido(comboUnidades);
    }

    public String getConductorElegido() {
        return valorValido(comboConductores);
    }

    public JButton getBotonAsignar() {
        return botonAsignar;
    }

    public JButton getBotonRedefinir() {
        return botonRedefinir;
    }

    public JButton getBotonAsignarConductor() {
        return botonAsignarConductor;
    }

    // ---------------------------------------------------- Actualización de datos
    public void actualizarRutas(List<String> nombres) {
        actualizar(comboRutas, nombres);
    }

    public void actualizarUnidades(List<String> placas) {
        actualizar(comboUnidades, placas);
    }

    public void actualizarConductores(List<String> cedulas) {
        actualizar(comboConductores, cedulas);
    }

    private String valorValido(JComboBox<String> combo) {
        Object item = combo.getSelectedItem();
        if (item == null || NINGUNO.equals(item.toString())) {
            return null;
        }
        return item.toString();
    }

    private void actualizar(JComboBox<String> combo, List<String> valores) {
        Object anterior = combo.getSelectedItem();
        combo.removeAllItems();
        combo.addItem(NINGUNO);
        for (String valor : valores) {
            combo.addItem(valor);
        }
        if (anterior != null && !NINGUNO.equals(anterior) && valores.contains(anterior)) {
            combo.setSelectedItem(anterior);
        } else {
            combo.setSelectedItem(NINGUNO);
        }
        actualizarEstadoVisual();
    }
}