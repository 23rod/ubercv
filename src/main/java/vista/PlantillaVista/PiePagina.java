package vista.PlantillaVista;

import javax.swing.*;
import java.awt.FlowLayout;
import java.awt.Dimension;
import javax.swing.plaf.ColorUIResource;

public class PiePagina extends JPanel {
    private JLabel textoPiePagina;

    public PiePagina(){
        // linea separadora del cuerpo
        JSeparator separador = new JSeparator();
        separador.setPreferredSize(new Dimension(getWidth()-1, 2));
        separador.setForeground(new ColorUIResource(0, 0, 0));
        add(separador);

        // ------------------------------------------- COntenido Pie de Pagina -----------------------------------------------------------------
        JPanel piePagina = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 15));
        piePagina.setOpaque(false) ;
        piePagina.setBackground(new ColorUIResource(0, 0, 0));

        textoPiePagina = new JLabel("© UNIVERSIDAD CENTRAL DE VENEZUELA. 2026 All Rights Reserved");
        textoPiePagina.setForeground(new ColorUIResource(60, 60, 90));

        piePagina.add(textoPiePagina);
        
        add(piePagina);
    }

    // ----------------------------------------------- acceso de datos del controlador ---------------------------------------------------
    public JLabel getTextoPiePagina() {
        return textoPiePagina;
    }
}
