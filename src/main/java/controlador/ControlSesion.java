package controlador;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import modelo.Resultado;
import modelo.Rol;
import servicio.ServicioUsuarios;
import vista.PlantillaVista.MenuInicio;
import vista.PlantillaVista.VistaConcreta;
import vista.PlantillaVista.CuerposDelSistema.CuerpoRegistro;

/** HU01 y HU02: registro, inicio y cierre de sesión. */
class ControlSesion {

    private final ServicioUsuarios servicioUsuarios;
    private final Navegador navegador;
    private final Control control; // las vistas de bienvenida y login reciben el Control único de la app

    ControlSesion(ServicioUsuarios servicioUsuarios, Navegador navegador, Control control) {
        this.servicioUsuarios = servicioUsuarios;
        this.navegador = navegador;
        this.control = control;
    }

    Resultado<Rol> iniciarSesion(String correo, String clave) {
        return servicioUsuarios.iniciarSesion(correo, clave);
    }

    Resultado<Void> registrarUsuario(String correo, String clave, String confirmacion, String cedula, Rol rol) {
        return servicioUsuarios.registrar(correo, clave, confirmacion, cedula, rol);
    }

    Rol getRolSesion() {
        return servicioUsuarios.getRolSesion();
    }

    /** Pantalla de bienvenida (MenuInicio). */
    void abrirMenuInicio() {
        navegador.mostrar(new MenuInicio(control));
    }

    /** HU01: ventana de registro. Al registrar con éxito entra directo a su pantalla principal. */
    void abrirRegistro() {
        CuerpoRegistro cuerpo = new CuerpoRegistro();
        VistaConcreta vista = new VistaConcreta(navegador.conBotonVolver("Registro de Usuario", cuerpo, null, this::abrirMenuInicio));
        vista.getRootPane().setDefaultButton(cuerpo.getBotonConfirmar());
        navegador.prepararVentana(vista, false); // sin sesión: el botón Cerrar Sesión se oculta
        vista.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        cuerpo.getBotonConfirmar().addActionListener(e -> {
            Resultado<Void> r = registrarUsuario(
                    cuerpo.getCampoCorreo().getText(),
                    new String(cuerpo.getCampoContrasenia().getPassword()),
                    new String(cuerpo.getCampoConfirmarContrasenia().getPassword()),
                    cuerpo.getCampoCedula().getText(),
                    Rol.desdeTexto(cuerpo.getRolSeleccionado()));
            JOptionPane.showMessageDialog(vista, r.getMensaje());
            if (r.isOk()) {
                iniciarSesion(cuerpo.getCampoCorreo().getText(), new String(cuerpo.getCampoContrasenia().getPassword()));
                control.abrirVentanaPrincipal();
            }
        });
        vista.setVisible(true);
    }

    void cerrarSesion() {
        servicioUsuarios.cerrarSesion();
        abrirMenuInicio();
    }
}
