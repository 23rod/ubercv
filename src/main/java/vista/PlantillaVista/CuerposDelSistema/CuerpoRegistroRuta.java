package vista.PlantillaVista.CuerposDelSistema;

import vista.PlantillaVista.CrearSubPanel;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.table.DefaultTableModel;

public class CuerpoRegistroRuta extends JPanel {

    private JTextField campoNombreRuta;
    private JTextField campoInicioJornada;
    private JTextField campoFinalJornada;
    private JComboBox<String> plegableRuta;
    private JButton botonConfirmar;
    private JButton botonAsignarUnidad;
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

        // Creacion del placeholder tipo de ruta
        JLabel tipoRutaLabel = new JLabel("Defina el tipo de ruta:");
        String[] tipoRuta = {"Ninguno", "Urbana", "Extraurbana"};
        plegableRuta = new JComboBox<>(tipoRuta);
        JPanel panelPlegableTipoRuta = CrearSubPanel.subPanel();
        panelPlegableTipoRuta.add(tipoRutaLabel);
        panelPlegableTipoRuta.add(plegableRuta);
 
        // Creacion del boton confirmar
        botonConfirmar = new JButton("Confirmar");
        JPanel panelBotonConfirmar = CrearSubPanel.subPanel();
        panelBotonConfirmar.add(botonConfirmar);

        // Creacion del boton confirmar
        botonAsignarUnidad = new JButton("Confirmar");
        JPanel panelBotonAsignarUnidad = CrearSubPanel.subPanel();
        panelBotonAsignarUnidad.add(botonConfirmar);

        // Cracion de la tabla de rutas
        String[] nombreColumnas = {"Nombre de Ruta", "Tipo de Ruta", "Inicio de Jornada", "Final de Jornada", "Unidades Asignadas(Placa)", "Modificar Ruta"};
        filasTablaRutas = new DefaultTableModel(nombreColumnas,3){ // Contenedor "dinamico" de las filas de la tabla (AQUI SE MODIFICA LA CANTIDAD DE FILAS)
            @Override                                                                // Metodo que evita que las celdas sean modificadas por el usuario
            public boolean isCellEditable(int row, int column) {
            return false;
            }
        };
        tablaRutas = new JTable(filasTablaRutas);
        tablaRutas.setRowHeight(30);
        
        JScrollPane tablaConScroll = new JScrollPane(tablaRutas); //Metodo para garantizar la graficacion y visualizacion de una tabla de N filas
        tablaConScroll.setPreferredSize(new DimensionUIResource(920, 150));
        
        JPanel panelTablaRutas = CrearSubPanel.subPanel(); // Ingraso la tablas con scroll en un panel para modificar sus dumensiones y ubicacion.
        panelTablaRutas.add(tablaConScroll);
               

        // --------------------------------------- disposicion del panel central del Cuerpo(Registro) -------------------------------------------- 
        
        add(panelNombreRuta);
        add(panelInicioJornada);
        add(panelFinalJornada);
        add(panelPlegableTipoRuta);
        add(panelBotonConfirmar);
        add(panelBotonAsignarUnidad);
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
    public String getTipoRutaSeleccionada(){
         return (String) plegableRuta.getSelectedItem(); 
    }
    public JButton getBotonConfirmar(){
        return botonConfirmar;
    }
    public JButton getBotonAsignarUnidad(){
        return botonAsignarUnidad;
    }
    public DefaultTableModel getModeloTablaRutas(){
        return filasTablaRutas;
    }
}
 