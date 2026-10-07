package vista.PlantillaVista.CuerposDelSistema;

import vista.PlantillaVista.CrearSubPanel;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;

public class CuerpoRegistro extends JPanel {

    private JTextField campoCedula;
    private JTextField campoCorreo;
    private JPasswordField campoContrasenia;
    private JPasswordField campoConfirmarContrasenia;
    private JComboBox<String> plegableRoles;
    private JButton botonConfirmar;
    
    public CuerpoRegistro(){
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Extencion a todo lo alto disponible de la pagina
        this.setOpaque(true);
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.setBackground(new ColorUIResource(255,255,255));

        // Creacion del placeholder Cedula
        JLabel cedulaLabel = new JLabel("Cédula:"); //disposición en el panel contenedor
        campoCedula = new JTextField(20);
        JPanel panelCedula = CrearSubPanel.subPanel();
        panelCedula.add(cedulaLabel);
        panelCedula.add(campoCedula);

        // Creacion del placeholder correo
        JLabel correoLabel = new JLabel("Correo Electrónico:"); //disposición en el panel contenedor
        campoCorreo = new JTextField(20);
        JPanel panelCorreo = CrearSubPanel.subPanel();
        panelCorreo.add(correoLabel);
        panelCorreo.add(campoCorreo);

        // Creacion de placeholder contrasenia
        JLabel contraseniaLabel = new JLabel("Contraseña:");
        campoContrasenia = new JPasswordField(20);
        JPanel panelContrasenia = CrearSubPanel.subPanel();
        panelContrasenia.add(contraseniaLabel);
        panelContrasenia.add(campoContrasenia);
        
        // Creacion del placeholder confirmar contrasenia
        JLabel confirmarContraseniaLabel = new JLabel("Confirmar Contraseña:");
        campoConfirmarContrasenia = new JPasswordField(20);
        JPanel panelConfirmarContrasenia = CrearSubPanel.subPanel();
        panelConfirmarContrasenia.add(confirmarContraseniaLabel);
        panelConfirmarContrasenia.add(campoConfirmarContrasenia);
        
        // Creacion del placeholder roles
        JLabel rolesLabel = new JLabel("Defina su rol:");
        String[] roles = {"Ninguno", "Estudiante", "Empleado", "Conductor", "Admin de Transporte"};
        plegableRoles = new JComboBox<>(roles);
        JPanel panelPlegableRoles = CrearSubPanel.subPanel();
        panelPlegableRoles.add(rolesLabel);
        panelPlegableRoles.add(plegableRoles);

        // Creacion del boton confirmar
        botonConfirmar = new JButton("Confirmar");
        JPanel panelBoton = CrearSubPanel.subPanel();
        panelBoton.add(botonConfirmar);

        // --------------------------------------- disposicion del panel central del Cuerpo(Registro) -------------------------------------------- 
        
        add(panelCedula);
        add(panelCorreo);
        crearCampoContrasenia();
        add(panelConfirmarContrasenia);
        add(panelPlegableRoles);
        add(panelBoton);
    }

    //Creación del ojo para visualizar la contraseña
    private void crearCampoContrasenia() {
        JLabel contraseniaLabel = new JLabel("Contraseña:");
        campoContrasenia = new JPasswordField(20);        
        campoContrasenia.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        // Guardar el caracter de ocultación por defecto del sistema
        final char defaultEchoChar = campoContrasenia.getEchoChar();

        // Cargar iconos
        ImageIcon iconoAbierto = new ImageIcon(getClass().getResource("/vista/PlantillaVista/ImagenesDeInicio/ojo.png"));
        ImageIcon iconoCerrado = new ImageIcon(getClass().getResource("/vista/PlantillaVista/ImagenesDeInicio/ojo-cerrado.png"));
        
        // Crear el botón toggle (texto del ojo)
        JToggleButton toggleButton = new JToggleButton(iconoCerrado);        
        toggleButton.setPreferredSize(new Dimension(32, 24));
        toggleButton.setFocusPainted(false);
        toggleButton.setContentAreaFilled(false);
        toggleButton.setBorderPainted(false);
        toggleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleButton.setToolTipText("Mostrar/Ocultar contraseña");

        // Alternar visibilidad al presionar
        toggleButton.addActionListener(e -> {
            if (toggleButton.isSelected()) {
                campoContrasenia.setEchoChar((char) 0); // Texto visible
                toggleButton.setIcon(iconoAbierto);         // Cambiar símbolo/icono
            } else {
                campoContrasenia.setEchoChar(defaultEchoChar); // Volver a ocultar
                toggleButton.setIcon(iconoCerrado);
            }
        });

        // Contenedor estilizado para simular un solo campo de texto
        JPanel panelContrasenia = CrearSubPanel.subPanel();
        panelContrasenia.setBounds(15, 15, 300, 50);
        panelContrasenia.add(contraseniaLabel);
        panelContrasenia.add(campoContrasenia);
        panelContrasenia.add(toggleButton);
        add(panelContrasenia);
    }


    // ----------------------------------------- Acceso para los controladores ---------------------------------------------------------
    public JTextField getCampoCedula(){
        return campoCedula;
    }    
    public JTextField getCampoCorreo(){
        return campoCorreo;
    }
    public JPasswordField getCampoContrasenia(){
        return campoContrasenia;
    }
    public JPasswordField getCampoConfirmarContrasenia(){
        return campoConfirmarContrasenia;
    }
    public String getRolSeleccionado(){
         return (String) plegableRoles.getSelectedItem(); 
    }
    public JButton getBotonConfirmar(){
        return botonConfirmar;
    }


    
}
 

