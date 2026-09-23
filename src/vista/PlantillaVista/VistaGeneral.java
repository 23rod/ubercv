package vista.PlantillaVista;

import javax.swing.*;

public class VistaGeneral extends JFrame { //clase base que se usara para graficar todas las pagina del sistema
    private EncabezadoGeneral encabezadoGeneral;
    //private Cuerpo cuerpo;
    private PiePagina piePagina;

    public VistaGeneral(){
        setSize(1000, 700); // Condicional al dispositivo de la apertura se decidira luego de hacer el controlador correspondiente
        encabezadoGeneral = new EncabezadoGeneral();
        add(encabezadoGeneral);



        piePagina = new PiePagina();
        add(piePagina);
    }

}

