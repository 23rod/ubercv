package utilidades.validadores;

public class Validador {
    
    public static boolean esCedulaValida(String cedula) {
        return cedula != null && cedula.matches("\\d{8}");
    }

    public static boolean esCorreoValido(String correo) {
        return correo != null && correo.matches("^.+@.+\\.com$");
    }

    public static boolean esClaveValida(String clave) {
        return clave != null && clave.length() >= 8;
    }
}