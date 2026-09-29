package persistencia.memoria;

import java.util.HashMap;
import java.util.Map;
import modelo.Rol;

public class DatosSemilla {
    private static final Map<String, Rol> PADRON = new HashMap<>();

    static {
        // simulado
        PADRON.put("30049983", Rol.ESTUDIANTE); // Paul
        PADRON.put("31527374", Rol.ESTUDIANTE); // Sergio
        PADRON.put("32111397", Rol.ESTUDIANTE); // Edgar
        PADRON.put("12345678", Rol.ADMIN_TRANSPORTE); // Admin default
    }

    public static Rol validarEnPadron(String cedula) {
        return PADRON.getOrDefault(cedula, Rol.NINGUNO);
    }
}