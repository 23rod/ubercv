package vista.PlantillaVista.CuerposDelSistema;

import vista.PlantillaVista.CrearSubPanel;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class CuerpoRegistroRuta extends JPanel {

    private static final String ELIJA_RUTA = "Ninguna";

    private JTextField campoNombreRuta;
    private JTextField campoInicioJornada;
    private JTextField campoFinalJornada;
    private JComboBox<String> plegableRuta;
    private JComboBox<String> comboRutasRedefinir;
    private JButton botonGuardarRuta;
    private JButton botonRedefinir;
    private JTable tablaRutas;
    private DefaultTableModel filasTablaRutas;

    public CuerpoRegistroRuta(){
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Extencion a todo lo alto disponible de la pagina
        this.setOpaque(true);
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.setBackground(new ColorUIResource(255,255,255));
        
        // Creacion del placeholder Nombre Ruta
        JLabel nombreRutaLabel = new JLabel("Nombre Ruta:"); //disposición en el panel contenedor
        campoNombreRuta = new JTextField(20);
        JPanel panelNombreRuta = CrearSubPanel.subPanel();
        panelNombreRuta.add(nombreRutaLabel);
        panelNombreRuta.add(campoNombreRuta);

        // Creacion del placeholder Inicio Jornada se ingresa la hora con formato de 24 horas "00:00"
        JLabel inicioJornadaLabel = new JLabel("Inicio de Jornada:"); 
        campoInicioJornada = new JTextField(20);
        JPanel panelInicioJornada = CrearSubPanel.subPanel();
        panelInicioJornada.add(inicioJornadaLabel);
        panelInicioJornada.add(campoInicioJornada);

        // Creacion del placeholder Final Jornada se ingresa la hora con formato de 24 horas "00:00"
        JLabel finalJornadaLabel = new JLabel("Final de Jornada:"); 
        campoFinalJornada = new JTextField(20);
        JPanel panelFinalJornada = CrearSubPanel.subPanel();
        panelFinalJornada.add(finalJornadaLabel);
        panelFinalJornada.add(campoFinalJornada);

        // Creacion del panel superior con el tipo de ruta y el boton Guardar Ruta
        JLabel tipoRutaLabel = new JLabel("Defina el tipo de ruta:");
        String[] tipoRuta = {"Ninguno", "Urbana", "Extraurbana"};
        plegableRuta = new JComboBox<>(tipoRuta);
        
        botonGuardarRuta = new JButton("Guardar Ruta");

        JPanel panelTipoRuta = CrearSubPanel.subPanel();
        panelTipoRuta.add(tipoRutaLabel);
        panelTipoRuta.add(plegableRuta);
        panelTipoRuta.add(botonGuardarRuta);

        // Creacion del panel para redefinir ruta con
        JLabel rutaRedefinirLabel = new JLabel("Elija una ruta a redefinir: ");
        comboRutasRedefinir = new JComboBox<>();
        comboRutasRedefinir.addItem(ELIJA_RUTA);
        
        botonRedefinir = new JButton("Redefinir ruta");

        JPanel panelRedefinirRuta = CrearSubPanel.subPanel();
        panelRedefinirRuta.add(rutaRedefinirLabel);
        panelRedefinirRuta.add(comboRutasRedefinir);
        panelRedefinirRuta.add(botonRedefinir);
 
        // Creacion de la tabla de rutas
        String[] nombreColumnas = {"Nombre de Ruta", "Tipo", "Inicio", "Fin", "Unidades y Cupos", "Conductores Asignados"};
        filasTablaRutas = new DefaultTableModel(nombreColumnas,3){ // Contenedor "dinamico" de las filas de la tabla (AQUI SE MODIFICA LA CANTIDAD DE FILAS)
            @Override                                                                // Metodo que evita que las celdas sean modificadas por el usuario
            public boolean isCellEditable(int row, int column) {
            return false;
            }
        };
        tablaRutas = new JTable(filasTablaRutas);
        tablaRutas.setRowHeight(30);
        tablaRutas.getTableHeader().setReorderingAllowed(false);
        tablaRutas.getTableHeader().setResizingAllowed(false);
        tablaRutas.getColumnModel().getColumn(0).setPreferredWidth(140); // Nombre de Ruta
        tablaRutas.getColumnModel().getColumn(1).setPreferredWidth(80);  // Tipo
        tablaRutas.getColumnModel().getColumn(2).setPreferredWidth(60);  // Inicio
        tablaRutas.getColumnModel().getColumn(3).setPreferredWidth(60);  // Fin
        tablaRutas.getColumnModel().getColumn(4).setPreferredWidth(240); // Unidades y Cupos
        tablaRutas.getColumnModel().getColumn(5).setPreferredWidth(340); // Conductores Asignados
        
        JScrollPane tablaConScroll = new JScrollPane(tablaRutas); //Metodo para garantizar la graficacion y visualizacion de una tabla de N filas
        tablaConScroll.setPreferredSize(new DimensionUIResource(920, 150));
        
        JPanel panelTablaRutas = CrearSubPanel.subPanel(); // Ingraso la tablas con scroll en un panel para modificar sus dumensiones y ubicacion.
        panelTablaRutas.add(tablaConScroll);
               
        // --------------------------------------- disposicion del panel central del Cuerpo(Registro) -------------------------------------------- 
        
        add(panelNombreRuta);
        add(panelInicioJornada);
        add(panelFinalJornada);
        add(panelTipoRuta);
        add(panelRedefinirRuta);
        add(panelTablaRutas);
    }

    // ----------------------------------------- Acceso para los controladores ---------------------------------------------------------
    public JTextField getcampoNombreRuta(){
        return campoNombreRuta;
    }
    public JTextField getcampoInicioJornada(){
        return campoInicioJornada;
    }
    public JTextField getcampoFinalJornada(){
        return campoFinalJornada;
    }    
    public JComboBox<String> getPlegableRuta() {
        return plegableRuta;
    }
    public String getTipoRutaSeleccionada(){
         return (String) plegableRuta.getSelectedItem(); 
    }
    public JButton getBotonGuardarRuta(){
        return botonGuardarRuta;
    }
    public JButton getBotonRedefinir(){
        return botonRedefinir;
    }
    public JComboBox<String> getComboRutasRedefinir() {
        return comboRutasRedefinir;
    }
    public String getRutaRedefinirElegida(){
        Object item = comboRutasRedefinir.getSelectedItem();
        if (item == null || ELIJA_RUTA.equals(item.toString())) {
            return null;
        }
        return item.toString();
    }
    public void actualizarRutasRedefinir(List<String> nombres) {
        Object anterior = comboRutasRedefinir.getSelectedItem();
        comboRutasRedefinir.removeAllItems();
        comboRutasRedefinir.addItem(ELIJA_RUTA);
        for (String valor : nombres) {
            comboRutasRedefinir.addItem(valor);
        }
        if (anterior != null && !ELIJA_RUTA.equals(anterior) && nombres.contains(anterior)) {
            comboRutasRedefinir.setSelectedItem(anterior);
        } else {
            comboRutasRedefinir.setSelectedItem(ELIJA_RUTA);
        }
    }
    public DefaultTableModel getModeloTablaRutas(){
        return filasTablaRutas;
    }
}