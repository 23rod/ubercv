package modelo;

public class Resultado<T> {
    private final boolean ok;
    private final String mensaje;
    private final T valor;

    private Resultado(boolean ok, String mensaje, T valor) {
        this.ok = ok;
        this.mensaje = mensaje;
        this.valor = valor;
    }

    public static <T> Resultado<T> exito(T valor, String mensaje) {
        return new Resultado<>(true, mensaje, valor);
    }

    public static <T> Resultado<T> error(String mensaje) {
        return new Resultado<>(false, mensaje, null);
    }

    public boolean isOk() { return ok; }
    public String getMensaje() { return mensaje; }
    public T getValor() { return valor; }
}