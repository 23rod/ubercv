package vista.PlantillaVista;


import java.awt.FlowLayout;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

public class PiePagina extends JPanel {
    private JLabel textoPiePagina;
    private JPanel piePagina;

    public PiePagina(){
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Extencion a todo lo ancho disponible de la pagina
        this.setOpaque(true);
        this.setBackground(new ColorUIResource(255, 255, 255));

        // linea separadora del cuerpo
        JSeparator separador = new JSeparator();
        separador.setForeground(new ColorUIResource(0, 0, 0));

        // ----------------------------------------------- Contenido Pie de Pagina -------------------------------------------------------
        piePagina = CrearSubPanel.subPanel();
        piePagina.setBackground(new ColorUIResource(255, 255, 255));
        piePagina.setLayout(new FlowLayout(FlowLayout.LEFT));

        textoPiePagina = new JLabel("© UNIVERSIDAD CENTRAL DE VENEZUELA. 2026 All Rights Reserved");
        textoPiePagina.setFont(new FontUIResource("Captions",0,12));
        textoPiePagina.setForeground(new ColorUIResource(52, 17, 92));
        textoPiePagina.setBorder(BorderFactory.createEmptyBorder(3,10,10,10));

        // ------------------------------------------- Construccion del pie de pagina ----------------------------------------------------
        this.add(separador);
        piePagina.add(textoPiePagina);
        this.add(piePagina);
    }

    // ----------------------------------------------- acceso de datos del controlador ---------------------------------------------------
    public JLabel getTextoPiePagina() {
        return textoPiePagina;
    }
}
