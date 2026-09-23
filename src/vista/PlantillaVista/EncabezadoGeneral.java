package vista.PlantillaVista;

import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;

public class EncabezadoGeneral extends JPanel{

    private JLabel Logo;

    public EncabezadoGeneral(){

        // --------------------------------------------- fondo/borde -------------------------------------------------------------
        this.setOpaque(true);
        this.setBackground(new ColorUIResource(0,0,0));
        
        // --------------------------------------------- creacion elementos -------------------------------------------------
        // LOGO
        JPanel ubicacionLogo = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        ubicacionLogo.setOpaque(false);
        Logo = new JLabel("Ubercv");
        ubicacionLogo.add(Logo);
        
        // linea separadora del cuerpo 
        JPanel panelSeparador = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        panelSeparador.setOpaque(false);
        panelSeparador.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0)); 
        
        JSeparator separador = new JSeparator();
        separador.setPreferredSize(new Dimension(getWidth()-1, 2));
        separador.setForeground(new ColorUIResource(255,255,255));
        panelSeparador.add(separador);

        //aniadir elementos
        this.add(ubicacionLogo);
        this.add(panelSeparador);
    }

}