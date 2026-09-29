package vista.PlantillaVista.CuerposDelSistema;

import vista.PlantillaVista.CrearSubPanel;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;

public class CuerpoRegistro extends JPanel {

    private JTextField campoCorreo;
    private JPasswordField campoContrasenia;
    private JPasswordField campoConfirmarContrasenia;
    private JComboBox<String> plegableRoles;
    private JButton botonConfirmar;
    
    public CuerpoRegistro(){
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Extencion a todo lo alto dispoble de la pagina
        this.setOpaque(true);
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.setBackground(new ColorUIResource(255,255,255));

        // Creacion del placehodler correo
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
        
        add(panelCorreo);
        add(panelContrasenia);
        add(panelConfirmarContrasenia);
        add(panelPlegableRoles);
        add(panelBoton);
    }

    // ----------------------------------------- Acceso para los controladores ---------------------------------------------------------
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

    public static void main(String[] args) {
        CuerpoRegistro nuevo = new CuerpoRegistro();
        nuevo.setVisible(true);
    }

    
}
 

