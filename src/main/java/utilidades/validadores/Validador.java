package utilidades.validadores;

import java.time.LocalTime;
import java.util.regex.Pattern;

public class Validador {

    public static final int CAPACIDAD_MAX = 120;
    public static final int MAX_MODELO = 40;
    public static final int MAX_NOMBRE_RUTA = 60;

    // Algo@algo.com, sin espacios y con una sola arroba (HU01: correo @____.com)
    private static final Pattern CORREO =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.com$", Pattern.CASE_INSENSITIVE);
    // De 5 a 8 letras o números (cubre placas como AB123CD o ABC123)
    private static final Pattern PLACA = Pattern.compile("^[A-Z0-9]{5,8}$", Pattern.CASE_INSENSITIVE);
    // Hora de 24 horas: H:mm o HH:mm
    private static final Pattern HORA = Pattern.compile("^([01]?\\d|2[0-3]):[0-5]\\d$");

    public static boolean esCedulaValida(String cedula) {
        return cedula != null && cedula.matches("\\d{5,10}");
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

    /** Número entero entre 1 y CAPACIDAD_MAX (120). */
    public static boolean esCapacidadValida(String capacidad) {
        if (capacidad == null) {
            return false;
        }
        String texto = capacidad.trim();
        if (!texto.matches("\\d{1,3}")) {
            return false;
        }
        int valor = Integer.parseInt(texto);
        return valor >= 1 && valor <= CAPACIDAD_MAX;
    }

    /** true si el texto (sin espacios en los extremos) no supera el largo máximo. */
    public static boolean cabe(String texto, int maximo) {
        return texto != null && texto.trim().length() <= maximo;
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
