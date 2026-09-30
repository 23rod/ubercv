package modelo;

/** Estado operativo de una unidad de transporte (la etiqueta es el texto que muestra la vista). */
public enum EstadoUnidad {
    ACTIVO("Activo"),
    EN_MANTENIMIENTO("En mantenimiento"),
    FUERA_DE_SERVICIO("Fuera de Servicio");

    private final String etiqueta;

    EstadoUnidad(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** Traduce el texto del combo de la vista. "Ninguno" o cualquier texto desconocido devuelve null. */
    public static EstadoUnidad desdeTexto(String texto) {
        if (texto == null) {
            return null;
        }
        for (EstadoUnidad estado : values()) {
            if (estado.etiqueta.equalsIgnoreCase(texto.trim())) {
                return estado;
            }
        }
        return null;
    }
}
