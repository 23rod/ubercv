package vista.PlantillaVista;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;

import vista.PlantillaVista.CuerposDelSistema.*;

public class VistaConcreta extends JFrame { //clase base que se usara para graficar todas las pagina del sistema
    
    private EncabezadoGeneral encabezadoGeneral;
    private JPanel cuerpo;
    private PiePagina piePagina;

    public VistaConcreta(){
        this.setBackground(new ColorUIResource(255,255,255));
        this.setSize(1000, 700); 

        // ------------------------------------------ Ubicacion de las partes de la pagina -----------------------------------------------
        encabezadoGeneral = new EncabezadoGeneral();
        add(encabezadoGeneral, "North");

        // Condicional al rol de la apertura se decidira luego de hacer el controlador correspondiente
        // Para seleccionar un cuerpo de lapagina camviar constructor
        cuerpo = new CuerpoFuncionesAdmin();
        add(cuerpo, "Center");

        piePagina = new PiePagina();
        add(piePagina, "South");
    }
}

