package vista.PlantillaVista.CuerposDelSistema;

import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import vista.PlantillaVista.CrearSubPanel;

/** Pantalla del administrador: operaciones de pago móvil de cada usuario (HU05). Solo dibuja y expone getters. */
public class CuerpoRecargasAdmin extends JPanel {

    private static final String SIN_USUARIO = "Ninguno";

    private JComboBox<String> plegableUsuarios;
    private JTable tablaRecargas;
    private DefaultTableModel filasTablaRecargas;

    public CuerpoRecargasAdmin() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setOpaque(true);
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.setBackground(new ColorUIResource(255, 255, 255));

        // Selector del usuario
        JLabel etiquetaUsuario = new JLabel("Elija el usuario: ");
        plegableUsuarios = new JComboBox<>(new String[] {SIN_USUARIO});
        JPanel panelUsuario = CrearSubPanel.subPanel();
        panelUsuario.add(etiquetaUsuario);
        panelUsuario.add(plegableUsuarios);

        // Tabla de operaciones del usuario elegido
        String[] nombreColumnas = {"Fecha", "Hora", "Teléfono", "Cédula", "Banco origen", "Banco destino", "Referencia", "Monto", "Resultado"};
        filasTablaRecargas = new DefaultTableModel(nombreColumnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaRecargas = new JTable(filasTablaRecargas);
        tablaRecargas.setRowHeight(30);
        tablaRecargas.getTableHeader().setReorderingAllowed(false);
        tablaRecargas.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // los nombres de banco son largos: la tabla se desplaza en horizontal
        int[] anchos = {90, 60, 110, 90, 260, 260, 130, 80, 330};
        for (int i = 0; i < anchos.length; i++) {
            tablaRecargas.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        DefaultTableCellRenderer centrar = new DefaultTableCellRenderer();
        centrar.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < tablaRecargas.getColumnCount(); i++) {
            tablaRecargas.getColumnModel().getColumn(i).setCellRenderer(centrar);
        }
        ((DefaultTableCellRenderer) tablaRecargas.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        JScrollPane tablaConScroll = new JScrollPane(tablaRecargas);
        tablaConScroll.setPreferredSize(new DimensionUIResource(920, 260));
        JPanel panelTabla = CrearSubPanel.subPanel();
        panelTabla.add(tablaConScroll);

        add(panelUsuario);
        add(panelTabla);
    }

    // ----------------------------------------- Acceso para los controladores ---------------------------------------------------------
    public JComboBox<String> getPlegableUsuarios() {
        return plegableUsuarios;
    }

    /** Correo del usuario elegido, o null si no hay ninguno elegido. */
    public String getUsuarioSeleccionado() {
        Object elegido = plegableUsuarios.getSelectedItem();
        if (elegido == null || SIN_USUARIO.equals(elegido)) {
            return null;
        }
        return elegido.toString();
    }

    /** Llena el selector con los usuarios que tienen operaciones (la primera opción siempre es "Ninguno"). */
    public void actualizarUsuarios(List<String> correos) {
        plegableUsuarios.removeAllItems();
        plegableUsuarios.addItem(SIN_USUARIO);
        for (String correo : correos) {
            plegableUsuarios.addItem(correo);
        }
    }

    public DefaultTableModel getModeloTabla() {
        return filasTablaRecargas;
    }
}
