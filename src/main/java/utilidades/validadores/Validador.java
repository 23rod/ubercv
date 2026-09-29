package utilidades.validadores;

import java.util.regex.Pattern;

public class Validador {

    // Algo@algo.com, sin espacios y con una sola arroba (HU01: correo @____.com)
    private static final Pattern CORREO =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.com$", Pattern.CASE_INSENSITIVE);

    public static boolean esCedulaValida(String cedula) {
        return cedula != null && cedula.matches("\\d{8}");
    }

    public static boolean esCorreoValido(String correo) {
        return correo != null && CORREO.matcher(correo.trim()).matches();
    }

    public static boolean esClaveValida(String clave) {
        return clave != null && clave.length() >= 8;
    }
}
