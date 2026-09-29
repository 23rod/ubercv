package controlador;

import modelo.Resultado;
import modelo.Rol;
import persistencia.memoria.DatosSemilla;
import persistencia.memoria.RepositorioUsuarios;
import servicio.ServicioUsuarios;

/** Puente entre las vistas Swing y la lógica. Las vistas solo llaman a estos métodos. */
public class Control {

    private final ServicioUsuarios servicioUsuarios;

    public Control() {
        RepositorioUsuarios repositorioUsuarios = new RepositorioUsuarios();
        DatosSemilla.sembrarUsuarios(repositorioUsuarios); // cuentas de demo
        this.servicioUsuarios = new ServicioUsuarios(repositorioUsuarios);
    }

    public Resultado<Rol> iniciarSesion(String correo, String clave) {
        return servicioUsuarios.iniciarSesion(correo, clave);
    }

    public Resultado<Void> registrarUsuario(String correo, String clave, String confirmacion, String cedula, Rol rol) {
        return servicioUsuarios.registrar(correo, clave, confirmacion, cedula, rol);
    }

    public void abrirVentanaPrincipal() {
        // Se conecta con VistaConcreta / mostrarCuerpo en la fase de integración
        System.out.println("Navegando a la ventana principal como: " + getRolSesion());
    }

    public void cerrarSesion() {
        servicioUsuarios.cerrarSesion();
        System.out.println("Sesión cerrada. Navegando al Login...");
    }

    public Rol getRolSesion() {
        return servicioUsuarios.getRolSesion();
    }
}
