package modelo;

/** Textos exactos de las historias de usuario. La vista solo los muestra; las pruebas los comparan. */
public class Mensajes {
    // HU01 - Registro
    public static final String REGISTRO_EXITOSO = "Registro de usuario exitoso";
    public static final String DATOS_INVALIDOS = "Datos no válidos";
    public static final String DETALLE_CEDULA = "La cédula debe tener de 5 a 10 dígitos (sin letras, puntos ni guiones)";
    public static final String DETALLE_CORREO = "El correo debe tener el formato usuario@dominio.com";
    public static final String DETALLE_CLAVE = "La contraseña debe tener al menos 8 caracteres";
    public static final String USUARIO_EXISTENTE = "Este usuario ya existe";
    public static final String ROL_NO_COINCIDE = "El rol para los datos de usuario ingresados no coincide con la base de datos";
    public static final String CLAVES_NO_COINCIDEN = "Las contraseñas no coinciden";
    public static final String ROL_REQUERIDO = "Seleccione un rol";

    // HU02 - Inicio de sesión
    public static final String LOGIN_EXITOSO = "Inicio de sesión exitoso";
    public static final String LOGIN_INCORRECTO = "Datos de usuario incorrectos, intente de nuevo";

    // Control de acceso por rol (RBAC)
    public static final String ACCESO_DENEGADO = "No tiene permisos para realizar esta acción";

    // Comunes a flota y rutas
    public static final String CAMPOS_OBLIGATORIOS = "Complete todos los campos";

    // HU09 - Registro de unidades
    public static final String UNIDAD_GUARDADA = "Unidad guardada correctamente";
    public static final String UNIDAD_EXISTENTE = "Unidad ya existente";
    public static final String PLACA_INVALIDA = "Placa no válida (use de 5 a 8 letras o números)";
    public static final String CAPACIDAD_INVALIDA = "La capacidad debe ser un número entero entre 10 y 120";
    public static final String MODELO_LARGO = "El modelo no puede tener más de 40 caracteres";
    public static final String ESTADO_REQUERIDO = "Seleccione un estado";
    public static final String UNIDAD_NO_ENCONTRADA = "Unidad no encontrada";
    public static final String ESTADO_ACTUALIZADO = "Estado de la unidad actualizado";
    public static final String ESTADO_ACTUALIZADO_LIBERADA = "Estado de la unidad actualizado. La unidad se liberó de su ruta (solo las unidades activas prestan servicio)";

    // HU10 - Definir rutas y horarios
    public static final String RUTA_GUARDADA = "Ruta guardada correctamente";
    public static final String RUTA_EXISTENTE = "Ruta ya existente";
    public static final String RUTA_REDEFINIDA = "Ruta redefinida";
    public static final String RUTA_NO_ENCONTRADA = "Ruta no encontrada";
    public static final String NOMBRE_RUTA_LARGO = "El nombre de la ruta no puede tener más de 60 caracteres";
    public static final String TIPO_RUTA_REQUERIDO = "Seleccione un tipo de ruta";
    public static final String HORARIO_INVALIDO = "Horario no válido: use el formato HH:mm y que el inicio sea anterior al final";

    // HU12 - Asignar unidad a ruta
    public static final String UNIDAD_ASIGNADA = "Unidad asignada correctamente";
    public static final String UNIDAD_REASIGNADA = "Unidad reasignada correctamente";
    public static final String CONFIRMAR_CAMBIO = "¿Está seguro del cambio?";
    public static final String UNIDAD_YA_ASIGNADA = "En esta ruta ya se encuentra asignada la unidad";
    public static final String UNIDAD_NO_ACTIVA = "Solo se pueden asignar unidades en estado Activo";

    // Asignación de conductores a unidades/rutas
    public static final String CONDUCTOR_ASIGNADO = "Conductor asignado correctamente";
    public static final String CONDUCTOR_REASIGNADO = "Conductor reasignado correctamente";
    public static final String CONDUCTOR_YA_ASIGNADO = "En esta unidad ya se encuentra asignado el conductor";
    public static final String CONDUCTOR_OCUPADO = "El conductor ya se encuentra asignado a otra unidad";
    public static final String CONDUCTOR_NO_ENCONTRADO = "Conductor no encontrado";
    public static final String USUARIO_NO_ES_CONDUCTOR = "El usuario seleccionado no tiene rol de Conductor";
    public static final String UNIDAD_SIN_RUTA = "La unidad debe estar asignada a una ruta antes de asignarle un conductor";
    public static final String UNIDAD_NO_PERTENECE_RUTA = "La unidad seleccionada no pertenece a la ruta elegida";
}
