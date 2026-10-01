package servicio;

import static org.junit.jupiter.api.Assertions.*;

import modelo.Mensajes;
import modelo.Resultado;
import modelo.Rol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistencia.memoria.RepositorioUsuarios;

class ServicioUsuariosTest {

    private RepositorioUsuarios repositorio;
    private ServicioUsuarios servicio;

    @BeforeEach
    void preparar() {
        repositorio = new RepositorioUsuarios(); // repositorio vacío para cada prueba
        servicio = new ServicioUsuarios(repositorio);
    }

    private Resultado<Void> registrarAna() {
        return servicio.registrar("ana@ucv.com", "Clave1234", "Clave1234", "10000001", Rol.ESTUDIANTE);
    }

    // ------------------------------------------------------------ HU01: registro
    @Test
    void registroExitoso() {
        Resultado<Void> r = registrarAna();
        assertTrue(r.isOk());
        assertEquals(Mensajes.REGISTRO_EXITOSO, r.getMensaje());
        assertTrue(repositorio.existeCorreo("ana@ucv.com"));
        assertTrue(repositorio.existeCedula("10000001"));
    }

    @Test
    void registroNormalizaCorreoConMayusculasYEspacios() {
        Resultado<Void> r = servicio.registrar("  ANA@UCV.COM ", "Clave1234", "Clave1234", "10000001", Rol.ESTUDIANTE);
        assertTrue(r.isOk());
        assertTrue(repositorio.existeCorreo("ana@ucv.com"));
    }

    @Test
    void registroConCorreoInvalidoEsRechazado() {
        Resultado<Void> r = servicio.registrar("ana@ucv", "Clave1234", "Clave1234", "10000001", Rol.ESTUDIANTE);
        assertFalse(r.isOk());
        assertTrue(r.getMensaje().startsWith(Mensajes.DATOS_INVALIDOS));
        assertTrue(r.getMensaje().contains(Mensajes.DETALLE_CORREO));
    }

    @Test
    void registroConCedulaInvalidaEsRechazado() {
        Resultado<Void> corta = servicio.registrar("ana@ucv.com", "Clave1234", "Clave1234", "1234", Rol.ESTUDIANTE);
        Resultado<Void> letras = servicio.registrar("ana@ucv.com", "Clave1234", "Clave1234", "1000000A", Rol.ESTUDIANTE);
        assertTrue(corta.getMensaje().startsWith(Mensajes.DATOS_INVALIDOS));
        assertTrue(corta.getMensaje().contains(Mensajes.DETALLE_CEDULA));
        assertTrue(letras.getMensaje().startsWith(Mensajes.DATOS_INVALIDOS));
        assertTrue(letras.getMensaje().contains(Mensajes.DETALLE_CEDULA));
    }

    @Test
    void registroConClaveCortaEsRechazado() {
        Resultado<Void> r = servicio.registrar("ana@ucv.com", "corta", "corta", "10000001", Rol.ESTUDIANTE);
        assertFalse(r.isOk());
        assertTrue(r.getMensaje().startsWith(Mensajes.DATOS_INVALIDOS));
        assertTrue(r.getMensaje().contains(Mensajes.DETALLE_CLAVE));
    }

    @Test
    void registroConClavesDistintasEsRechazado() {
        Resultado<Void> r = servicio.registrar("ana@ucv.com", "Clave1234", "Otra12345", "10000001", Rol.ESTUDIANTE);
        assertFalse(r.isOk());
        assertEquals(Mensajes.CLAVES_NO_COINCIDEN, r.getMensaje());
    }

    @Test
    void registroConRolNingunoEsRechazado() {
        Resultado<Void> r = servicio.registrar("ana@ucv.com", "Clave1234", "Clave1234", "10000001", Rol.NINGUNO);
        assertFalse(r.isOk());
        assertEquals(Mensajes.ROL_REQUERIDO, r.getMensaje());
    }

    @Test
    void registroConCorreoDuplicadoEsRechazado() {
        registrarAna();
        Resultado<Void> r = servicio.registrar("ana@ucv.com", "Clave1234", "Clave1234", "10000002", Rol.ESTUDIANTE);
        assertFalse(r.isOk());
        assertEquals(Mensajes.USUARIO_EXISTENTE, r.getMensaje());
    }

    @Test
    void registroConCedulaDuplicadaEsRechazado() {
        registrarAna();
        Resultado<Void> r = servicio.registrar("otra@ucv.com", "Clave1234", "Clave1234", "10000001", Rol.ESTUDIANTE);
        assertFalse(r.isOk());
        assertEquals(Mensajes.USUARIO_EXISTENTE, r.getMensaje());
    }

    @Test
    void registroConRolDistintoAlDelPadronEsRechazado() {
        // La cédula 10000001 es de un ESTUDIANTE en el padrón
        Resultado<Void> r = servicio.registrar("ana@ucv.com", "Clave1234", "Clave1234", "10000001", Rol.ADMIN_TRANSPORTE);
        assertFalse(r.isOk());
        assertEquals(Mensajes.ROL_NO_COINCIDE, r.getMensaje());
    }

    @Test
    void registroConCedulaFueraDelPadronEsRechazado() {
        Resultado<Void> r = servicio.registrar("ana@ucv.com", "Clave1234", "Clave1234", "99999999", Rol.ESTUDIANTE);
        assertFalse(r.isOk());
        assertEquals(Mensajes.ROL_NO_COINCIDE, r.getMensaje());
    }

    @Test
    void laClaveNoSeGuardaEnTextoPlano() {
        registrarAna();
        String guardado = repositorio.buscarPorCorreo("ana@ucv.com").getHashClave();
        assertNotEquals("Clave1234", guardado);
        assertFalse(guardado.contains("Clave1234"));
    }

    @Test
    void mismaClaveEnDosUsuariosGeneraHashesDistintos() {
        registrarAna();
        servicio.registrar("luis@ucv.com", "Clave1234", "Clave1234", "10000002", Rol.ESTUDIANTE);
        String hashAna = repositorio.buscarPorCorreo("ana@ucv.com").getHashClave();
        String hashLuis = repositorio.buscarPorCorreo("luis@ucv.com").getHashClave();
        assertNotEquals(hashAna, hashLuis); // por la sal aleatoria
    }

    // ------------------------------------------------------------ HU02: inicio de sesión
    @Test
    void loginCorrectoDevuelveElRolYAbreSesion() {
        registrarAna();
        Resultado<Rol> r = servicio.iniciarSesion("ana@ucv.com", "Clave1234");
        assertTrue(r.isOk());
        assertEquals(Rol.ESTUDIANTE, r.getValor());
        assertTrue(servicio.haySesion());
        assertEquals(Rol.ESTUDIANTE, servicio.getRolSesion());
    }

    @Test
    void loginIgnoraMayusculasEnElCorreo() {
        registrarAna();
        assertTrue(servicio.iniciarSesion("ANA@ucv.com", "Clave1234").isOk());
    }

    @Test
    void loginConClaveIncorrectaFalla() {
        registrarAna();
        Resultado<Rol> r = servicio.iniciarSesion("ana@ucv.com", "Equivocada1");
        assertFalse(r.isOk());
        assertEquals(Mensajes.LOGIN_INCORRECTO, r.getMensaje());
        assertFalse(servicio.haySesion());
    }

    @Test
    void loginConCorreoInexistenteFallaConElMismoMensaje() {
        Resultado<Rol> r = servicio.iniciarSesion("nadie@ucv.com", "Clave1234");
        assertFalse(r.isOk());
        assertEquals(Mensajes.LOGIN_INCORRECTO, r.getMensaje());
    }

    @Test
    void cerrarSesionDejaSinRol() {
        registrarAna();
        servicio.iniciarSesion("ana@ucv.com", "Clave1234");
        servicio.cerrarSesion();
        assertFalse(servicio.haySesion());
        assertEquals(Rol.NINGUNO, servicio.getRolSesion());
    }

    // ------------------------------------------------------------ Control de acceso por rol
    @Test
    void exigirRolSinSesionEsDenegado() {
        Resultado<Void> r = servicio.exigirRol(Rol.ADMIN_TRANSPORTE);
        assertFalse(r.isOk());
        assertEquals(Mensajes.ACCESO_DENEGADO, r.getMensaje());
    }

    @Test
    void exigirRolDistintoEsDenegadoYElCorrectoEsPermitido() {
        registrarAna();
        servicio.iniciarSesion("ana@ucv.com", "Clave1234");
        assertFalse(servicio.exigirRol(Rol.ADMIN_TRANSPORTE).isOk());
        assertTrue(servicio.exigirRol(Rol.ESTUDIANTE).isOk());
    }

    @Test
    void elMensajeDeDatosInvalidosListaTodosLosCamposQueFallan() {
        // Cédula con prefijo "V-", correo que no termina en .com y clave corta
        Resultado<Void> r = servicio.registrar("juan@ucv.edu.ve", "123", "123", "V-12345678", Rol.ESTUDIANTE);
        assertTrue(r.getMensaje().startsWith(Mensajes.DATOS_INVALIDOS));
        assertTrue(r.getMensaje().contains(Mensajes.DETALLE_CEDULA));
        assertTrue(r.getMensaje().contains(Mensajes.DETALLE_CORREO));
        assertTrue(r.getMensaje().contains(Mensajes.DETALLE_CLAVE));
    }

    @Test
    void unaCedulaDeCincoDigitosTieneFormatoValido() {
        // Formato válido (5 dígitos) pero fuera del padrón: falla por el rol, no por "Datos no válidos"
        Resultado<Void> r = servicio.registrar("ana@ucv.com", "Clave1234", "Clave1234", "12345", Rol.ESTUDIANTE);
        assertEquals(Mensajes.ROL_NO_COINCIDE, r.getMensaje());
    }

    @Test
    void elMensajeSoloMencionaLosCamposInvalidos() {
        Resultado<Void> r = servicio.registrar("ana@ucv", "Clave1234", "Clave1234", "10000001", Rol.ESTUDIANTE);
        assertTrue(r.getMensaje().contains(Mensajes.DETALLE_CORREO));
        assertFalse(r.getMensaje().contains(Mensajes.DETALLE_CEDULA));
        assertFalse(r.getMensaje().contains(Mensajes.DETALLE_CLAVE));
    }
}
