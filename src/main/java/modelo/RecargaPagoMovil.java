package modelo;

import java.time.LocalDateTime;

/**
 * Una operación de pago móvil hecha por un usuario para recargar su monedero, exitosa o fallida.
 * Es inmutable. Se guarda para que el administrador pueda consultar las operaciones de cada usuario.
 */
public final class RecargaPagoMovil {

    private final String correo;
    private final LocalDateTime fecha;
    private final String bancoOrigen;
    private final String bancoDestino;
    private final String cedula;
    private final String telefono;
    private final String referencia;
    private final String monto; // tal como lo escribió el usuario (puede ser inválido en una operación fallida)
    private final boolean exitosa;
    private final String detalle; // "Recarga exitosa" o "Recarga fallida, datos inválidos" (más el motivo si lo hay)

    public RecargaPagoMovil(String correo, LocalDateTime fecha, String bancoOrigen, String bancoDestino, String cedula,
                            String telefono, String referencia, String monto, boolean exitosa, String detalle) {
        this.correo = correo;
        this.fecha = fecha;
        this.bancoOrigen = bancoOrigen;
        this.bancoDestino = bancoDestino;
        this.cedula = cedula;
        this.telefono = telefono;
        this.referencia = referencia;
        this.monto = monto;
        this.exitosa = exitosa;
        this.detalle = detalle;
    }

    public String getCorreo() {
        return correo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getBancoOrigen() {
        return bancoOrigen;
    }

    public String getBancoDestino() {
        return bancoDestino;
    }

    public String getCedula() {
        return cedula;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getReferencia() {
        return referencia;
    }

    public String getMonto() {
        return monto;
    }

    public boolean isExitosa() {
        return exitosa;
    }

    public String getDetalle() {
        return detalle;
    }
}
