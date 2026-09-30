package vista.PlantillaVista;


import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

//Imports para el formato de la fuente de las etiquetas
import java.awt.Font;


//Imports para la imagen de fondo
import javax.swing.ImageIcon;
import javax.swing.JButton;

import java.awt.Image;


//Imports para la dimension de la ventana (Que abarque todo el monitor)
import java.awt.Toolkit;
import java.awt.Color;
import java.awt.Dimension;

//Barra de desplazamiento hacia abajo 
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import controlador.Control;


public class MenuInicio extends JFrame {    

    private final Control control; // unico Control de la aplicacion (lo crea Main)

    public MenuInicio(Control control) {
        
        //Inicializamos la ventana y le damos un nombre a la ventana
        super("Sistema Gestión de Transporte: UberCV");
        this.control = control;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Obtenemos las dimensiones del monitor 
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        //Establecemos el tamaño de la ventana al tamaño del monitor
        setSize(screenSize.width, screenSize.height);
        setLocation(0,0); //Ubicarla en la esquina superior izquierda de la pantalla

        agregarComponentes();        
    }

    private void agregarComponentes(){
        JPanel panel = new JPanel();
        panel.setLayout(null);

        //Agregamos las etiquetas
        agregarEtiquetas(panel);
        agregarBotones(panel);

        // Definir explícitamente el ancho y el alto total del panel
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        panel.setPreferredSize(new Dimension(screenSize.width, 1800));

        //Agregamos la barra de desplazamiento
        JScrollPane scrollPane = new JScrollPane(panel);

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane);
    }

    private void agregarEtiquetas(JPanel panel) {
        // Implementación de etiquetas
        JLabel etiquetaLogo = new JLabel("UberCV");
        JLabel etiquetaTitulo = new JLabel("Sistema  Gestión de Transporte");

        //Etiqueta superior izquierda
        etiquetaLogo.setBounds(15, 15, 200, 30);
        etiquetaLogo.setFont(new Font("Arial", Font.BOLD, 24));
        etiquetaLogo.setOpaque(false);

        //Etiqueta central: Titulo del sistema
        etiquetaTitulo.setBounds(250, 80, 1000, 70);
        etiquetaTitulo.setFont(new Font("Arial", Font.PLAIN, 60));
        etiquetaTitulo.setOpaque(false);

        //Insertar una imagen debajo de la etiqueta central
        ImageIcon imagenBuses = new ImageIcon("src/main/java/vista/PlantillaVista/ImagenesDeInicio/autobusesUCV.jpg");

        //Dar tamaño a la imagen 
        Image imagenBusesRedimensionada = imagenBuses.getImage().getScaledInstance(800, 400, Image.SCALE_SMOOTH);

        ImageIcon imagenBusesFinal = new ImageIcon(imagenBusesRedimensionada);
        JLabel etiquetaImagenBuses = new JLabel(imagenBusesFinal);
        etiquetaImagenBuses.setBounds(280, 160, 800, 400);

        //Crear una etiqueta para: "Porque el verdadero parcial es lograr llegar a la clase."
        JLabel etiquetaFraseSlogan = new JLabel("'Porque el verdadero parcial es lograr llegar a la clase'");
        etiquetaFraseSlogan.setBounds(490, 600, 800, 30);
        etiquetaFraseSlogan.setFont(new Font("Times New Roman", Font.ITALIC, 16));

        JLabel etiquetaFrasePatrocinanteJLabel = new JLabel("Con la confianza de:");
        etiquetaFrasePatrocinanteJLabel.setBounds(120, 630, 100, 30);
        etiquetaFrasePatrocinanteJLabel.setFont(new Font("Times New Roman", Font.ITALIC, 12));

        agregarImagenesDePatrocinantes(panel);

        //Añadir las etiquetas restantes luego de y= 1000

        //Beneficios: etiquetas
        JLabel etiquetaBeneficios = new JLabel();
        etiquetaBeneficios.setBounds(120, 1050, 200, 30);
        etiquetaBeneficios.setText("Beneficios :");
        etiquetaBeneficios.setFont(new Font("Times New Roman", Font.PLAIN, 14));

        JLabel tituloBeneficios = new JLabel();
        tituloBeneficios.setBounds(120, 1080, 700, 60);
        tituloBeneficios.setText("Máxima Comodidad para tu Entorno Universitario");
        tituloBeneficios.setFont(new Font("Arial", Font.BOLD, 25));

        JLabel subtituloBeneficios = new JLabel();
        subtituloBeneficios.setBounds(120, 1150, 700, 30);
        subtituloBeneficios.setText("Estudiantes, profesores y todo el personal gozan de transporte a su localidad ida y vuelta.");
        subtituloBeneficios.setFont(new Font("Times New Roman", Font.PLAIN, 14));

        JTextArea beneficios1 = new JTextArea();
        beneficios1.setBounds(200, 1300, 200, 150);
        beneficios1.setText("Reserva Tu Viaje:\n\n"+
            "Con el nuevo sistema de gestión\n"+
            "tienes la facilidad de reservar un\n"+
            "puesto en tu ruta y horario de\n"+
            "conveniencia.");
            beneficios1.setFont(new Font("Arial", Font.PLAIN, 12));
            beneficios1.setBackground(Color.WHITE);
            beneficios1.setEditable(false);

        JTextArea beneficios2 = new JTextArea();
        beneficios2.setBounds(550, 1300, 220, 150);
        beneficios2.setText("Rutas Por Toda La Gran Caracas:\n\n"+
            "Paradas de autobús distribuidas para\n"+
            "el fácil acceso al transporte de todos\n"+
            "los pertenecientes al ecosistema\n"+
            "universitario.");
        beneficios2.setFont(new Font("Arial", Font.PLAIN, 12));
        beneficios2.setEditable(false);
        beneficios2.setBackground(Color.WHITE);

        JTextArea beneficios3 = new JTextArea();
        beneficios3.setBounds(950, 1300, 220, 150);
        beneficios3.setText("Visualiza el Estado de tu Viaje:\n\n"+
            "Aprovecha al máximo la función de\n"+
            "simulación donde se te informará el\n"+
            "estado de tu viaje y transporte.\n");
        beneficios3.setFont(new Font("Arial", Font.PLAIN, 12));
        beneficios3.setEditable(false);
        beneficios3.setBackground(Color.WHITE);

        panel.add(etiquetaLogo);
        panel.add(etiquetaTitulo);
        panel.add(etiquetaImagenBuses);
        panel.add(etiquetaFraseSlogan);
        panel.add(etiquetaFrasePatrocinanteJLabel);

        //Beneficios
        panel.add(etiquetaBeneficios);
        panel.add(tituloBeneficios);
        panel.add(subtituloBeneficios);
        panel.add(beneficios1);
        panel.add(beneficios2);
        panel.add(beneficios3);
    }

    private void agregarImagenesDePatrocinantes(JPanel panel) {
        // Implementación de imágenes de patrocinantes
        //Insertar los logos de los patrocinantes
        //#1

        JLabel fondoAzul = new JLabel();
        fondoAzul.setBounds(200, 690, 950, 180);
        fondoAzul.setOpaque(true);
        fondoAzul.setBackground(Color.GRAY);
        
        ImageIcon logoPatrocinante1 = new ImageIcon("src/main/java/vista/PlantillaVista/ImagenesDeInicio/logoComputacion-1024x1024.jpg");
        Image logoPatrocinante1Redimensionada = logoPatrocinante1.getImage().getScaledInstance(200, 150, Image.SCALE_SMOOTH);
        ImageIcon logoPatrocinante1Final = new ImageIcon(logoPatrocinante1Redimensionada);
        JLabel etiquetaLogoPatrocinante1 = new JLabel(logoPatrocinante1Final);
        etiquetaLogoPatrocinante1.setBounds(250, 700, 200, 150);

        // #2
        ImageIcon logoPatrocinante2 = new ImageIcon("src/main/java/vista/PlantillaVista/ImagenesDeInicio/facultadCiencias.jpg");
        Image logoPatrocinante2Redimensionada = logoPatrocinante2.getImage().getScaledInstance(200, 150, Image.SCALE_SMOOTH);
        ImageIcon logoPatrocinante2Final = new ImageIcon(logoPatrocinante2Redimensionada);
        JLabel etiquetaLogoPatrocinante2 = new JLabel(logoPatrocinante2Final);
        etiquetaLogoPatrocinante2.setBounds(550, 700, 200, 150);

        //#3
        ImageIcon logoPatrocinante3 = new ImageIcon("src/main/java/vista/PlantillaVista/ImagenesDeInicio/ucv.png");
        Image logoPatrocinante3Redimensionada = logoPatrocinante3.getImage().getScaledInstance(200, 150, Image.SCALE_SMOOTH);
        ImageIcon logoPatrocinante3Final = new ImageIcon(logoPatrocinante3Redimensionada);
        JLabel etiquetaLogoPatrocinante3 = new JLabel(logoPatrocinante3Final);
        etiquetaLogoPatrocinante3.setBounds(850, 700, 200, 150);

        panel.add(etiquetaLogoPatrocinante1);
        panel.add(etiquetaLogoPatrocinante2);
        panel.add(etiquetaLogoPatrocinante3);
        panel.add(fondoAzul);
    }

    private void agregarBotones(JPanel panel) {
        // Implementación de botones
        JButton botonIniciarSesion = new JButton("Iniciar Sesión");
        botonIniciarSesion.setBounds(1100, 30, 150, 30);
        botonIniciarSesion.setBackground(Color.BLUE);
        botonIniciarSesion.setForeground(Color.WHITE);
        botonIniciarSesion.addActionListener(e -> {
            // Acción al hacer clic en el botón "Iniciar Sesión"
            //VERIFICAR EL ARGUMENTO DEL CONSTRUCTOR "CONTROL"
            VentanaInicioSesion ventanaInicioSesion = new VentanaInicioSesion(control);
            ventanaInicioSesion.setVisible(true);
        });
        panel.add(botonIniciarSesion);
    }



    public static void main(String[] args) {
        MenuInicio menuInicio = new MenuInicio(new Control());
        menuInicio.setVisible(true);
    }
    
}
