package servicio;

import modelo.Mensajes;
import modelo.Resultado;
import modelo.Rol;
import modelo.Usuario;
import persistencia.memoria.DatosSemilla;
import persistencia.memoria.RepositorioUsuarios;
import utilidades.seguridad.HashClave;
import utilidades.validadores.Validador;

/**
 * Lógica de HU01 (registro) y HU02 (inicio de sesión) más la sesión actual.
 * No usa Swing: por eso se puede probar con JUnit sin abrir ninguna ventana.
 */
public class ServicioUsuarios {

    private final RepositorioUsuarios repositorio;
    private Usuario usuarioSesion; // null = nadie ha iniciado sesión

    public ServicioUsuarios(RepositorioUsuarios repositorio) {
        this.repositorio = repositorio;
    }

    // ---------------------------------------------------------------- HU01
    public Resultado<Void> registrar(String correo, String clave, String confirmacion, String cedula, Rol rol) {
        String correoNorm = normalizarCorreo(correo);
        String cedulaNorm = (cedula == null) ? null : cedula.trim();

        // 1) Formato de los datos (HU01 escenario 3)
        if (!Validador.esCorreoValido(correoNorm)
                || !Validador.esCedulaValida(cedulaNorm)
                || !Validador.esClaveValida(clave)) {
            return Resultado.error(Mensajes.DATOS_INVALIDOS);
        }
        // 2) El combo tiene la opción "Ninguno"
        if (rol == null || rol == Rol.NINGUNO) {
            return Resultado.error(Mensajes.ROL_REQUERIDO);
        }
        // 3) Campo "confirmar contraseña" de la vista
        if (!clave.equals(confirmacion)) {
            return Resultado.error(Mensajes.CLAVES_NO_COINCIDEN);
        }
        // 4) Duplicados (HU01 escenario 4)
        if (repositorio.existeCorreo(correoNorm) || repositorio.existeCedula(cedulaNorm)) {
            return Resultado.error(Mensajes.USUARIO_EXISTENTE);
        }
        // 5) El rol elegido debe coincidir con el padrón (HU01 escenario 5)
        if (DatosSemilla.validarEnPadron(cedulaNorm) != rol) {
            return Resultado.error(Mensajes.ROL_NO_COINCIDE);
        }

        byte[] sal = HashClave.generarSal();
        String hash = HashClave.hashearClave(clave, sal);
        repositorio.guardar(new Usuario(correoNorm, hash, sal, cedulaNorm, rol));
        return Resultado.exito(null, Mensajes.REGISTRO_EXITOSO);
    }

    // ---------------------------------------------------------------- HU02
    public Resultado<Rol> iniciarSesion(String correo, String clave) {
        Usuario usuario = repositorio.buscarPorCorreo(normalizarCorreo(correo));
        // Mismo mensaje si el correo no existe o la clave falla: no revelamos cuál de los dos fue
        if (usuario == null || !HashClave.coincide(clave, usuario.getSal(), usuario.getHashClave())) {
            return Resultado.error(Mensajes.LOGIN_INCORRECTO);
        }
        usuarioSesion = usuario;
        return Resultado.exito(usuario.getRol(), Mensajes.LOGIN_EXITOSO);
    }

    // -------------------------------------------------------------- Sesión
    public void cerrarSesion() {
        usuarioSesion = null;
    }

    public boolean haySesion() {
        return usuarioSesion != null;
    }

    public Usuario getUsuarioSesion() {
        return usuarioSesion;
    }

    public Rol getRolSesion() {
        return (usuarioSesion == null) ? Rol.NINGUNO : usuarioSesion.getRol();
    }

    /** Control de acceso por rol: los servicios de flota y rutas la usarán para exigir ADMIN_TRANSPORTE. */
    public Resultado<Void> exigirRol(Rol requerido) {
        if (getRolSesion() != requerido) {
            return Resultado.error(Mensajes.ACCESO_DENEGADO);
        }
        return Resultado.exito(null, "");
    }

    private static String normalizarCorreo(String correo) {
        return (correo == null) ? null : correo.trim().toLowerCase();
    }
}
