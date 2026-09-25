package vista.PlantillaVista;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;

public class Cuerpo extends JPanel {

    private JTextField campoCorreo;
    private JPasswordField campoContraseña;
    private JPasswordField campoConfirmarContraseña;
    private JComboBox<String> plegableRoles;
    private JButton botonConfirmar;
    
    public Cuerpo(){
        // ------------------------------------- Formato Registro ---------------------------------------------
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Extencion a todo lo alto disponible de la pagina
        this.setOpaque(true);
        this.setBackground(new ColorUIResource(255,255,255));
        
        // Creacion de placeoder correo
        JLabel correoLabel = new JLabel("Correo Electrónico:", SwingConstants.CENTER); //Dispocision en el panel contenedor
        campoCorreo = new JTextField(20);
        JPanel panelCorreo = CrearSubPanel.subPanel();
        panelCorreo.add(correoLabel);
        panelCorreo.add(campoCorreo);

        // Creacion de placeoder contrasena
        JLabel contraseñaLabel = new JLabel("Contraseña:", SwingConstants.CENTER);
        campoContraseña = new JPasswordField(20);
        JPanel panelContraseña = CrearSubPanel.subPanel();
        panelContraseña.add(contraseñaLabel);
        panelContraseña.add(campoContraseña);
        
        // Creacion de placeoder confirmar contrasena
        JLabel confirmarContraseñaLabel = new JLabel("Confirmar Contraseña:", SwingConstants.CENTER);
        campoConfirmarContraseña = new JPasswordField(20);
        JPanel panelConfirmarContraseña = CrearSubPanel.subPanel();
        panelConfirmarContraseña.add(confirmarContraseñaLabel);
        panelConfirmarContraseña.add(campoConfirmarContraseña);
        
        // Creacion de placeoder roles
        JLabel rolesLabel = new JLabel("Defina su rol:", SwingConstants.CENTER);
        String[] roles = {"Ninguno", "Estudiante", "Empleado", "Conductor", "Admin de Transporte"};
        plegableRoles = new JComboBox<>(roles);
        JPanel panelPlegableRoles = CrearSubPanel.subPanel();
        panelPlegableRoles.add(rolesLabel);
        panelPlegableRoles.add(plegableRoles);

        // Creacion de boton confirmar
        botonConfirmar = new JButton("Confirmar");
        JPanel panelBoton = CrearSubPanel.subPanel();
        panelBoton.add(botonConfirmar);

        // --------------------------------------- Disposicion del panel central del Cuerpo -------------------------------------------- 
        //configuracion de borde superior
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(Box.createVerticalGlue()); 

        add(panelCorreo);
        add(panelContraseña);
        add(panelConfirmarContraseña);
        add(panelPlegableRoles);
        add(panelBoton);
        
         //configuracion de borde inferior
        add(Box.createVerticalGlue());
    }
    // ----------------------------------------- Acceso para los controladores ---------------------------------------------------------
    public JTextField getCampoCorreo(){return campoCorreo;}
    public JPasswordField getCampoContrasenia(){return campoContraseña;}
    public JPasswordField getCampoConfirmarContrasenia(){return campoConfirmarContraseña;}
    public JButton getBotonConfirmar(){return botonConfirmar;}
}
 