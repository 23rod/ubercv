package vista.PlantillaVista.CuerposDelSistema;

import vista.PlantillaVista.CrearSubPanel;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;

public class CuerpoRegistroRuta extends JPanel {

    private JTextField campoNombreRuta;
    private JTextField campoInicioJornada;
    private JTextField campoFinalJornada;
    private JComboBox<String> plegableRuta;
    private JButton botonConfirmar;
    
    public CuerpoRegistroRuta(){
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Extencion a todo lo alto dispoble de la pagina
        this.setOpaque(true);
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.setBackground(new ColorUIResource(255,255,255));
        
        // Creacion del placehodler Nombre Ruta
        JLabel nombreRutaLabel = new JLabel("Nombre Ruta:"); //disposición en el panel contenedor
        campoNombreRuta = new JTextField(20);
        JPanel panelNombreRuta = CrearSubPanel.subPanel();
        panelNombreRuta.add(nombreRutaLabel);
        panelNombreRuta.add(campoNombreRuta);

        // Creacion del placehodler Inicio Jornada se ingresa la hora con formato de 24 horas "00:00"
        JLabel inicioJornadaLabel = new JLabel("Inicio de Jornada:"); 
        campoInicioJornada = new JTextField(20);
        JPanel panelInicioJornada = CrearSubPanel.subPanel();
        panelInicioJornada.add(inicioJornadaLabel);
        panelInicioJornada.add(campoInicioJornada);

        // Creacion del placehodler Final Jornada se ingresa la hora con formato de 24 horas "00:00"
        JLabel finalJornadaLabel = new JLabel("Inicio de Jornada:"); 
        campoFinalJornada = new JTextField(20);
        JPanel panelFinalJornada = CrearSubPanel.subPanel();
        panelFinalJornada.add(finalJornadaLabel);
        panelFinalJornada.add(campoFinalJornada);

        // Creacion del placeholder tipo de ruta
        JLabel tipoRutaLabel = new JLabel("Defina el tipo de ruta:");
        String[] tipoRuta = {"Ninguno", "Urbana", "Extraurbana"};
        plegableRuta = new JComboBox<>(tipoRuta);
        JPanel panelPlegaableTipoRuta = CrearSubPanel.subPanel();
        panelPlegaableTipoRuta.add(tipoRutaLabel);
        panelPlegaableTipoRuta.add(plegableRuta);

        // Creacion del boton confirmar
        botonConfirmar = new JButton("Confirmar");
        JPanel panelBoton = CrearSubPanel.subPanel();
        panelBoton.add(botonConfirmar);

        // --------------------------------------- disposicion del panel central del Cuerpo(Registro) -------------------------------------------- 
        
        add(panelNombreRuta);
        add(panelInicioJornada);
        add(panelFinalJornada);
        add(panelPlegaableTipoRuta);
        add(panelBoton);
    }

    // ----------------------------------------- Acceso para los controladores ---------------------------------------------------------
    public JTextField getcampoNombreRuta(){
        return campoNombreRuta;
    }
    public JTextField getcampoInicioJornada(){
        return campoInicioJornada;
    }public JTextField getcampoFinalJornada(){
        return campoFinalJornada;
    }
    public JButton getBotonConfirmar(){
        return botonConfirmar;
    }
}
 