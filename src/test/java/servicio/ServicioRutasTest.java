package servicio;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;
import modelo.EstadoUnidad;
import modelo.Mensajes;
import modelo.Resultado;
import modelo.Ruta;
import modelo.TipoRuta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistencia.memoria.DatosSemilla;
import persistencia.memoria.RepositorioRutas;
import persistencia.memoria.RepositorioUnidades;
import persistencia.memoria.RepositorioUsuarios;

/** HU10: definir y redefinir rutas y horarios. */
class ServicioRutasTest {

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
    void crearUnaRutaValidaLaGuardaConSuHorario() {
        Resultado<Void> r = rutas.crearRuta("UCV - Altamira", TipoRuta.URBANA, "6:30", "20:00");
        assertTrue(r.isOk());
        assertEquals(Mensajes.RUTA_GUARDADA, r.getMensaje());

        Ruta ruta = repoRutas.buscarPorNombre("ucv - altamira"); // no distingue mayúsculas
        assertNotNull(ruta);
        assertEquals(TipoRuta.URBANA, ruta.getTipo());
        assertEquals(LocalTime.of(6, 30), ruta.getInicioJornada());
        assertEquals(LocalTime.of(20, 0), ruta.getFinJornada());
    }

    @Test
    void crearUnaRutaRepetidaMuestraRutaYaExistente() {
        crearRuta("UCV - Altamira");
        Resultado<Void> r = rutas.crearRuta("ucv - altamira", TipoRuta.EXTRAURBANA, "05:00", "18:00");
        assertFalse(r.isOk());
        assertEquals(Mensajes.RUTA_EXISTENTE, r.getMensaje());
        assertEquals(1, rutas.listarRutas().size());
    }

    @Test
    void elHorarioDebeTenerFormatoHoraYElInicioAntesDelFinal() {
        for (String[] h : new String[][] {{"25:00", "20:00"}, {"06:60", "20:00"}, {"abc", "20:00"},
                {"20:00", "06:00"}, {"08:00", "08:00"}}) {
            Resultado<Void> r = rutas.crearRuta("Ruta X", TipoRuta.URBANA, h[0], h[1]);
            assertEquals(Mensajes.HORARIO_INVALIDO, r.getMensaje(), h[0] + " - " + h[1]);
        }
        assertTrue(rutas.listarRutas().isEmpty());
    }

    @Test
    void camposVaciosOSinTipoSonRechazados() {
        assertEquals(Mensajes.CAMPOS_OBLIGATORIOS, rutas.crearRuta("", TipoRuta.URBANA, "06:00", "20:00").getMensaje());
        assertEquals(Mensajes.CAMPOS_OBLIGATORIOS, rutas.crearRuta("Ruta", TipoRuta.URBANA, "", "20:00").getMensaje());
        assertEquals(Mensajes.TIPO_RUTA_REQUERIDO, rutas.crearRuta("Ruta", null, "06:00", "20:00").getMensaje());
    }

    @Test
    void redefinirUnaRutaCambiaSusDatosYMuestraRutaRedefinida() {
        crearRuta("UCV - Altamira");
        Resultado<Void> r = rutas.redefinirRuta("UCV - Altamira", "UCV - Chacao", TipoRuta.EXTRAURBANA, "07:00", "19:00");
        assertTrue(r.isOk());
        assertEquals(Mensajes.RUTA_REDEFINIDA, r.getMensaje());

        assertNull(repoRutas.buscarPorNombre("UCV - Altamira"));
        Ruta nueva = repoRutas.buscarPorNombre("UCV - Chacao");
        assertEquals(TipoRuta.EXTRAURBANA, nueva.getTipo());
        assertEquals(LocalTime.of(7, 0), nueva.getInicioJornada());
    }

    @Test
    void redefinirConElMismoNombreSoloCambiaElHorario() {
        crearRuta("UCV - Altamira");
        Resultado<Void> r = rutas.redefinirRuta("UCV - Altamira", "UCV - Altamira", TipoRuta.URBANA, "08:00", "17:00");
        assertEquals(Mensajes.RUTA_REDEFINIDA, r.getMensaje());
        assertEquals(LocalTime.of(8, 0), repoRutas.buscarPorNombre("UCV - Altamira").getInicioJornada());
    }

    @Test
    void redefinirConElNombreDeOtraRutaEsRechazado() {
        crearRuta("Ruta A");
        crearRuta("Ruta B");
        Resultado<Void> r = rutas.redefinirRuta("Ruta A", "ruta b", TipoRuta.URBANA, "06:00", "20:00");
        assertEquals(Mensajes.RUTA_EXISTENTE, r.getMensaje());
        assertNotNull(repoRutas.buscarPorNombre("Ruta A")); // no se perdió nada
    }

    @Test
    void redefinirUnaRutaInexistenteFalla() {
        Resultado<Void> r = rutas.redefinirRuta("No existe", "Nueva", TipoRuta.URBANA, "06:00", "20:00");
        assertEquals(Mensajes.RUTA_NO_ENCONTRADA, r.getMensaje());
    }

    @Test
    void alRenombrarUnaRutaSusUnidadesLaSiguen() {
        crearRuta("Ruta A");
        unidad("ABC123", 40);
        asignaciones.asignarUnidad("ABC123", "Ruta A", false);
        rutas.redefinirRuta("Ruta A", "Ruta Nueva", TipoRuta.URBANA, "06:00", "20:00");
        assertEquals(1, asignaciones.unidadesDeRuta("Ruta Nueva").size());
        assertEquals(40, asignaciones.cuposDeRuta("Ruta Nueva"));
    }

    @Test
    void soloElAdminPuedeDefinirRutas() {
        usuarios.iniciarSesion("conductor@ucv.com", DatosSemilla.CLAVE_DEMO);
        assertEquals(Mensajes.ACCESO_DENEGADO, crearRuta("Ruta A").getMensaje());
        usuarios.cerrarSesion();
        assertEquals(Mensajes.ACCESO_DENEGADO, crearRuta("Ruta A").getMensaje());
        assertTrue(rutas.listarRutas().isEmpty());
    }

    // =================================================================== HU12

    @Test
    void elNombreDeLaRutaNoPuedePasarDeSesentaCaracteres() {
        String justo = "R".repeat(60);
        String largo = "R".repeat(61);
        assertTrue(rutas.crearRuta(justo, TipoRuta.URBANA, "06:00", "20:00").isOk());
        assertEquals(Mensajes.NOMBRE_RUTA_LARGO, rutas.crearRuta(largo, TipoRuta.URBANA, "06:00", "20:00").getMensaje());
        assertEquals(Mensajes.NOMBRE_RUTA_LARGO, rutas.redefinirRuta(justo, largo, TipoRuta.URBANA, "06:00", "20:00").getMensaje());
    }
}
