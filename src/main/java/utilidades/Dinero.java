package utilidades;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;

/**
 * Utilidades para dinero y porcentajes. Se usa BigDecimal (nunca double) con 2 decimales
 * y redondeo HALF_UP, para que los cálculos de tarifas y saldos sean exactos.
 */
public final class Dinero {

    public static final BigDecimal CERO = new BigDecimal("0.00");
    private static final BigDecimal CIEN = new BigDecimal("100");
    /** Hasta 9 dígitos enteros y hasta 2 decimales, con punto o coma ("12", "12.5", "12,50"). */
    private static final Pattern FORMATO = Pattern.compile("\\d{1,9}([.,]\\d{1,2})?");

    private Dinero() {
    }

    /** Convierte el texto escrito por el usuario a BigDecimal con 2 decimales, o devuelve null si no es válido. */
    public static BigDecimal parsearDecimal(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.trim();
        if (!FORMATO.matcher(limpio).matches()) {
            return null;
        }
        return new BigDecimal(limpio.replace(',', '.')).setScale(2, RoundingMode.HALF_UP);
    }

    /** Redondea a 2 decimales (HALF_UP). */
    public static BigDecimal redondear(BigDecimal monto) {
        return monto.setScale(2, RoundingMode.HALF_UP);
    }

    /** monto × porcentaje / 100, con un único redondeo al final. Ej.: 12.50 al 15 % = 1.88. */
    public static BigDecimal porcentajeDe(BigDecimal monto, BigDecimal porcentaje) {
        return monto.multiply(porcentaje).divide(CIEN, 2, RoundingMode.HALF_UP);
    }

    /** Texto con 2 decimales y punto decimal, sin depender del idioma del sistema. */
    public static String formatear(BigDecimal monto) {
        return redondear(monto).toPlainString();
    }
}
