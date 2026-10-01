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

/** HU10 (rutas y horarios) y HU12 (asignar unidades a rutas) = Control de Itinerarios. */
class ServicioItinerariosTest {

    private RepositorioRutas repoRutas;
    private ServicioUsuarios usuarios;
    private ServicioFlota flota;
    private ServicioItinerarios itinerarios;

    @BeforeEach
    void preparar() {
        RepositorioUsuarios repoUsuarios = new RepositorioUsuarios();
        DatosSemilla.sembrarUsuarios(repoUsuarios);
        usuarios = new ServicioUsuarios(repoUsuarios);
        RepositorioUnidades repoUnidades = new RepositorioUnidades();
        repoRutas = new RepositorioRutas();
        flota = new ServicioFlota(repoUnidades, usuarios);
        itinerarios = new ServicioItinerarios(repoRutas, repoUnidades, usuarios);
        usuarios.iniciarSesion("admin@ucv.com", DatosSemilla.CLAVE_DEMO);
    }

    private Resultado<Void> crearRuta(String nombre) {
        return itinerarios.crearRuta(nombre, TipoRuta.URBANA, "06:00", "20:00");
    }

    private void unidad(String placa, int capacidad) {
        flota.registrarUnidad(placa, "Modelo X", String.valueOf(capacidad), EstadoUnidad.ACTIVO);
    }

    // =================================================================== HU10
    @Test
    void crearUnaRutaValidaLaGuardaConSuHorario() {
        Resultado<Void> r = itinerarios.crearRuta("UCV - Altamira", TipoRuta.URBANA, "6:30", "20:00");
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
        Resultado<Void> r = itinerarios.crearRuta("ucv - altamira", TipoRuta.EXTRAURBANA, "05:00", "18:00");
        assertFalse(r.isOk());
        assertEquals(Mensajes.RUTA_EXISTENTE, r.getMensaje());
        assertEquals(1, itinerarios.listarRutas().size());
    }

    @Test
    void elHorarioDebeTenerFormatoHoraYElInicioAntesDelFinal() {
        for (String[] h : new String[][] {{"25:00", "20:00"}, {"06:60", "20:00"}, {"abc", "20:00"},
                {"20:00", "06:00"}, {"08:00", "08:00"}}) {
            Resultado<Void> r = itinerarios.crearRuta("Ruta X", TipoRuta.URBANA, h[0], h[1]);
            assertEquals(Mensajes.HORARIO_INVALIDO, r.getMensaje(), h[0] + " - " + h[1]);
        }
        assertTrue(itinerarios.listarRutas().isEmpty());
    }

    @Test
    void camposVaciosOSinTipoSonRechazados() {
        assertEquals(Mensajes.CAMPOS_OBLIGATORIOS, itinerarios.crearRuta("", TipoRuta.URBANA, "06:00", "20:00").getMensaje());
        assertEquals(Mensajes.CAMPOS_OBLIGATORIOS, itinerarios.crearRuta("Ruta", TipoRuta.URBANA, "", "20:00").getMensaje());
        assertEquals(Mensajes.TIPO_RUTA_REQUERIDO, itinerarios.crearRuta("Ruta", null, "06:00", "20:00").getMensaje());
    }

    @Test
    void redefinirUnaRutaCambiaSusDatosYMuestraRutaRedefinida() {
        crearRuta("UCV - Altamira");
        Resultado<Void> r = itinerarios.redefinirRuta("UCV - Altamira", "UCV - Chacao", TipoRuta.EXTRAURBANA, "07:00", "19:00");
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
        Resultado<Void> r = itinerarios.redefinirRuta("UCV - Altamira", "UCV - Altamira", TipoRuta.URBANA, "08:00", "17:00");
        assertEquals(Mensajes.RUTA_REDEFINIDA, r.getMensaje());
        assertEquals(LocalTime.of(8, 0), repoRutas.buscarPorNombre("UCV - Altamira").getInicioJornada());
    }

    @Test
    void redefinirConElNombreDeOtraRutaEsRechazado() {
        crearRuta("Ruta A");
        crearRuta("Ruta B");
        Resultado<Void> r = itinerarios.redefinirRuta("Ruta A", "ruta b", TipoRuta.URBANA, "06:00", "20:00");
        assertEquals(Mensajes.RUTA_EXISTENTE, r.getMensaje());
        assertNotNull(repoRutas.buscarPorNombre("Ruta A")); // no se perdió nada
    }

    @Test
    void redefinirUnaRutaInexistenteFalla() {
        Resultado<Void> r = itinerarios.redefinirRuta("No existe", "Nueva", TipoRuta.URBANA, "06:00", "20:00");
        assertEquals(Mensajes.RUTA_NO_ENCONTRADA, r.getMensaje());
    }

    @Test
    void alRenombrarUnaRutaSusUnidadesLaSiguen() {
        crearRuta("Ruta A");
        unidad("ABC123", 40);
        itinerarios.asignarUnidad("ABC123", "Ruta A", false);
        itinerarios.redefinirRuta("Ruta A", "Ruta Nueva", TipoRuta.URBANA, "06:00", "20:00");
        assertEquals(1, itinerarios.unidadesDeRuta("Ruta Nueva").size());
        assertEquals(40, itinerarios.cuposDeRuta("Ruta Nueva"));
    }

    @Test
    void soloElAdminPuedeDefinirRutas() {
        usuarios.iniciarSesion("conductor@ucv.com", DatosSemilla.CLAVE_DEMO);
        assertEquals(Mensajes.ACCESO_DENEGADO, crearRuta("Ruta A").getMensaje());
        usuarios.cerrarSesion();
        assertEquals(Mensajes.ACCESO_DENEGADO, crearRuta("Ruta A").getMensaje());
        assertTrue(itinerarios.listarRutas().isEmpty());
    }

    // =================================================================== HU12
    @Test
    void asignarUnaUnidadLibreLaRelacionaConLaRutaYSumaCupos() {
        crearRuta("Ruta A");
        unidad("ABC123", 40);
        Resultado<Void> r = itinerarios.asignarUnidad("abc123", "ruta a", false);
        assertTrue(r.isOk());
        assertEquals(Mensajes.UNIDAD_ASIGNADA, r.getMensaje());
        assertEquals(1, itinerarios.unidadesDeRuta("Ruta A").size());
        assertEquals(40, itinerarios.cuposDeRuta("Ruta A"));
    }

    @Test
    void loscuposDeUnaRutaSonLaSumaDeLasCapacidadesDeSusUnidades() {
        crearRuta("Ruta A");
        unidad("ABC123", 40);
        unidad("DEF456", 35);
        itinerarios.asignarUnidad("ABC123", "Ruta A", false);
        itinerarios.asignarUnidad("DEF456", "Ruta A", false);
        assertEquals(75, itinerarios.cuposDeRuta("Ruta A"));
    }

    @Test
    void asignarUnaUnidadALaRutaDondeYaEstaMuestraElMensajeDeLaHU() {
        crearRuta("Ruta A");
        unidad("ABC123", 40);
        itinerarios.asignarUnidad("ABC123", "Ruta A", false);
        Resultado<Void> r = itinerarios.asignarUnidad("ABC123", "Ruta A", true);
        assertFalse(r.isOk());
        assertEquals(Mensajes.UNIDAD_YA_ASIGNADA, r.getMensaje());
        assertEquals(40, itinerarios.cuposDeRuta("Ruta A")); // no se duplica
    }

    @Test
    void moverUnaUnidadDeRutaRequiereConfirmacion() {
        crearRuta("Ruta A");
        crearRuta("Ruta B");
        unidad("ABC123", 40);
        itinerarios.asignarUnidad("ABC123", "Ruta A", false);

        assertTrue(itinerarios.requiereConfirmacion("ABC123", "Ruta B"));
        assertFalse(itinerarios.requiereConfirmacion("ABC123", "Ruta A"));

        Resultado<Void> sinConfirmar = itinerarios.asignarUnidad("ABC123", "Ruta B", false);
        assertEquals(Mensajes.CONFIRMAR_CAMBIO, sinConfirmar.getMensaje()); // "¿Está seguro del cambio?"
        assertEquals(40, itinerarios.cuposDeRuta("Ruta A")); // nada cambió todavía

        Resultado<Void> confirmada = itinerarios.asignarUnidad("ABC123", "Ruta B", true);
        assertTrue(confirmada.isOk());
        assertEquals(Mensajes.UNIDAD_REASIGNADA, confirmada.getMensaje());
        assertEquals(0, itinerarios.cuposDeRuta("Ruta A"));
        assertEquals(40, itinerarios.cuposDeRuta("Ruta B"));
    }

    @Test
    void soloSePuedenAsignarUnidadesActivas() {
        crearRuta("Ruta A");
        flota.registrarUnidad("ABC123", "Modelo X", "40", EstadoUnidad.EN_MANTENIMIENTO);
        Resultado<Void> r = itinerarios.asignarUnidad("ABC123", "Ruta A", false);
        assertEquals(Mensajes.UNIDAD_NO_ACTIVA, r.getMensaje());
        assertEquals(0, itinerarios.cuposDeRuta("Ruta A"));
    }

    @Test
    void siUnaUnidadAsignadaDejaDeEstarActivaSeLiberaDeSuRuta() {
        crearRuta("Ruta A");
        unidad("ABC123", 40);
        itinerarios.asignarUnidad("ABC123", "Ruta A", false);
        Resultado<Void> r = flota.cambiarEstado("ABC123", EstadoUnidad.FUERA_DE_SERVICIO);
        assertEquals(Mensajes.ESTADO_ACTUALIZADO_LIBERADA, r.getMensaje()); // avisa que la unidad se liberó
        assertEquals(0, itinerarios.cuposDeRuta("Ruta A"));
        assertTrue(itinerarios.unidadesDeRuta("Ruta A").isEmpty());
    }

    @Test
    void asignarConUnidadORutaInexistenteFalla() {
        crearRuta("Ruta A");
        unidad("ABC123", 40);
        assertEquals(Mensajes.UNIDAD_NO_ENCONTRADA, itinerarios.asignarUnidad("ZZZ999", "Ruta A", false).getMensaje());
        assertEquals(Mensajes.RUTA_NO_ENCONTRADA, itinerarios.asignarUnidad("ABC123", "No existe", false).getMensaje());
    }

    @Test
    void soloElAdminPuedeAsignarUnidades() {
        crearRuta("Ruta A");
        unidad("ABC123", 40);
        usuarios.iniciarSesion("empleado@ucv.com", DatosSemilla.CLAVE_DEMO);
        assertEquals(Mensajes.ACCESO_DENEGADO, itinerarios.asignarUnidad("ABC123", "Ruta A", false).getMensaje());
        assertEquals(0, itinerarios.cuposDeRuta("Ruta A"));
    }

    @Test
    void elNombreDeLaRutaNoPuedePasarDeSesentaCaracteres() {
        String justo = "R".repeat(60);
        String largo = "R".repeat(61);
        assertTrue(itinerarios.crearRuta(justo, TipoRuta.URBANA, "06:00", "20:00").isOk());
        assertEquals(Mensajes.NOMBRE_RUTA_LARGO, itinerarios.crearRuta(largo, TipoRuta.URBANA, "06:00", "20:00").getMensaje());
        assertEquals(Mensajes.NOMBRE_RUTA_LARGO, itinerarios.redefinirRuta(justo, largo, TipoRuta.URBANA, "06:00", "20:00").getMensaje());
    }
}
