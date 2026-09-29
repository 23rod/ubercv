package utilidades.seguridad;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

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
            byte[] hash = md.digest(clave.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error en algoritmo de hash", e);
        }
    }
}