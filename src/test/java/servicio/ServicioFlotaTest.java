package servicio;

import static org.junit.jupiter.api.Assertions.*;

import modelo.EstadoUnidad;
import modelo.Mensajes;
import modelo.Resultado;
import modelo.UnidadTransporte;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistencia.memoria.DatosSemilla;
import persistencia.memoria.RepositorioUnidades;
import persistencia.memoria.RepositorioUsuarios;

/** HU09: Gestión de Flota. */
class ServicioFlotaTest {

    private RepositorioUnidades repositorio;
    private ServicioUsuarios usuarios;
    private ServicioFlota flota;

    @BeforeEach
    void preparar() {
        RepositorioUsuarios repoUsuarios = new RepositorioUsuarios();
        DatosSemilla.sembrarUsuarios(repoUsuarios);
        usuarios = new ServicioUsuarios(repoUsuarios);
        repositorio = new RepositorioUnidades();
        flota = new ServicioFlota(repositorio, usuarios);
        usuarios.iniciarSesion("admin@ucv.com", DatosSemilla.CLAVE_DEMO); // el admin es quien gestiona la flota
    }

    // ---------------------------------------------------- Escenario 1: registro exitoso
    @Test
    void registrarUnidadValidaLaGuardaYMuestraElMensajeDeLaHU() {
        Resultado<Void> r = flota.registrarUnidad("abc123", "Yutong ZK6", "40", EstadoUnidad.ACTIVO);
        assertTrue(r.isOk());
        assertEquals(Mensajes.UNIDAD_GUARDADA, r.getMensaje());

        UnidadTransporte guardada = repositorio.buscarPorPlaca("ABC123");
        assertNotNull(guardada);
        assertEquals("ABC123", guardada.getPlaca()); // la placa se guarda en mayúsculas
        assertEquals(40, guardada.getCapacidad());
        assertEquals(EstadoUnidad.ACTIVO, guardada.getEstado());
        assertFalse(guardada.estaAsignada()); // nace "sin relacionar"
    }

    // ---------------------------------------------------- Escenario 2: unidad ya existente
    @Test
    void registrarUnaPlacaRepetidaMuestraUnidadYaExistente() {
        flota.registrarUnidad("ABC123", "Yutong", "40", EstadoUnidad.ACTIVO);
        Resultado<Void> r = flota.registrarUnidad("abc123", "Otro modelo", "30", EstadoUnidad.ACTIVO);
        assertFalse(r.isOk());
        assertEquals(Mensajes.UNIDAD_EXISTENTE, r.getMensaje());
        assertEquals(1, flota.listarUnidades().size());
    }

    // ---------------------------------------------------- Validaciones
    @Test
    void camposVaciosSonRechazados() {
        assertEquals(Mensajes.CAMPOS_OBLIGATORIOS, flota.registrarUnidad("", "Yutong", "40", EstadoUnidad.ACTIVO).getMensaje());
        assertEquals(Mensajes.CAMPOS_OBLIGATORIOS, flota.registrarUnidad("ABC123", "  ", "40", EstadoUnidad.ACTIVO).getMensaje());
        assertEquals(Mensajes.CAMPOS_OBLIGATORIOS, flota.registrarUnidad("ABC123", "Yutong", "", EstadoUnidad.ACTIVO).getMensaje());
        assertTrue(flota.listarUnidades().isEmpty());
    }

    @Test
    void unaPlacaConFormatoInvalidoEsRechazada() {
        assertEquals(Mensajes.PLACA_INVALIDA, flota.registrarUnidad("AB-1", "Yutong", "40", EstadoUnidad.ACTIVO).getMensaje());
        assertEquals(Mensajes.PLACA_INVALIDA, flota.registrarUnidad("ABC 123", "Yutong", "40", EstadoUnidad.ACTIVO).getMensaje());
    }

    @Test
    void laCapacidadDebeSerUnNumeroNaturalMayorQueCero() {
        for (String invalida : new String[] {"0", "-5", "abc", "12.5", "121", "11111"}) {
            Resultado<Void> r = flota.registrarUnidad("ABC123", "Yutong", invalida, EstadoUnidad.ACTIVO);
            assertEquals(Mensajes.CAPACIDAD_INVALIDA, r.getMensaje(), "capacidad: " + invalida);
        }
        assertTrue(flota.listarUnidades().isEmpty());
    }

    @Test
    void sinEstadoSeleccionadoLaUnidadEsRechazada() {
        Resultado<Void> r = flota.registrarUnidad("ABC123", "Yutong", "40", null); // null = opción "Ninguno"
        assertEquals(Mensajes.ESTADO_REQUERIDO, r.getMensaje());
    }

    // ---------------------------------------------------- Control de acceso
    @Test
    void sinSesionNadiePuedeRegistrarUnidades() {
        usuarios.cerrarSesion();
        Resultado<Void> r = flota.registrarUnidad("ABC123", "Yutong", "40", EstadoUnidad.ACTIVO);
        assertEquals(Mensajes.ACCESO_DENEGADO, r.getMensaje());
        assertTrue(flota.listarUnidades().isEmpty());
    }

    @Test
    void unEstudianteNoPuedeRegistrarUnidades() {
        usuarios.iniciarSesion("estudiante@ucv.com", DatosSemilla.CLAVE_DEMO);
        Resultado<Void> r = flota.registrarUnidad("ABC123", "Yutong", "40", EstadoUnidad.ACTIVO);
        assertEquals(Mensajes.ACCESO_DENEGADO, r.getMensaje());
        assertTrue(flota.listarUnidades().isEmpty());
    }

    // ---------------------------------------------------- Estado operativo
    @Test
    void cambiarElEstadoDeUnaUnidadLoActualiza() {
        flota.registrarUnidad("ABC123", "Yutong", "40", EstadoUnidad.ACTIVO);
        Resultado<Void> r = flota.cambiarEstado("ABC123", EstadoUnidad.EN_MANTENIMIENTO);
        assertTrue(r.isOk());
        assertEquals(EstadoUnidad.EN_MANTENIMIENTO, repositorio.buscarPorPlaca("ABC123").getEstado());
    }

    @Test
    void cambiarElEstadoDeUnaUnidadInexistenteFalla() {
        Resultado<Void> r = flota.cambiarEstado("ZZZ999", EstadoUnidad.ACTIVO);
        assertEquals(Mensajes.UNIDAD_NO_ENCONTRADA, r.getMensaje());
    }

    @Test
    void laCapacidadAdmiteDesdeUnoHastaCientoVeinte() {
        assertTrue(flota.registrarUnidad("AAA111", "Minibus", "1", EstadoUnidad.ACTIVO).isOk());
        assertTrue(flota.registrarUnidad("BBB222", "Articulado", "120", EstadoUnidad.ACTIVO).isOk());
        assertEquals(Mensajes.CAPACIDAD_INVALIDA, flota.registrarUnidad("CCC333", "Gigante", "121", EstadoUnidad.ACTIVO).getMensaje());
    }

    @Test
    void elModeloNoPuedePasarDeCuarentaCaracteres() {
        String justo = "M".repeat(40);
        String largo = "M".repeat(41);
        assertTrue(flota.registrarUnidad("AAA111", justo, "40", EstadoUnidad.ACTIVO).isOk());
        Resultado<Void> r = flota.registrarUnidad("BBB222", largo, "40", EstadoUnidad.ACTIVO);
        assertEquals(Mensajes.MODELO_LARGO, r.getMensaje());
        assertNull(repositorio.buscarPorPlaca("BBB222"));
    }
}
