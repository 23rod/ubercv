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
    void cedulaDebeTenerDeCincoADiezDigitos() {
        assertTrue(Validador.esCedulaValida("12345"));      // mínimo: 5 dígitos
        assertTrue(Validador.esCedulaValida("12345678"));
        assertTrue(Validador.esCedulaValida("1234567890")); // máximo: 10 dígitos
        assertFalse(Validador.esCedulaValida("1234"));
        assertFalse(Validador.esCedulaValida("12345678901"));
        assertFalse(Validador.esCedulaValida("1234567a"));
        assertFalse(Validador.esCedulaValida("V-12345678"));
        assertFalse(Validador.esCedulaValida(null));
    }

    @Test
    void claveDebeTenerAlMenosOchoCaracteres() {
        assertTrue(Validador.esClaveValida("12345678"));
        assertFalse(Validador.esClaveValida("1234567"));
        assertFalse(Validador.esClaveValida(null));
    }

    @Test
    void placaDeCincoAOchoLetrasONumeros() {
        assertTrue(Validador.esPlacaValida("ABC123"));
        assertTrue(Validador.esPlacaValida(" ab123cd "));
        assertFalse(Validador.esPlacaValida("AB12"));
        assertFalse(Validador.esPlacaValida("ABC-123"));
        assertFalse(Validador.esPlacaValida("ABCDEFGHI"));
        assertFalse(Validador.esPlacaValida(null));
    }

    @Test
    void capacidadEsUnEnteroMayorQueCero() {
        assertTrue(Validador.esCapacidadValida("40"));
        assertTrue(Validador.esCapacidadValida(" 1 "));
        assertTrue(Validador.esCapacidadValida("120"));
        assertFalse(Validador.esCapacidadValida("0"));
        assertFalse(Validador.esCapacidadValida("121"));
        assertFalse(Validador.esCapacidadValida("11111"));
        assertFalse(Validador.esCapacidadValida("-3"));
        assertFalse(Validador.esCapacidadValida("4.5"));
        assertFalse(Validador.esCapacidadValida("abc"));
        assertFalse(Validador.esCapacidadValida(""));
        assertFalse(Validador.esCapacidadValida(null));
    }

    @Test
    void horaEnFormatoDe24Horas() {
        assertEquals(java.time.LocalTime.of(6, 30), Validador.parsearHora("06:30"));
        assertEquals(java.time.LocalTime.of(6, 30), Validador.parsearHora("6:30"));
        assertEquals(java.time.LocalTime.of(23, 59), Validador.parsearHora(" 23:59 "));
        assertNull(Validador.parsearHora("24:00"));
        assertNull(Validador.parsearHora("12:60"));
        assertNull(Validador.parsearHora("abc"));
        assertNull(Validador.parsearHora(null));
    }

    @Test
    void textoConLimiteDeLongitud() {
        assertTrue(Validador.cabe("a".repeat(40), 40));
        assertTrue(Validador.cabe("  " + "a".repeat(40) + "  ", 40)); // los espacios de los extremos no cuentan
        assertFalse(Validador.cabe("a".repeat(41), 40));
        assertFalse(Validador.cabe(null, 40));
    }
}
