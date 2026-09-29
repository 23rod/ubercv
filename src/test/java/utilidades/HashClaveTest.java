package utilidades;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import utilidades.seguridad.HashClave;

class HashClaveTest {

    @Test
    void mismaClaveYMismaSalDanElMismoHash() {
        byte[] sal = HashClave.generarSal();
        assertEquals(HashClave.hashearClave("Clave1234", sal), HashClave.hashearClave("Clave1234", sal));
    }

    @Test
    void salesDistintasDanHashesDistintos() {
        String h1 = HashClave.hashearClave("Clave1234", HashClave.generarSal());
        String h2 = HashClave.hashearClave("Clave1234", HashClave.generarSal());
        assertNotEquals(h1, h2);
    }

    @Test
    void coincideAceptaLaClaveCorrectaYRechazaLaIncorrecta() {
        byte[] sal = HashClave.generarSal();
        String hash = HashClave.hashearClave("Clave1234", sal);
        assertTrue(HashClave.coincide("Clave1234", sal, hash));
        assertFalse(HashClave.coincide("clave1234", sal, hash));
        assertFalse(HashClave.coincide(null, sal, hash));
    }
}
