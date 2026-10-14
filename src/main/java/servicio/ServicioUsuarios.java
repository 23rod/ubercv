package servicio;

import java.util.ArrayList;
import java.util.List;
import modelo.Mensajes;
import modelo.Resultado;
import modelo.Rol;
import modelo.Usuario;
import persistencia.memoria.DatosSemilla;
import persistencia.interfaces.IRepositorioUsuarios;
import utilidades.seguridad.HashClave;
import utilidades.validadores.Validador;



/**
 * Lógica de HU01 (registro) y HU02 (inicio de sesión) más la sesión actual.
 * No usa Swing: por eso se puede probar con JUnit sin abrir ninguna ventana.
 */
public class ServicioUsuarios implements Autorizador, ConsultaUsuarios, SesionActual {

    private final IRepositorioUsuarios repositorio;
    private Usuario usuarioSesion; // null = nadie ha iniciado sesión    

    public ServicioUsuarios(IRepositorioUsuarios repositorio) {
        this.repositorio = repositorio;
    }   


    // ---------------------------------------------------------------- HU01
    public Resultado<Void> registrar(String correo, String clave, String confirmacion, String cedula, Rol rol) {
        String correoNorm = normalizarCorreo(correo);
        String cedulaNorm = (cedula == null) ? null : cedula.trim();

        // 1) Formato de los datos (HU01 escenario 3)
        // Se indica qué campos fallan, en el orden del formulario (cédula, correo, contraseña)
        List<String> problemas = new ArrayList<>();
        if (!Validador.esCedulaValida(cedulaNorm)) {
            problemas.add(Mensajes.DETALLE_CEDULA);
        }
        if (!Validador.esCorreoValido(correoNorm)) {
            problemas.add(Mensajes.DETALLE_CORREO);
        }
        if (!Validador.esClaveValida(clave)) {
            problemas.add(Mensajes.DETALLE_CLAVE);
        }
        if (!problemas.isEmpty()) {
            return Resultado.error(Mensajes.DATOS_INVALIDOS + ":\n- " + String.join("\n- ", problemas));
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
        // 5) El rol elegido debe coincidir con el padrón (HU01 escenario 5).
        //    El público general es externo a la universidad: solo puede registrarse quien NO está en el padrón.
        Rol rolEnPadron = DatosSemilla.validarEnPadron(cedulaNorm);
        boolean coincide = (rol == Rol.PUBLICO_GENERAL) ? (rolEnPadron == Rol.NINGUNO) : (rolEnPadron == rol);
        if (!coincide) {
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

    @Override
    public Usuario getUsuarioSesion() {
        return usuarioSesion;
    }

    @Override
    public Rol getRolSesion() {
        return (usuarioSesion == null) ? Rol.NINGUNO : usuarioSesion.getRol();
    }

    /** Control de acceso por rol: los servicios de flota y rutas la usarán para exigir ADMIN_TRANSPORTE. */
    @Override
    public Resultado<Void> exigirRol(Rol requerido) {
        if (getRolSesion() != requerido) {
            return Resultado.error(Mensajes.ACCESO_DENEGADO);
        }
        return Resultado.exito(null, "");
    }

    @Override
    public Usuario buscarPorCedula(String cedula) {
        if (cedula == null) {
            return null;
        }
        return repositorio.buscarPorCedula(cedula.trim());
    }

    @Override
    public List<Usuario> listarPorRol(Rol rol) {
        List<Usuario> resultado = new ArrayList<>();

        if (rol == null) {
            return resultado;
        }

        for (Usuario usuario : repositorio.listar()) {
            if (usuario.getRol() == rol) {
                resultado.add(usuario);
            }
        }

        return resultado;
    }

    private static String normalizarCorreo(String correo) {
        return (correo == null) ? null : correo.trim().toLowerCase();
    }
}
