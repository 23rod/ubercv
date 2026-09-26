package vista.PlantillaVista.CuerposDelSistema;

import vista.PlantillaVista.CrearSubPanel;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;

public class CuerpoRegistroUnidad extends JPanel {

    private JTextField campoPlaca;
    private JTextField campoModelo;
    private JTextField campoCapacidad;
    private JComboBox<String> estadoUnidad;
    private JButton botonConfirmar;
    
    public CuerpoRegistroUnidad(){
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Extencion a todo lo alto dispoble de la pagina
        this.setOpaque(true);
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.setBackground(new ColorUIResource(255,255,255));
        
        // Creacion del placehodler Placa
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

        // --------------------------------------- disposicion del panel central del Cuerpo(Registro) -------------------------------------------- 
        
        add(panelPlaca);
        add(panelModelo);
        add(panelCapacidad);
        add(panelPlegableEstadoUnidad);
        add(panelBoton);
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
    public JButton getBotonConfirmar(){
        return botonConfirmar;
    }
}
 