package vista.PlantillaVista;

import java.awt.Color;
//Imports para la fuente de texto de las labels
import java.awt.Font;


//Imports para la interfaz gráfica de la ventana de inicio de sesión
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
//import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JPanel;
//Imports para recoger los datos de inicio de sesión: contrasena y correo
import javax.swing.JTextField;
import javax.swing.JPasswordField;

public class VentanaInicioSesion extends JFrame {
    private JTextField nombreUsuarioField;
    private JPasswordField contrasenaUsuarioField;

    // Constructor: dimensiones, campos, botones,
    public VentanaInicioSesion(String correoGuardado, String contrasenaGuardada) {
        
        //Creamos la ventana y colocamos un título
        super("Inicio de Sesión"); //herencia de JFrame
        setResizable(false); //No se puede maximizar la ventana

        //Para que finalice la aplicacion al cerrar la ventana
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Establecemos el tamaño de la ventana
        setSize(500, 500);

        //Establecemos la ubicación de la ventana en el centro de la pantalla
        setLocationRelativeTo(null);

        //Creamos el panel y las etiquetas y botones en él
        JPanel panel = Panel();

        //Creamos los botones de registrarse e iniciar sesión
        crearBotones(panel, correoGuardado, contrasenaGuardada);     






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
        etiquetaTituloCentral.setOpaque(true);

        //Crear una etiqueta para el usuario
        JLabel etiquetaUsuario = new JLabel("Usuario:");
        etiquetaUsuario.setBounds(130, 160, 100, 20);

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
        nombreUsuarioField.setText("correo123@gmail.com");        

        //Crear campo de texto para la contraseña
        contrasenaUsuarioField = new JPasswordField();
        contrasenaUsuarioField.setBounds(130, 240, 230, 30);

        //Campos de una linea de texto
        panel.add(nombreUsuarioField);        
        panel.add(contrasenaUsuarioField);
    }

    private void crearBotones(JPanel panel, String correoGuardado, String contrasenaGuardada) {
        //Boton para iniciar sesión
        JButton botonIniciarSesion = new JButton("Iniciar Sesión");
        botonIniciarSesion.setBounds(165, 300, 150, 30);

        //Boton para ir a registrarse
        JButton botonRegistro = new JButton("Registrarse");
        botonRegistro.setBounds(270, 365, 150, 30);       

            
        //Logica para iniciar sesión e ingresar al menu de usuario
        //Falta ingresar la clase de menu de usuario para que se pueda abrir la ventana del menu de usuario
        botonIrMenuUsuario(botonIniciarSesion, correoGuardado, contrasenaGuardada); 
        //Falta ingresar la clase de registro para que se pueda abrir la ventana de registro
        botonIrRegistro(botonRegistro); 


        panel.add(botonIniciarSesion);
        panel.add(botonRegistro);            

    }

    //Lógica para abrir la ventana del menu de usuario al iniciar sesión
    private void botonIrMenuUsuario( JButton botonIniciarSesion, String correoGuardado, String contrasenaGuardada) {

        //Vincular el listener al boton de iniciar sesión
        botonIniciarSesion.addActionListener(e -> {
            //Extraer los valores de los campos de texto
            String correoRecibido = nombreUsuarioField.getText();
            String contrasenaRecibida = new String(contrasenaUsuarioField.getPassword());
            //Validación para verificar si el correo y la contraseña coinciden con los datos guardados del "Modelo"
            if(correoRecibido.equals(correoGuardado) && contrasenaRecibida.equals(contrasenaGuardada)) {
                // Iniciar sesión exitosa: ir al menu de usuario
                

                this.dispose(); // Cierra la ventana de inicio de sesión
                // Lógica para abrir la ventana principal del usuario

            } else{
                //Se borra el contenido de los campos de texto para que el usuario vuelva a ingresar sus datos
                nombreUsuarioField.setText("");
                contrasenaUsuarioField.setText("");
                nombreUsuarioField.requestFocus(); //Coloca el cursor en el campo de correo
                //JOptionPane.showMessageDialog(null, "Correo o contraseña incorrectos.");
            }

        }); 

    }
    
    private void botonIrRegistro(JButton botonRegistro) {
        //Vincular el listener al boton de registrarse
        botonRegistro.addActionListener(e -> {
            // Lógica para abrir la ventana de registro
            
            this.dispose(); // Cierra la ventana de inicio de sesión
        });
    }   
    
    //Prueba de la ventana de inicio de sesión
    public static void main(String[] args) {
        VentanaInicioSesion ventana = new VentanaInicioSesion("","");
        ventana.setVisible(true);
        int a=0;
        JOptionPane.showMessageDialog(null, a);
    }



}
