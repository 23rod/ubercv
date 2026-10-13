package modelo;

/** Clases de operación que se anotan en el historial del monedero virtual. */
public enum TipoMovimiento {
    RECARGA("Recarga"),
    COBRO("Cobro"),
    REINTEGRO("Reintegro");

    private final String etiqueta;

    TipoMovimiento(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** Suma al saldo (recarga y reintegro) o resta (cobro). */
    public boolean aumentaElSaldo() {
        return this != COBRO;
    }
}
