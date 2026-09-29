package vista.PlantillaVista.CuerposDelSistema;

import vista.PlantillaVista.CrearSubPanel;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;

public class CuerpoFuncionesAdmin extends JPanel {

    private JButton botonFlota;
    private JButton botonItinerarios;
    
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
        botonItinerarios = new JButton("Control de Itinerarios");
        JPanel panelBotonItinerarios = CrearSubPanel.subPanel();
        panelBotonItinerarios.add(botonItinerarios);

        // --------------------------------------- Disposicion del panel central del Cuerpo(Registro) -------------------------------------------- 
        
        add(panelBotonFlota);
        add(panelBotonItinerarios);
    }

    // ----------------------------------------- Acceso para los controladores ---------------------------------------------------------
    public JButton getBotonFlota(){
        return botonFlota;
    }
    public JButton getBotonItinerario(){
        return botonItinerarios;
    }   
}
 