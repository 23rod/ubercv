package servicio;

import static org.junit.jupiter.api.Assertions.*;

///import java.time.LocalTime;
import modelo.EstadoUnidad;
import modelo.Mensajes;
import modelo.Resultado;
//import modelo.Ruta;
import modelo.TipoRuta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistencia.memoria.DatosSemilla;
import persistencia.memoria.RepositorioRutas;
import persistencia.memoria.RepositorioUnidades;
import persistencia.memoria.RepositorioUsuarios;

/** HU12: asignar unidades a rutas (y cupos de cada ruta). */
class ServicioAsignacionesTest {

    private RepositorioRutas repoRutas;
    private ServicioUsuarios usuarios;
    private ServicioFlota flota;
    private ServicioRutas rutas;
    private ServicioAsignaciones asignaciones;

    @BeforeEach
    void preparar() {
        RepositorioUsuarios repoUsuarios = new RepositorioUsuarios();
        DatosSemilla.sembrarUsuarios(repoUsuarios);
        usuarios = new ServicioUsuarios(repoUsuarios);
        RepositorioUnidades repoUnidades = new RepositorioUnidades();
        repoRutas = new RepositorioRutas();
        flota = new ServicioFlota(repoUnidades, usuarios);
        rutas = new ServicioRutas(repoRutas, repoUnidades, usuarios);
        asignaciones = new ServicioAsignaciones(repoRutas, repoUnidades, usuarios, usuarios);
        usuarios.iniciarSesion("admin@ucv.com", DatosSemilla.CLAVE_DEMO);
    }

    private Resultado<Void> crearRuta(String nombre) {
        return rutas.crearRuta(nombre, TipoRuta.URBANA, "06:00", "20:00");
    }

    private void unidad(String placa, int capacidad) {
        flota.registrarUnidad(placa, "Modelo X", String.valueOf(capacidad), EstadoUnidad.ACTIVO);
    }

    // =================================================================== HU10
    @Test
    void asignarUnaUnidadLibreLaRelacionaConLaRutaYSumaCupos() {
        crearRuta("Ruta A");
        unidad("ABC123", 40);
        Resultado<Void> r = asignaciones.asignarUnidad("abc123", "ruta a", false);
        assertTrue(r.isOk());
        assertEquals(Mensajes.UNIDAD_ASIGNADA, r.getMensaje());
        assertEquals(1, asignaciones.unidadesDeRuta("Ruta A").size());
        assertEquals(40, asignaciones.cuposDeRuta("Ruta A"));
    }

    @Test
    void loscuposDeUnaRutaSonLaSumaDeLasCapacidadesDeSusUnidades() {
        crearRuta("Ruta A");
        unidad("ABC123", 40);
        unidad("DEF456", 35);
        asignaciones.asignarUnidad("ABC123", "Ruta A", false);
        asignaciones.asignarUnidad("DEF456", "Ruta A", false);
        assertEquals(75, asignaciones.cuposDeRuta("Ruta A"));
    }

    @Test
    void asignarUnaUnidadALaRutaDondeYaEstaMuestraElMensajeDeLaHU() {
        crearRuta("Ruta A");
        unidad("ABC123", 40);
        asignaciones.asignarUnidad("ABC123", "Ruta A", false);
        Resultado<Void> r = asignaciones.asignarUnidad("ABC123", "Ruta A", true);
        assertFalse(r.isOk());
        assertEquals(Mensajes.UNIDAD_YA_ASIGNADA, r.getMensaje());
        assertEquals(40, asignaciones.cuposDeRuta("Ruta A")); // no se duplica
    }

    @Test
    void moverUnaUnidadDeRutaRequiereConfirmacion() {
        crearRuta("Ruta A");
        crearRuta("Ruta B");
        unidad("ABC123", 40);
        asignaciones.asignarUnidad("ABC123", "Ruta A", false);

        assertTrue(asignaciones.requiereConfirmacion("ABC123", "Ruta B"));
        assertFalse(asignaciones.requiereConfirmacion("ABC123", "Ruta A"));

        Resultado<Void> sinConfirmar = asignaciones.asignarUnidad("ABC123", "Ruta B", false);
        assertEquals(Mensajes.CONFIRMAR_CAMBIO, sinConfirmar.getMensaje()); // "¿Está seguro del cambio?"
        assertEquals(40, asignaciones.cuposDeRuta("Ruta A")); // nada cambió todavía

        Resultado<Void> confirmada = asignaciones.asignarUnidad("ABC123", "Ruta B", true);
        assertTrue(confirmada.isOk());
        assertEquals(Mensajes.UNIDAD_REASIGNADA, confirmada.getMensaje());
        assertEquals(0, asignaciones.cuposDeRuta("Ruta A"));
        assertEquals(40, asignaciones.cuposDeRuta("Ruta B"));
    }

    @Test
    void soloSePuedenAsignarUnidadesActivas() {
        crearRuta("Ruta A");
        flota.registrarUnidad("ABC123", "Modelo X", "40", EstadoUnidad.EN_MANTENIMIENTO);
        Resultado<Void> r = asignaciones.asignarUnidad("ABC123", "Ruta A", false);
        assertEquals(Mensajes.UNIDAD_NO_ACTIVA, r.getMensaje());
        assertEquals(0, asignaciones.cuposDeRuta("Ruta A"));
    }

    @Test
    void siUnaUnidadAsignadaDejaDeEstarActivaSeLiberaDeSuRuta() {
        crearRuta("Ruta A");
        unidad("ABC123", 40);
        asignaciones.asignarUnidad("ABC123", "Ruta A", false);
        Resultado<Void> r = flota.cambiarEstado("ABC123", EstadoUnidad.FUERA_DE_SERVICIO);
        assertEquals(Mensajes.ESTADO_ACTUALIZADO_LIBERADA, r.getMensaje()); // avisa que la unidad se liberó
        assertEquals(0, asignaciones.cuposDeRuta("Ruta A"));
        assertTrue(asignaciones.unidadesDeRuta("Ruta A").isEmpty());
    }

    @Test
    void asignarConUnidadORutaInexistenteFalla() {
        crearRuta("Ruta A");
        unidad("ABC123", 40);
        assertEquals(Mensajes.UNIDAD_NO_ENCONTRADA, asignaciones.asignarUnidad("ZZZ999", "Ruta A", false).getMensaje());
        assertEquals(Mensajes.RUTA_NO_ENCONTRADA, asignaciones.asignarUnidad("ABC123", "No existe", false).getMensaje());
    }

    @Test
    void soloElAdminPuedeAsignarUnidades() {
        crearRuta("Ruta A");
        unidad("ABC123", 40);
        usuarios.iniciarSesion("empleado@ucv.com", DatosSemilla.CLAVE_DEMO);
        assertEquals(Mensajes.ACCESO_DENEGADO, asignaciones.asignarUnidad("ABC123", "Ruta A", false).getMensaje());
        assertEquals(0, asignaciones.cuposDeRuta("Ruta A"));
    }
}
