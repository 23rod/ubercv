package modelo;

public enum Rol {
    ESTUDIANTE,
    EMPLEADO,
    CONDUCTOR,
    ADMIN_TRANSPORTE,
    PUBLICO_GENERAL,
    NINGUNO;

    /** Nombre legible del rol para mostrar en pantalla (coincide con el texto que acepta {@link #desdeTexto}). */
    public String getEtiqueta() {
        switch (this) {
            case ESTUDIANTE:
                return "Estudiante";
            case EMPLEADO:
                return "Empleado";
            case CONDUCTOR:
                return "Conductor";
            case ADMIN_TRANSPORTE:
                return "Admin de Transporte";
            case PUBLICO_GENERAL:
                return "Público General";
            default:
                return "Ninguno";
        }
    }

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
            case "público general":
            case "publico general":
                return PUBLICO_GENERAL;
            default:
                return NINGUNO;
        }
    }
}
