package utilidades;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DineroTest {

    @Test
    @DisplayName("Dado un texto numérico válido, cuando se parsea, entonces se obtiene un BigDecimal con 2 decimales")
    void parseaTextosValidosConPuntoOComa() {
        assertEquals(new BigDecimal("12.00"), Dinero.parsearDecimal("12"));
        assertEquals(new BigDecimal("12.50"), Dinero.parsearDecimal("12.5"));
        assertEquals(new BigDecimal("12.50"), Dinero.parsearDecimal("12,50"));
        assertEquals(new BigDecimal("0.01"), Dinero.parsearDecimal(" 0.01 "));
    }

    @Test
    @DisplayName("Dado un texto que no es un monto válido, cuando se parsea, entonces devuelve null")
    void rechazaTextosInvalidos() {
        for (String malo : new String[] {null, "", "  ", "abc", "-1", "1.234", "1e3", "12.", ".5", "1.2.3", "1234567890"}) {
            assertNull(Dinero.parsearDecimal(malo), "debía rechazar: " + malo);
        }
    }

    @Test
    @DisplayName("Dado un monto y un porcentaje, cuando se calcula, entonces se redondea una sola vez al final (HALF_UP)")
    void calculaElPorcentajeConRedondeoHalfUp() {
        assertEquals(new BigDecimal("1.88"), Dinero.porcentajeDe(new BigDecimal("12.50"), new BigDecimal("15")));
        assertEquals(new BigDecimal("5.63"), Dinero.porcentajeDe(new BigDecimal("12.50"), new BigDecimal("45")));
        assertEquals(new BigDecimal("100.00"), Dinero.porcentajeDe(new BigDecimal("100.00"), new BigDecimal("100")));
    }

    @Test
    @DisplayName("Dado un monto, cuando se formatea, entonces siempre lleva 2 decimales y punto decimal")
    void formateaConDosDecimales() {
        assertEquals("3.00", Dinero.formatear(new BigDecimal("3")));
        assertEquals("1.88", Dinero.formatear(new BigDecimal("1.875")));
        assertEquals("0.00", Dinero.formatear(Dinero.CERO));
    }

    @Test
    @DisplayName("Dado que 0.1 + 0.2 falla con double, cuando se suma con BigDecimal, entonces da exactamente 0.30")
    void laAritmeticaDecimalEsExacta() {
        BigDecimal suma = Dinero.parsearDecimal("0.1").add(Dinero.parsearDecimal("0.2"));
        assertEquals(new BigDecimal("0.30"), suma);
    }
}
