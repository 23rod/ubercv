package modelo;

/** Textos exactos de las historias de usuario. La vista solo los muestra; las pruebas los comparan. */
public class Mensajes {
    // HU01 - Registro
    public static final String REGISTRO_EXITOSO = "Registro de usuario exitoso";
    public static final String DATOS_INVALIDOS = "Datos no válidos";
    public static final String USUARIO_EXISTENTE = "Este usuario ya existe";
    public static final String ROL_NO_COINCIDE = "El rol para los datos de usuario ingresados no coincide con la base de datos";
    public static final String CLAVES_NO_COINCIDEN = "Las contraseñas no coinciden";
    public static final String ROL_REQUERIDO = "Seleccione un rol";

    // HU02 - Inicio de sesión
    public static final String LOGIN_EXITOSO = "Inicio de sesión exitoso";
    public static final String LOGIN_INCORRECTO = "Datos de usuario incorrectos, intente de nuevo";

    // Control de acceso por rol (RBAC)
    public static final String ACCESO_DENEGADO = "No tiene permisos para realizar esta acción";
}
