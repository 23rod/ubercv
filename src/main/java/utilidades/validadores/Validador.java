package utilidades.validadores;

import java.time.LocalTime;
import java.util.regex.Pattern;

public class Validador {

    // Algo@algo.com, sin espacios y con una sola arroba (HU01: correo @____.com)
    private static final Pattern CORREO =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.com$", Pattern.CASE_INSENSITIVE);
    // De 5 a 8 letras o números (cubre placas como AB123CD o ABC123)
    private static final Pattern PLACA = Pattern.compile("^[A-Z0-9]{5,8}$", Pattern.CASE_INSENSITIVE);
    // Hora de 24 horas: H:mm o HH:mm
    private static final Pattern HORA = Pattern.compile("^([01]?\\d|2[0-3]):[0-5]\\d$");

    public static boolean esCedulaValida(String cedula) {
        return cedula != null && cedula.matches("\\d{8}");
    }

    public static boolean esCorreoValido(String correo) {
        return correo != null && CORREO.matcher(correo.trim()).matches();
    }

    public static boolean esClaveValida(String clave) {
        return clave != null && clave.length() >= 8;
    }

    public static boolean esTextoNoVacio(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }

    public static boolean esPlacaValida(String placa) {
        return placa != null && PLACA.matcher(placa.trim()).matches();
    }

    /** Número natural (entero mayor que cero) de hasta 4 dígitos. */
    public static boolean esCapacidadValida(String capacidad) {
        if (capacidad == null) {
            return false;
        }
        String texto = capacidad.trim();
        return texto.matches("\\d{1,4}") && Integer.parseInt(texto) > 0;
    }

    /** Convierte "H:mm" o "HH:mm" a LocalTime. Devuelve null si el formato no es válido. */
    public static LocalTime parsearHora(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.trim();
        if (!HORA.matcher(limpio).matches()) {
            return null;
        }
        String[] partes = limpio.split(":");
        return LocalTime.of(Integer.parseInt(partes[0]), Integer.parseInt(partes[1]));
    }
}
