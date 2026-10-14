package modelo;

/** Mensajes del monedero virtual (HU04 a HU07b). */
public final class MensajesMonedero {

    private MensajesMonedero() {
    }

    public static final String SALDO_INSUFICIENTE = "Saldo insuficiente, por favor recargue";
    public static final String MONTO_INVALIDO = "El monto debe ser mayor que cero";
    public static final String CUENTA_INVALIDA = "Cuenta de usuario no válida";
    public static final String REFERENCIA_REPETIDA = "Esa referencia de pago ya fue utilizada en otra recarga";

    // HU04 / HU05 - Recarga por pago móvil
    public static final String SOLICITUD_ACEPTADA = "Solicitud aceptada";
    public static final String RECARGA_EXITOSA = "Recarga exitosa";
    public static final String RECARGA_FALLIDA = "Recarga fallida, datos inválidos";
    /** Primera línea del mensaje cuando hay datos incorrectos; debajo va una línea por cada campo. */
    public static final String DATOS_PAGO_INCORRECTOS = "Datos de pago móvil incorrectos, intente de nuevo";
    public static final String BANCO_ORIGEN_INVALIDO = "- Banco origen: seleccione un banco de la lista";
    public static final String BANCO_DESTINO_INVALIDO = "- Banco destino: seleccione un banco de la lista";
    public static final String CEDULA_PAGO_INVALIDA = "- Cédula: debe tener entre 5 y 10 dígitos, sin letras ni puntos";
    public static final String TELEFONO_INVALIDO = "- Teléfono: debe tener 11 dígitos, sin guiones ni espacios";
    public static final String REFERENCIA_INVALIDA = "- Referencia: debe tener entre 6 y 12 dígitos";
    public static final String MONTO_PAGO_INVALIDO = "- Monto: debe ser mayor que cero, con hasta dos decimales";
}
