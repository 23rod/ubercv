package modelo;

public enum TipoRuta {
    URBANA("Urbana"),
    EXTRAURBANA("Extraurbana");

    private final String etiqueta;

    TipoRuta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** Traduce el texto del combo de la vista. "Ninguno" o cualquier texto desconocido devuelve null. */
    public static TipoRuta desdeTexto(String texto) {
        if (texto == null) {
            return null;
        }
        for (TipoRuta tipo : values()) {
            if (tipo.etiqueta.equalsIgnoreCase(texto.trim())) {
                return tipo;
            }
        }
        return null;
    }
}
