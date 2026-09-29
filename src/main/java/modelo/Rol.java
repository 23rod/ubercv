package modelo;

public enum Rol {
    ESTUDIANTE,
    EMPLEADO,
    CONDUCTOR,
    ADMIN_TRANSPORTE,
    NINGUNO;

    /**
     * Traduce el texto que muestra el combo de la vista ("Admin de Transporte", etc.)
     * al enum. Cualquier texto desconocido (incluido "Ninguno") devuelve NINGUNO.
     */
    public static Rol desdeTexto(String texto) {
        if (texto == null) {
            return NINGUNO;
        }
        switch (texto.trim().toLowerCase()) {
            case "estudiante":
                return ESTUDIANTE;
            case "empleado":
                return EMPLEADO;
            case "conductor":
                return CONDUCTOR;
            case "admin de transporte":
                return ADMIN_TRANSPORTE;
            default:
                return NINGUNO;
        }
    }
}
