package modelo;

/** Vehículo de la flota. nombreRuta == null significa "unidad sin relacionar" (HU12). */
public class UnidadTransporte {

    private final String placa;
    private final String modelo;
    private final int capacidad;
    private EstadoUnidad estado;
    private String nombreRuta;

    public UnidadTransporte(String placa, String modelo, int capacidad, EstadoUnidad estado) {
        this.placa = placa;
        this.modelo = modelo;
        this.capacidad = capacidad;
        this.estado = estado;
    }

    public String getPlaca() { return placa; }
    public String getModelo() { return modelo; }
    public int getCapacidad() { return capacidad; }
    public EstadoUnidad getEstado() { return estado; }
    public String getNombreRuta() { return nombreRuta; }

    public void setEstado(EstadoUnidad estado) { this.estado = estado; }
    public void setNombreRuta(String nombreRuta) { this.nombreRuta = nombreRuta; }

    public boolean estaAsignada() {
        return nombreRuta != null;
    }
}
