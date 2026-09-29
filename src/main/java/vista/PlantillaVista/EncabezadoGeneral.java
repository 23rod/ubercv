package vista.PlantillaVista;

import java.awt.FlowLayout;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

public class EncabezadoGeneral extends JPanel{

    private boolean visible;
    private JPanel panelComponentesEncabezado;
    private JLabel Logo;
    private JPanel ubicacionLogo;
    private JButton botonCerrarSesion;
    private JPanel panelBotonCerrarSesion;

    public EncabezadoGeneral(){
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Extencion a todo lo ancho disponible de la pagina
        this.setOpaque(true);
        this.setBackground(new ColorUIResource(255, 255, 255));

        // --------------------------------------------- fondo/borde -------------------------------------------------------------
        this.setOpaque(true);
        this.setBackground(new ColorUIResource(255,255,255));
        
        // --------------------------------------------- creacion elementos ------------------------------------------------------
        // Creacion del LOGO
        ubicacionLogo = CrearSubPanel.subPanel();
        Logo = new JLabel("<html><u>Ubercv</u></html>");
        Logo.setForeground(new ColorUIResource(0,0,0));
        Logo.setFont(new FontUIResource("Inter",1,56));
        ubicacionLogo.setLayout(new FlowLayout(FlowLayout.LEFT)); // Ubicacion del Logo
        ubicacionLogo.add(Logo);
        ubicacionLogo.setAlignmentY(JPanel.CENTER_ALIGNMENT);
        ubicacionLogo.setBorder(BorderFactory.createEmptyBorder(10,10,3,10)); //Separacion de los bordes de la pagina
        
        // Creacion del BOTON cerrar sesion
        botonCerrarSesion = new JButton("Cerrar Sesión");
        panelBotonCerrarSesion = CrearSubPanel.subPanel();
        panelBotonCerrarSesion.setLayout(new FlowLayout(FlowLayout.RIGHT)); // Ubicacion del boton
        panelBotonCerrarSesion.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 10));// Separacion del boton del borde superior
        panelBotonCerrarSesion.add(botonCerrarSesion);
        
        // Creacion del PANEL contenedor de comoponentes en el encabezado
        panelComponentesEncabezado = CrearSubPanel.subPanel();
        panelComponentesEncabezado.setLayout(new BoxLayout(panelComponentesEncabezado, BoxLayout.X_AXIS));
        panelComponentesEncabezado.add(ubicacionLogo);
        panelComponentesEncabezado.add(panelBotonCerrarSesion);

        // linea separadora del cuerpo 
        JSeparator separador = new JSeparator();
        separador.setForeground(new ColorUIResource(0,0,0));
        
        // ------------------------------------------------- Aniadir elementos ---------------------------------------------------
        add(panelComponentesEncabezado);
        add(separador);
    }
    
    // ----------------------------------------------- acceso de datos del controlador -------------------------------------------
    
    public boolean getVisibilidadBotonCerrarSesion() {
        return visible;
    }
    public JLabel getTextoPiePagina() {
        return Logo;
    }
    public JButton getBotonCerrarSesion(){
        return botonCerrarSesion;
    }

    // --------------------------------------------------- metodos del encabezado ------------------------------------------------

    public void mostrarBotonCerrarSesion(boolean visible) { // Metodo para controlar en que paginas de la web se el botonde cerrar sesion
        this.visible = visible; // control de la visibilidad del objeto
        botonCerrarSesion.setVisible(visible);
        panelBotonCerrarSesion.setVisible(visible);
        // Calcula y grafica nuevamente el panel luego de hacer visible o no el boton
        this.revalidate();
        this.repaint();
    }
}