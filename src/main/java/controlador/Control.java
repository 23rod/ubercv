package controlador;

import modelo.Resultado;
import modelo.Rol;
import modelo.Mensajes;

public class Control {
    
    private Rol rolSesion = Rol.NINGUNO;

    public Control() {
        // Constructor listo para inicializar servicios y repositorios en el siguiente paso
    }

    public Resultado<Rol> iniciarSesion(String correo, String clave) {
        // STUB para PR #1. Retorna error por defecto para que la vista de Edgar no falle.
        return Resultado.error(Mensajes.LOGIN_INCORRECTO);
    }

    public Resultado<Void> registrarUsuario(String correo, String clave, String confirmacion, String cedula, Rol rol) {
        // STUB para PR #1.
        return Resultado.error("Lógica de registro en construcción");
    }

    public void abrirVentanaPrincipal() {
        // Aquí conectaremos con la VistaConcreta / Main en la integración
        System.out.println("Navegando a la ventana principal...");
    }

    public void cerrarSesion() {
        this.rolSesion = Rol.NINGUNO;
        System.out.println("Sesión cerrada. Navegando al Login...");
    }

    public Rol getRolSesion() {
        return rolSesion;
    }
}