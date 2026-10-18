package controlador;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Window;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import vista.PlantillaVista.VistaConcreta;

/**
 * Utilidades de ventanas compartidas por todos los controladores: mostrar una sola ventana activa,
 * preparar la ventana principal y envolver un cuerpo con la barra "Volver al menú".
 * No contiene lógica de negocio ni conoce los servicios.
 */
class Navegador {

    /** Cierra las demás ventanas abiertas y muestra la nueva (una sola ventana activa a la vez). */
    void mostrar(Window nueva) {
        for (Window ventana : Window.getWindows()) {
            if (ventana != nueva && ventana.isDisplayable()) {
                ventana.dispose();
            }
        }
        nueva.setVisible(true);
    }

    void prepararVentana(VistaConcreta vista, boolean conSesion) {
        vista.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        vista.setLocationRelativeTo(null);
        vista.getEncabezadoGeneral().mostrarBotonCerrarSesion(conSesion);
        vista.getEncabezadoGeneral().mostrarInfoUsuario(conSesion); // sin sesión no hay panel de usuario/rol/saldo
    }

    /** Envuelve un cuerpo con una barra superior "Volver al menú" (y un panel inferior opcional) sin tocar el código de las vistas. */
    JPanel conBotonVolver(String titulo, JPanel cuerpo, JPanel inferior, Runnable accionVolver) {
        JButton volver = new JButton("Volver al menú");
        volver.addActionListener(e -> accionVolver.run());

        JLabel etiquetaTitulo = new JLabel(titulo, SwingConstants.CENTER);
        etiquetaTitulo.setFont(new Font("Arial", Font.BOLD, 22));

        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(Color.WHITE);
        barra.add(volver, BorderLayout.WEST);
        barra.add(etiquetaTitulo, BorderLayout.CENTER);
        // Panel vacío a la derecha del mismo ancho para que el título quede exactamente en el centro
        JPanel espaciador = new JPanel();
        espaciador.setOpaque(false);
        espaciador.setPreferredSize(volver.getPreferredSize());
        barra.add(espaciador, BorderLayout.EAST);

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(Color.WHITE);
        contenedor.add(barra, BorderLayout.NORTH);
        contenedor.add(cuerpo, BorderLayout.CENTER);
        if (inferior != null) {
            contenedor.add(inferior, BorderLayout.SOUTH);
        }
        return contenedor;
    }
}
