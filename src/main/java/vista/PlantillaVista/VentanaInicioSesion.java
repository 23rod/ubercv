package vista.PlantillaVista;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.BorderLayout;
//Imports para la fuente de texto de las labels
import java.awt.Font;


//Imports para la interfaz gráfica de la ventana de inicio de sesión
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
//import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JPanel;
//Imports para recoger los datos de inicio de sesión: contrasena y correo
import javax.swing.JTextField;
import javax.swing.JToggleButton;
//import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.JPasswordField;

import controlador.Control;
import modelo.Resultado;
import modelo.Rol;

public class VentanaInicioSesion extends JFrame {
    private JTextField nombreUsuarioField;
    private JPasswordField contrasenaUsuarioField;

    // Constructor: dimensiones, campos, botones,
    public VentanaInicioSesion(Control control) {
        
        //Creamos la ventana y colocamos un título
        super("Inicio de Sesión"); //herencia de JFrame
        setResizable(false); //No se puede maximizar la ventana

        //Para que finalice la aplicacion al cerrar la ventana
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //Establecemos el tamaño de la ventana
        setSize(500, 500);

        //Establecemos la ubicación de la ventana en el centro de la pantalla
        setLocationRelativeTo(null);

        //Creamos el panel y las etiquetas y botones en él
        JPanel panel = Panel();

        //Creamos los botones de registrarse e iniciar sesión
        crearBotones(panel, control);     






    }

    private JPanel Panel() {
        // Implementación del panel con campos de texto y botones
        JPanel panel = new JPanel();
        panel.setLayout(null);

        //Ingresamos las etiquetas
        ingresarEtiquetas(panel);
        ingresarCamposDeTexto(panel);          


        add(panel);

        return panel;
    }

    //Interfaces del Panel: etiquetas, campos de texto y botones
    private void ingresarEtiquetas(JPanel panel){
        //Crear etiqueta "INICIO DE SESIÓN"
        JLabel etiquetaTituloCentral = new JLabel("INICIO DE SESIÓN");
        etiquetaTituloCentral.setBounds(140, 70, 250, 50);
        etiquetaTituloCentral.setFont(new Font("Arial", Font.BOLD, 24));
        etiquetaTituloCentral.setOpaque(false);

        //Crear una etiqueta para el usuario
        JLabel etiquetaUsuario = new JLabel("Correo electrónico:");
        etiquetaUsuario.setBounds(130, 160, 200, 20);

        //Crear una etiqueta para la contraseña
        JLabel etiquetaContrasena = new JLabel("Contraseña:");
        etiquetaContrasena.setBounds(130, 220, 100, 20);

        //Crear una etiqueta para ir a registrarse
        JLabel etiquetaRegistro = new JLabel("¿No te has registrado? Regístrate aquí");
        etiquetaRegistro.setBounds(40, 370, 230, 20);
        etiquetaRegistro.setForeground(Color.BLUE);

         //Tenemos que aniadir la etiqueta al panel para guardar sus caracteristicas
        panel.add(etiquetaTituloCentral); //Titulo
        panel.add(etiquetaContrasena); //Etiqueta de contraseña
        panel.add(etiquetaRegistro); //Etiqueta de registro
        panel.add(etiquetaUsuario); //Etiqueta de usuario

    }

    private void ingresarCamposDeTexto(JPanel panel){
        //Crear campos de texto para el correo
        nombreUsuarioField = new JTextField();
        nombreUsuarioField.setBounds(130, 180, 230, 30);

        
        //Crear campo de texto para la contraseña
        campoContrasena(panel);

        //Campos de una linea de texto
        panel.add(nombreUsuarioField);        
        panel.add(contrasenaUsuarioField);
    }


    private void campoContrasena(JPanel panel) {        
        //Crear campo de texto para la contraseña
        contrasenaUsuarioField = new JPasswordField();
        contrasenaUsuarioField.setBounds(130, 245, 180, 18);
        contrasenaUsuarioField.setBorder(null); // Quitar borde para que use el del contenedor del ícono

        //Guardar el caracter de ocultación por defecto del sistema
        final char defaultEchoChar = contrasenaUsuarioField.getEchoChar();

        // Cargar iconos
        ImageIcon iconoAbierto = new ImageIcon(getClass().getResource("/vista/PlantillaVista/ImagenesDeInicio/ojo.png"));
        ImageIcon iconoCerrado = new ImageIcon(getClass().getResource("/vista/PlantillaVista/ImagenesDeInicio/ojo-cerrado.png"));
        // Crear el botón toggle (texto del ojo)
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
                contrasenaUsuarioField.setEchoChar((char) 0); // Texto visible
                toggleButton.setIcon(iconoAbierto);         // Cambiar símbolo/icono
            } else {
                contrasenaUsuarioField.setEchoChar(defaultEchoChar); // Volver a ocultar
                toggleButton.setIcon(iconoCerrado);
            }
        });

        // Contenedor estilizado para simular un solo campo de texto
        JPanel fieldContainer = new JPanel(new BorderLayout());
        fieldContainer.setBackground(Color.WHITE);
        fieldContainer.setBounds(128, 240, 230, 30);
        fieldContainer.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(Color.GRAY, 1),
        new EmptyBorder(4, 6, 4, 6)));


        fieldContainer.add(contrasenaUsuarioField, BorderLayout.CENTER);
        fieldContainer.add(toggleButton, BorderLayout.EAST);
        panel.add(fieldContainer);  

    }
              
      
    

    private void crearBotones(JPanel panel, Control control) {
        //Boton para iniciar sesión
        JButton botonIniciarSesion = new JButton("Iniciar Sesión");
        botonIniciarSesion.setBounds(165, 300, 150, 30);

        //Boton para ir a registrarse
        JButton botonRegistro = new JButton("Registrarse");
        botonRegistro.setBounds(270, 365, 150, 30);       

            
        //Logica para iniciar sesión e ingresar al menu de usuario
        //Falta ingresar la clase de menu de usuario para que se pueda abrir la ventana del menu de usuario
        botonIrMenuUsuario(botonIniciarSesion, control); 
        //Falta ingresar la clase de registro para que se pueda abrir la ventana de registro
        botonIrRegistro(botonRegistro, control); 


        panel.add(botonIniciarSesion);
        panel.add(botonRegistro);            
        
        getRootPane().setDefaultButton(botonIniciarSesion);
    }

    //Lógica para abrir la ventana del menu de usuario al iniciar sesión
    private void botonIrMenuUsuario( JButton botonIniciarSesion, Control control) {

        //Vincular el listener al boton de iniciar sesión
        botonIniciarSesion.addActionListener(e -> {
            //Extraer los valores de los campos de texto
            String correoRecibido = nombreUsuarioField.getText();
            String contrasenaRecibida = new String(contrasenaUsuarioField.getPassword());
            
            //Validación para verificar si el correo y la contraseña coinciden con los datos guardados del "Modelo"
            Resultado<Rol> r = control.iniciarSesion(correoRecibido, contrasenaRecibida);
            
            if (r.isOk()) { 
                dispose(); control.abrirVentanaPrincipal(); 
                
            }
            else { 
                JOptionPane.showMessageDialog(this, r.getMensaje());
                // Iniciar sesión exitosa: ir al menu de usuario                
                // Lógica para abrir la ventana principal del usuario
            
                //Se borra el contenido de los campos de texto para que el usuario vuelva a ingresar sus datos
                nombreUsuarioField.setText("");
                contrasenaUsuarioField.setText("");
                nombreUsuarioField.requestFocus(); //Coloca el cursor en el campo de correo
                //JOptionPane.showMessageDialog(null, "Correo o contraseña incorrectos.");
            }

        }); 

        

    }
    
    private void botonIrRegistro(JButton botonRegistro, Control control) {
        //Vincular el listener al boton de registrarse
        botonRegistro.addActionListener(e -> {
            control.abrirRegistro(); // el Control abre la ventana de registro y cierra las demas
            this.dispose();
        });
    } 
}
