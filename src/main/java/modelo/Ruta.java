package modelo;

import java.time.LocalTime;

/** Ruta con su horario (jornada de servicio). El nombre identifica la ruta. */
public class Ruta {

    private final String nombre;
    private final TipoRuta tipo;
    private final LocalTime inicioJornada;
    private final LocalTime finJornada;

    public Ruta(String nombre, TipoRuta tipo, LocalTime inicioJornada, LocalTime finJornada) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.inicioJornada = inicioJornada;
        this.finJornada = finJornada;
    }

    public String getNombre() { return nombre; }
    public TipoRuta getTipo() { return tipo; }
    public LocalTime getInicioJornada() { return inicioJornada; }
    public LocalTime getFinJornada() { return finJornada; }
}
