package vista.PlantillaVista;

import java.awt.FlowLayout;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

public class EncabezadoGeneral extends JPanel{

    private JLabel Logo;
    private JPanel ubicacionLogo;

    public EncabezadoGeneral(){
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Extencion a todo lo ancho disponible de la pagina
        this.setOpaque(true);
        this.setBackground(new ColorUIResource(255, 255, 255));

        // --------------------------------------------- fondo/borde -------------------------------------------------------------
        this.setOpaque(true);
        this.setBackground(new ColorUIResource(255,255,255));
        
        // --------------------------------------------- creacion elementos -------------------------------------------------
        // LOGO
        ubicacionLogo = CrearSubPanel.subPanel();
        Logo = new JLabel("<html><u>Ubercv</u></html>");
        Logo.setForeground(new ColorUIResource(0,0,0));
        Logo.setFont(new FontUIResource("Inter",1,56));
        ubicacionLogo.add(Logo);
        ubicacionLogo.setLayout(new FlowLayout(FlowLayout.LEFT));
        ubicacionLogo.setBorder(BorderFactory.createEmptyBorder(10,10,3,10));
        
        // linea separadora del cuerpo 
        JSeparator separador = new JSeparator();
        separador.setForeground(new ColorUIResource(0,0,0));
        
        //aniadir elementos
        this.add(ubicacionLogo);
        this.add(separador);
    }
    
    // ----------------------------------------------- acceso de datos del controlador ---------------------------------------------------
    public JLabel getTextoPiePagina() {
        return Logo;
    }
}