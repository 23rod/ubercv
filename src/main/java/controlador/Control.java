package controlador;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Window;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import modelo.Resultado;
import modelo.Rol;
import persistencia.memoria.DatosSemilla;
import persistencia.memoria.RepositorioUsuarios;
import servicio.ServicioUsuarios;
import vista.PlantillaVista.MenuInicio;
import vista.PlantillaVista.VistaConcreta;
import vista.PlantillaVista.CuerposDelSistema.CuerpoFuncionesAdmin;
import vista.PlantillaVista.CuerposDelSistema.CuerpoFuncionesConductor;
import vista.PlantillaVista.CuerposDelSistema.CuerpoFuncionesUsuario;
import vista.PlantillaVista.CuerposDelSistema.CuerpoRegistro;
import vista.PlantillaVista.CuerposDelSistema.CuerpoRegistroRuta;
import vista.PlantillaVista.CuerposDelSistema.CuerpoRegistroUnidad;

/**
 * Puente entre las vistas Swing y la lógica. Las vistas solo llaman a estos métodos;
 * la lógica de negocio vive en los servicios (que no conocen Swing).
 * Main crea UN solo Control y se lo pasa a las ventanas, así todas comparten usuarios y sesión.
 */
public class Control {

    private final ServicioUsuarios servicioUsuarios;

    public Control() {
        RepositorioUsuarios repositorioUsuarios = new RepositorioUsuarios();
        DatosSemilla.sembrarUsuarios(repositorioUsuarios); // cuentas de demo
        this.servicioUsuarios = new ServicioUsuarios(repositorioUsuarios);
    }

    // ------------------------------------------------------------------ Lógica
    public Resultado<Rol> iniciarSesion(String correo, String clave) {
        return servicioUsuarios.iniciarSesion(correo, clave);
    }

    public Resultado<Void> registrarUsuario(String correo, String clave, String confirmacion, String cedula, Rol rol) {
        return servicioUsuarios.registrar(correo, clave, confirmacion, cedula, rol);
    }

    public Rol getRolSesion() {
        return servicioUsuarios.getRolSesion();
    }

    // -------------------------------------------------------------- Navegación
    /** Pantalla de bienvenida (MenuInicio). */
    public void abrirMenuInicio() {
        mostrar(new MenuInicio(this));
    }

    /** HU01: ventana de registro. Al registrar con éxito regresa al menú principal. */
    public void abrirRegistro() {
        CuerpoRegistro cuerpo = new CuerpoRegistro();
        VistaConcreta vista = new VistaConcreta(conBotonVolver(cuerpo, this::abrirMenuInicio));
        prepararVentana(vista, false); // sin sesión: el botón Cerrar Sesión se oculta

        cuerpo.getBotonConfirmar().addActionListener(e -> {
            Resultado<Void> r = registrarUsuario(
                    cuerpo.getCampoCorreo().getText(),
                    new String(cuerpo.getCampoContrasenia().getPassword()),
                    new String(cuerpo.getCampoConfirmarContrasenia().getPassword()),
                    cuerpo.getCampoCedula().getText(),
                    Rol.desdeTexto(cuerpo.getRolSeleccionado()));
            JOptionPane.showMessageDialog(vista, r.getMensaje());
            if (r.isOk()) {
                abrirMenuInicio();
            }
        });
        mostrar(vista);
    }

    /** HU02: tras iniciar sesión, la pantalla depende del rol. */
    public void abrirVentanaPrincipal() {
        Rol rol = getRolSesion();
        if (rol == Rol.NINGUNO) { // sin sesión no hay ventana principal
            abrirMenuInicio();
            return;
        }

        CuerpoFuncionesAdmin menuAdmin = null;
        JPanel cuerpo;
        switch (rol) {
            case ADMIN_TRANSPORTE:
                menuAdmin = new CuerpoFuncionesAdmin();
                cuerpo = menuAdmin;
                break;
            case CONDUCTOR:
                cuerpo = new CuerpoFuncionesConductor();
                break;
            default: // ESTUDIANTE y EMPLEADO
                cuerpo = new CuerpoFuncionesUsuario();
        }

        VistaConcreta vista = new VistaConcreta(cuerpo);
        prepararVentana(vista, true);
        vista.getEncabezadoGeneral().getBotonCerrarSesion().addActionListener(e -> cerrarSesion());
        if (menuAdmin != null) {
            conectarMenuAdmin(vista, menuAdmin);
        }
        mostrar(vista);
    }

    public void cerrarSesion() {
        servicioUsuarios.cerrarSesion();
        abrirMenuInicio();
    }

    // ----------------------------------------------------------------- Interno
    /** Menú del admin: Flota e Itinerarios (solo ADMIN_TRANSPORTE). */
    private void conectarMenuAdmin(VistaConcreta vista, CuerpoFuncionesAdmin menu) {
        menu.getBotonFlota().addActionListener(e -> {
            if (accesoAdmin(vista)) {
                vista.mostrarCuerpo(conBotonVolver(new CuerpoRegistroUnidad(), () -> vista.mostrarCuerpo(menu)));
            }
        });
        menu.getBotonItinerario().addActionListener(e -> {
            if (accesoAdmin(vista)) {
                vista.mostrarCuerpo(conBotonVolver(new CuerpoRegistroRuta(), () -> vista.mostrarCuerpo(menu)));
            }
        });
    }

    private boolean accesoAdmin(VistaConcreta vista) {
        Resultado<Void> acceso = servicioUsuarios.exigirRol(Rol.ADMIN_TRANSPORTE);
        if (!acceso.isOk()) {
            JOptionPane.showMessageDialog(vista, acceso.getMensaje());
        }
        return acceso.isOk();
    }

    /** Envuelve un cuerpo con una barra superior "Volver al menú" sin tocar el código de las vistas. */
    private JPanel conBotonVolver(JPanel cuerpo, Runnable accionVolver) {
        JButton volver = new JButton("Volver al menú");
        volver.addActionListener(e -> accionVolver.run());
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barra.setBackground(Color.WHITE);
        barra.add(volver);
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(Color.WHITE);
        contenedor.add(barra, BorderLayout.NORTH);
        contenedor.add(cuerpo, BorderLayout.CENTER);
        return contenedor;
    }

    private void prepararVentana(VistaConcreta vista, boolean conSesion) {
        vista.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        vista.setLocationRelativeTo(null);
        vista.getEncabezadoGeneral().mostrarBotonCerrarSesion(conSesion);
    }

    /** Cierra las demás ventanas abiertas y muestra la nueva (una sola ventana activa a la vez). */
    private void mostrar(Window nueva) {
        for (Window ventana : Window.getWindows()) {
            if (ventana != nueva && ventana.isDisplayable()) {
                ventana.dispose();
            }
        }
        nueva.setVisible(true);
    }
}
