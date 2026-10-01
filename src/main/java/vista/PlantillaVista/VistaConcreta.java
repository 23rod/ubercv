package vista.PlantillaVista;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;

public class VistaConcreta extends JFrame { //clase base que se usara para graficar todas las pagina del sistema
    
    private EncabezadoGeneral encabezadoGeneral;
    private JPanel cuerpo;
    private PiePagina piePagina;

    // --- CONSTRUCTOR NUEVO (Acuerdo de arquitectura) ---
    public VistaConcreta(JPanel cuerpoInicial){
        this.setBackground(new ColorUIResource(255,255,255));
        this.setSize(1000, 700); 

        // ------------------------------------------ Ubicacion de las partes de la pagina -----------------------------------------------
        encabezadoGeneral = new EncabezadoGeneral();
        add(encabezadoGeneral, "North");
        
        cuerpo = cuerpoInicial;
        add(cuerpo, "Center");

        piePagina = new PiePagina();
        add(piePagina, "South");        
    }

    // --- METODO DE NAVEGACION DINAMICA ---
    public void mostrarCuerpo(JPanel nuevoCuerpo){
         if (cuerpo != null) remove(cuerpo);
        cuerpo = nuevoCuerpo;
        add(cuerpo, "Center");
        revalidate();
        repaint();
    }

    // --- ACCESO PARA EL CONTROLADOR (conectar el boton Cerrar Sesion) ---
    public EncabezadoGeneral getEncabezadoGeneral(){
        return encabezadoGeneral;
    }
}
