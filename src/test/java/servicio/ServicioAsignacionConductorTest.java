package servicio;

import static org.junit.jupiter.api.Assertions.*;

import modelo.EstadoUnidad;
import modelo.Mensajes;
import modelo.Resultado;
import modelo.Rol;
import modelo.UnidadTransporte;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistencia.memoria.DatosSemilla;
import persistencia.memoria.RepositorioRutas;
import persistencia.memoria.RepositorioUnidades;
import persistencia.memoria.RepositorioUsuarios;

class ServicioAsignacionConductorTest {

    private RepositorioUnidades repoUnidades;
    private RepositorioRutas repoRutas;
    private RepositorioUsuarios repoUsuarios;
    private ServicioUsuarios usuarios;
    private ServicioItinerarios itinerarios;

    @BeforeEach
    void preparar() {
        repoUsuarios = new RepositorioUsuarios();
        DatosSemilla.sembrarUsuarios(repoUsuarios);

        repoUnidades = new RepositorioUnidades();
        repoRutas = new RepositorioRutas();
        DatosSemilla.sembrarFlota(repoUnidades, repoRutas);

        usuarios = new ServicioUsuarios(repoUsuarios);
        itinerarios = new ServicioItinerarios(repoRutas, repoUnidades, usuarios);
        usuarios.iniciarSesion("admin@ucv.com", DatosSemilla.CLAVE_DEMO);
    }

    @Test
    void asignarConductorAUnidadDeUnaRuta() {
        UnidadTransporte unidad = new UnidadTransporte(
                "JKL789", "Modelo X", 40, EstadoUnidad.ACTIVO);
        unidad.setNombreRuta("UCV - Altamira");
        repoUnidades.guardar(unidad);
        Resultado<Void> r = itinerarios.asignarConductor(
                "20000004", "JKL789", "UCV - Altamira");
        assertTrue(r.isOk());
        assertEquals(Mensajes.CONDUCTOR_ASIGNADO, r.getMensaje());
        assertEquals("20000004", unidad.getCedulaConductor());
    }

    @Test
    void conductorYaAsignadoAOtraUnidadEsRechazado() {
        UnidadTransporte unidad = new UnidadTransporte(
                "JKL789", "Modelo X", 40, EstadoUnidad.ACTIVO);
        unidad.setNombreRuta("UCV - Altamira");
        repoUnidades.guardar(unidad);
        // 20000003 ya está asignado a ABC123 en DatosSemilla
        Resultado<Void> r = itinerarios.asignarConductor(
                "20000003", "JKL789", "UCV - Altamira");
        assertFalse(r.isOk());
        assertEquals(Mensajes.CONDUCTOR_OCUPADO, r.getMensaje());
    }

    @Test
    void asignarUnUsuarioQueNoEsConductorEsRechazado() {
        Resultado<Void> r = itinerarios.asignarConductor(
                "20000001", "ABC123", "UCV - Altamira");

        assertFalse(r.isOk());
        assertEquals(Mensajes.USUARIO_NO_ES_CONDUCTOR, r.getMensaje());
    }

    @Test
    void conductorInexistenteEsRechazado() {
        Resultado<Void> r = itinerarios.asignarConductor(
                "99999999", "ABC123", "UCV - Altamira");

        assertFalse(r.isOk());
        assertEquals(Mensajes.CONDUCTOR_NO_ENCONTRADO, r.getMensaje());
    }

    @Test
    void unidadInexistenteEsRechazada() {
        Resultado<Void> r = itinerarios.asignarConductor(
                "20000003", "ZZZ999", "UCV - Altamira");

        assertFalse(r.isOk());
        assertEquals(Mensajes.UNIDAD_NO_ENCONTRADA, r.getMensaje());
    }

    @Test
    void rutaInexistenteEsRechazada() {
        Resultado<Void> r = itinerarios.asignarConductor(
                "20000003", "ABC123", "Ruta inexistente");

        assertFalse(r.isOk());
        assertEquals(Mensajes.RUTA_NO_ENCONTRADA, r.getMensaje());
    }

    @Test
    void unidadSinRutaNoPuedeRecibirConductor() {
        UnidadTransporte unidad = new UnidadTransporte(
                "JKL789", "Modelo X", 40, EstadoUnidad.ACTIVO);
        repoUnidades.guardar(unidad);

        Resultado<Void> r = itinerarios.asignarConductor(
                "20000003", "JKL789", "UCV - Altamira");

        assertFalse(r.isOk());
        assertEquals(Mensajes.UNIDAD_SIN_RUTA, r.getMensaje());
    }

    @Test
    void unidadDeOtraRutaNoPuedeRecibirConductor() {
        Resultado<Void> r = itinerarios.asignarConductor(
                "20000003", "ABC123", "UCV - Guarenas");

        assertFalse(r.isOk());
        assertEquals(Mensajes.UNIDAD_NO_PERTENECE_RUTA, r.getMensaje());
    }

    @Test
    void conductorSoloPuedeSerAsignadoPorAdmin() {
        usuarios.iniciarSesion("empleado@ucv.com", DatosSemilla.CLAVE_DEMO);

        Resultado<Void> r = itinerarios.asignarConductor(
                "20000003", "ABC123", "UCV - Altamira");

        assertFalse(r.isOk());
        assertEquals(Mensajes.ACCESO_DENEGADO, r.getMensaje());
    }

    @Test
    void reasignarConductorCambiaLaCedulaAsociada() {
        // 10000004 aparece en el padrón como CONDUCTOR, pero todavía no existe como usuario.
        repoUsuarios.guardar(new modelo.Usuario(
                "otro@ucv.com", "hash", new byte[]{1}, "10000004", Rol.CONDUCTOR));

        Resultado<Void> r = itinerarios.asignarConductor(
                "10000004", "ABC123", "UCV - Altamira");

        assertTrue(r.isOk());
        assertEquals(Mensajes.CONDUCTOR_REASIGNADO, r.getMensaje());
        assertEquals("10000004", repoUnidades.buscarPorPlaca("ABC123").getCedulaConductor());
    }

    @Test
    void listarUnidadesDeUnConductorDevuelveSoloLasSuyas() {
        // ABC123 ya viene asignada a 20000003 en DatosSemilla
        assertEquals(1, itinerarios.unidadesDeConductor("20000003").size());
        assertEquals("ABC123", itinerarios.unidadesDeConductor("20000003").get(0).getPlaca());
        assertTrue(itinerarios.unidadesDeConductor("10000004").isEmpty());
    }
}
