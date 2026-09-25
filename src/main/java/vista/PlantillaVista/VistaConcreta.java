package vista.PlantillaVista;

import javax.swing.*;

public class VistaConcreta extends JFrame { //clase base que se usara para graficar todas las pagina del sistema
    private EncabezadoGeneral encabezadoGeneral;
    private Cuerpo cuerpo;
    private PiePagina piePagina;

    public VistaConcreta(){
        // ------------------------------------------ Ubicacion de las partes de la pagina -----------------------------------------------
        setSize(1000, 700); // Condicional al rol de la apertura se decidira luego de hacer el controlador correspondiente
        encabezadoGeneral = new EncabezadoGeneral();
        add(encabezadoGeneral, "North");

        cuerpo = new Cuerpo();
        add(cuerpo, "Center");

        piePagina = new PiePagina();
        add(piePagina, "South");
    }

}

