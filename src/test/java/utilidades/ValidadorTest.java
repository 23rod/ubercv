package utilidades;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import utilidades.validadores.Validador;

class ValidadorTest {

    @Test
    void correosValidos() {
        assertTrue(Validador.esCorreoValido("ana@ucv.com"));
        assertTrue(Validador.esCorreoValido("  Ana.Perez@Gmail.COM "));
    }

    @Test
    void correosInvalidos() {
        assertFalse(Validador.esCorreoValido(null));
        assertFalse(Validador.esCorreoValido(""));
        assertFalse(Validador.esCorreoValido("ana@ucv"));
        assertFalse(Validador.esCorreoValido("ana@@ucv.com"));
        assertFalse(Validador.esCorreoValido("@ucv.com"));
        assertFalse(Validador.esCorreoValido("ana@.com"));
        assertFalse(Validador.esCorreoValido("a na@ucv.com"));
    }

    @Test
    void cedulaDebeTenerOchoDigitos() {
        assertTrue(Validador.esCedulaValida("12345678"));
        assertFalse(Validador.esCedulaValida("1234567"));
        assertFalse(Validador.esCedulaValida("123456789"));
        assertFalse(Validador.esCedulaValida("1234567a"));
        assertFalse(Validador.esCedulaValida(null));
    }

    @Test
    void claveDebeTenerAlMenosOchoCaracteres() {
        assertTrue(Validador.esClaveValida("12345678"));
        assertFalse(Validador.esClaveValida("1234567"));
        assertFalse(Validador.esClaveValida(null));
    }
}
