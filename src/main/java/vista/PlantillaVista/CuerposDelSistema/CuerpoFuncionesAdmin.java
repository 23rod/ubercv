package vista.PlantillaVista.CuerposDelSistema;

import vista.PlantillaVista.CrearSubPanel;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;

public class CuerpoFuncionesAdmin extends JPanel {

    private JButton botonFlota;
    private JButton botonItinerarios;
    private JButton botonTarifas;
    
    public CuerpoFuncionesAdmin(){
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Extencion a todo lo alto disponible de la pagina
        this.setOpaque(true);
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.setBackground(new ColorUIResource(255,255,255));

        // Creacion del boton Flota
        botonFlota = new JButton("Gestión de Flota");
        JPanel panelBotonFlota = CrearSubPanel.subPanel();
        panelBotonFlota.add(botonFlota);

        // Creacion del boton Itinerarios
        botonItinerarios = new JButton("Gestión de Itinerarios");
        JPanel panelBotonItinerarios = CrearSubPanel.subPanel();
        panelBotonItinerarios.add(botonItinerarios);

        // Creacion del boton Gestion tarifas
        botonTarifas = new JButton("Gestión de Tarifas");
        JPanel panelbotonTarifas = CrearSubPanel.subPanel();
        panelbotonTarifas.add(botonTarifas);

        // --------------------------------------- Disposicion del panel central del Cuerpo(Registro) -------------------------------------------- 
        
        add(panelBotonFlota);
        add(panelBotonItinerarios);
        add(panelbotonTarifas);
    }

    // ----------------------------------------- Acceso para los controladores ---------------------------------------------------------
    public JButton getBotonFlota(){
        return botonFlota;
    }
    public JButton getBotonItinerario(){
        return botonItinerarios;
    }   
    public JButton getBotonTarifas(){
        return botonTarifas;
    }   
}