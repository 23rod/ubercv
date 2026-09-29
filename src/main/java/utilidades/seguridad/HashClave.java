package utilidades.seguridad;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Guarda claves como hash SHA-256 con sal: nunca se almacena la clave en texto plano. */
public class HashClave {

    public static byte[] generarSal() {
        byte[] sal = new byte[16];
        new SecureRandom().nextBytes(sal);
        return sal;
    }

    public static String hashearClave(String clave, byte[] sal) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(sal);
            byte[] hash = md.digest(clave.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error en algoritmo de hash", e);
        }
    }

    /** Compara en tiempo constante (MessageDigest.isEqual) para no filtrar información por tiempos. */
    public static boolean coincide(String clave, byte[] sal, String hashEsperado) {
        if (clave == null || sal == null || hashEsperado == null) {
            return false;
        }
        byte[] calculado = hashearClave(clave, sal).getBytes(StandardCharsets.UTF_8);
        byte[] esperado = hashEsperado.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(calculado, esperado);
    }
}
