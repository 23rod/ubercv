package modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Una operación del monedero virtual de un usuario. Es inmutable: una vez anotada no se modifica;
 * el saldo se obtiene sumando y restando los movimientos, así que nunca puede quedar desincronizado del historial.
 */
public final class Movimiento {

    private final String correo;
    private final TipoMovimiento tipo;
    private final BigDecimal monto; // siempre positivo; el tipo dice si suma o resta
    private final LocalDateTime fecha;
    private final String descripcion;
    private final String referencia; // número de referencia del pago móvil (solo recargas); "" si no aplica

    public Movimiento(String correo, TipoMovimiento tipo, BigDecimal monto, LocalDateTime fecha,
                      String descripcion, String referencia) {
        this.correo = correo;
        this.tipo = tipo;
        this.monto = monto;
        this.fecha = fecha;
        this.descripcion = (descripcion == null) ? "" : descripcion;
        this.referencia = (referencia == null) ? "" : referencia;
    }

    public String getCorreo() {
        return correo;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getReferencia() {
        return referencia;
    }

    public boolean tieneReferencia() {
        return !referencia.isEmpty();
    }
}
