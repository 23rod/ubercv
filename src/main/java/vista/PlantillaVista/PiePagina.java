package vista.PlantillaVista;

import javax.swing.*;
import java.awt.FlowLayout;
import javax.swing.plaf.ColorUIResource;

public class PiePagina extends JPanel {
    private JLabel textoPiePagina;

    public PiePagina(){
        // linea separadora del cuerpo
        JSeparator separador = new JSeparator();
        separador.setForeground(new ColorUIResource(0, 0, 0));
        add(separador);

        // ------------------------------------------- COntenido Pie de Pagina -----------------------------------------------------------------
        JPanel piePagina = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 15));
        piePagina.setOpaque(true) ;
        piePagina.setBackground(new ColorUIResource(255, 255, 255));

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
