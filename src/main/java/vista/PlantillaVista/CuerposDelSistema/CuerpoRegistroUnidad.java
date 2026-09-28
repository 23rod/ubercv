package vista.PlantillaVista.CuerposDelSistema;

import vista.PlantillaVista.CrearSubPanel;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.table.DefaultTableModel;

public class CuerpoRegistroUnidad extends JPanel {

    private JTextField campoPlaca;
    private JTextField campoModelo;
    private JTextField campoCapacidad;
    private JComboBox<String> estadoUnidad;
    private JButton botonConfirmar;
    private JTable tablaUnidades;
    private DefaultTableModel filasTablaUnidades;
    
    public CuerpoRegistroUnidad(){
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Extencion a todo lo alto disponible de la pagina
        this.setOpaque(true);
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.setBackground(new ColorUIResource(255,255,255));
        
        // Creacion del placeholder Placa
        JLabel placaLabel = new JLabel("Placa:"); //disposición en el panel contenedor
        campoPlaca = new JTextField(20);
        JPanel panelPlaca = CrearSubPanel.subPanel();
        panelPlaca.add(placaLabel);
        panelPlaca.add(campoPlaca);

        // Creacion de placeholder Modelo
        JLabel modeloLabel = new JLabel("Modelo:");
        campoModelo = new JTextField(20);
        JPanel panelModelo = CrearSubPanel.subPanel();
        panelModelo.add(modeloLabel);
        panelModelo.add(campoModelo);
        
        // Creacion del placeholder Capadidad
        JLabel capacidadLabel = new JLabel("Capacidad:");
        campoCapacidad = new JTextField(20);
        JPanel panelCapacidad = CrearSubPanel.subPanel();
        panelCapacidad.add(capacidadLabel);
        panelCapacidad.add(campoCapacidad);

        // Creacion del placeholder estado de la unidad
        JLabel plegableLabel = new JLabel("Defina el estado de la unidad:");
        String[] tipoRuta = {"Ninguno", "Activo", "En mantenimiento", "Fuera de Servicio"};
        estadoUnidad = new JComboBox<>(tipoRuta);
        JPanel panelPlegableEstadoUnidad = CrearSubPanel.subPanel();
        panelPlegableEstadoUnidad.add(plegableLabel);
        panelPlegableEstadoUnidad.add(estadoUnidad);

        // Creacion del boton confirmar
        botonConfirmar = new JButton("Confirmar");
        JPanel panelBoton = CrearSubPanel.subPanel();
        panelBoton.add(botonConfirmar);

        // Cracion de la tabla de rutas
        String[] nombreColumnas = {"Placa", "Modelo", "Capacidad", "Estado Operativo"};
        filasTablaUnidades = new DefaultTableModel(nombreColumnas,3){ // Contenedor "dinamico" de las filas de la tabla (AQUI SE MODIFICA LA CANTIDAD DE FILAS)
            @Override                                                                // Metodo que evita que las celdas sean modificadas por el usuario
            public boolean isCellEditable(int row, int column) {
            return false;
            }
        };
        tablaUnidades = new JTable(filasTablaUnidades);
        tablaUnidades.setRowHeight(30);
        
        JScrollPane tablaConScroll = new JScrollPane(tablaUnidades); //Metodo para garantizar la graficacion y visualizacion de una tabla de N filas
        tablaConScroll.setPreferredSize(new DimensionUIResource(800, 150));
        
        JPanel panelTablaUnidades = CrearSubPanel.subPanel(); // Ingraso la tablas con scroll en un panel para modificar sus dumensiones y ubicacion.
        panelTablaUnidades.add(tablaConScroll);

        // --------------------------------------- disposicion del panel central del Cuerpo(Registro) -------------------------------------------- 
        
        add(panelPlaca);
        add(panelModelo);
        add(panelCapacidad);
        add(panelPlegableEstadoUnidad);
        add(panelBoton);
        add(panelTablaUnidades);
    }

    // ----------------------------------------- Acceso para los controladores ---------------------------------------------------------
    public JTextField getcampoPlaca(){
        return campoPlaca;
    }
    public JTextField getcampoModelo(){
        return campoModelo;
    }
    public JTextField getcampoCapacidad(){
        return campoCapacidad;
    }
    public String getEstadoSeleccionado(){
         return (String) estadoUnidad.getSelectedItem(); 
    }
    public JButton getBotonConfirmar(){
        return botonConfirmar;
    }
    public DefaultTableModel getModeloTablaUnidades(){
        return filasTablaUnidades;
    }
}
  