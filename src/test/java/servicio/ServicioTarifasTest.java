package servicio;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import modelo.Mensajes;
import modelo.Resultado;
import modelo.Rol;
import modelo.TipoRuta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistencia.memoria.DatosSemilla;
import persistencia.memoria.RepositorioRutas;
import persistencia.memoria.RepositorioTarifas;
import persistencia.memoria.RepositorioUnidades;
import persistencia.memoria.RepositorioUsuarios;

/** HU11: Configuración de Tarifas del cobro solidario (solo rutas extraurbanas). */
class ServicioTarifasTest {

    private static final String EXTRA = "UCV - Guarenas";
    private static final String URBANA = "UCV - Altamira";

    private ServicioUsuarios usuarios;
    private ServicioRutas rutas;
    private ServicioTarifas tarifas;

    @BeforeEach
    void preparar() {
        RepositorioUsuarios repoUsuarios = new RepositorioUsuarios();
        DatosSemilla.sembrarUsuarios(repoUsuarios);
        usuarios = new ServicioUsuarios(repoUsuarios);
        RepositorioRutas repoRutas = new RepositorioRutas();
        RepositorioTarifas repoTarifas = new RepositorioTarifas();
        rutas = new ServicioRutas(repoRutas, new RepositorioUnidades(), repoTarifas, usuarios);
        tarifas = new ServicioTarifas(repoTarifas, repoRutas, usuarios);
        usuarios.iniciarSesion("admin@ucv.com", DatosSemilla.CLAVE_DEMO);
        rutas.crearRuta(URBANA, TipoRuta.URBANA, "06:00", "20:00");
        rutas.crearRuta(EXTRA, TipoRuta.EXTRAURBANA, "05:30", "19:30");
    }

    private static BigDecimal d(String valor) {
        return new BigDecimal(valor);
    }

    // ------------------------------------------------------------ HU11 escenario 1: registrar la tarifa base
    @Test
    @DisplayName("Dado una ruta extraurbana, cuando el admin registra una T_base mayor que cero, entonces queda guardada")
    void registrarLaTarifaBaseDeUnaRutaExtraurbana() {
        Resultado<Void> r = tarifas.registrarTarifaBase(EXTRA, "10");
        assertTrue(r.isOk());
        assertEquals(Mensajes.TARIFA_REGISTRADA, r.getMensaje());
        assertEquals(d("10.00"), tarifas.tarifaBase(EXTRA));
    }

    @Test
    @DisplayName("Dado que el nombre de la ruta no distingue mayúsculas, cuando se registra con otro formato, entonces se encuentra igual")
    void elNombreDeLaRutaNoDistingueMayusculas() {
        assertTrue(tarifas.registrarTarifaBase("ucv - guarenas", "10").isOk());
        assertEquals(d("10.00"), tarifas.tarifaBase(EXTRA));
    }

    @Test
    @DisplayName("Dado una T_base con coma decimal, cuando se registra, entonces se acepta")
    void aceptaComaDecimal() {
        tarifas.registrarTarifaBase(EXTRA, "12,5");
        assertEquals(d("12.50"), tarifas.tarifaBase(EXTRA));
    }

    @Test
    @DisplayName("Dado que no existe ninguna ruta extraurbana, cuando se registra una tarifa, entonces se avisa")
    void sinRutasExtraurbanasNoSePuedeConfigurar() {
        RepositorioRutas soloUrbanas = new RepositorioRutas();
        RepositorioTarifas repoTarifas = new RepositorioTarifas();
        new ServicioRutas(soloUrbanas, new RepositorioUnidades(), repoTarifas, usuarios)
                .crearRuta(URBANA, TipoRuta.URBANA, "06:00", "20:00");
        ServicioTarifas servicio = new ServicioTarifas(repoTarifas, soloUrbanas, usuarios);

        assertEquals(Mensajes.SIN_RUTAS_EXTRAURBANAS, servicio.registrarTarifaBase(URBANA, "10").getMensaje());
    }

    @Test
    @DisplayName("Dado una ruta urbana, cuando se intenta ponerle tarifa, entonces se rechaza")
    void laTarifaSoloAplicaARutasExtraurbanas() {
        assertEquals(Mensajes.RUTA_NO_EXTRAURBANA, tarifas.registrarTarifaBase(URBANA, "10").getMensaje());
        assertNull(tarifas.tarifaBase(URBANA));
    }

    @Test
    @DisplayName("Dado una ruta que no existe o ninguna ruta elegida, cuando se registra, entonces se rechaza")
    void rutaInexistenteOVacia() {
        assertEquals(Mensajes.RUTA_NO_ENCONTRADA, tarifas.registrarTarifaBase("Inventada", "10").getMensaje());
        assertEquals(Mensajes.RUTA_REQUERIDA, tarifas.registrarTarifaBase("", "10").getMensaje());
        assertEquals(Mensajes.RUTA_REQUERIDA, tarifas.registrarTarifaBase(null, "10").getMensaje());
    }

    @Test
    @DisplayName("Dado una T_base cero, negativa, con letras o con más de 2 decimales, cuando se registra, entonces se rechaza")
    void tarifaBaseInvalida() {
        for (String mala : new String[] {"", "0", "0.00", "-5", "abc", "1.234", null}) {
            Resultado<Void> r = tarifas.registrarTarifaBase(EXTRA, mala);
            assertFalse(r.isOk(), "debía rechazar: " + mala);
            assertEquals(Mensajes.TARIFA_INVALIDA, r.getMensaje());
        }
        assertNull(tarifas.tarifaBase(EXTRA));
    }

    @Test
    @DisplayName("Dado una ruta que ya tiene tarifa, cuando se vuelve a registrar, entonces se rechaza y el valor no cambia")
    void noSeRegistraDosVeces() {
        tarifas.registrarTarifaBase(EXTRA, "10");
        Resultado<Void> r = tarifas.registrarTarifaBase(EXTRA, "99");
        assertEquals(Mensajes.TARIFA_YA_REGISTRADA, r.getMensaje());
        assertEquals(d("10.00"), tarifas.tarifaBase(EXTRA));
    }

    // ------------------------------------------------------------ HU11 escenario 2: actualizar
    @Test
    @DisplayName("Dado una ruta sin tarifa, cuando se intenta actualizar, entonces se indica que primero hay que guardarla")
    void noSeActualizaUnaTarifaInexistente() {
        assertEquals(Mensajes.TARIFA_NO_REGISTRADA, tarifas.actualizarTarifaBase(EXTRA, "10").getMensaje());
    }

    @Test
    @DisplayName("Dado una tarifa registrada, cuando el admin la actualiza, entonces los montos por tipo de usuario se recalculan")
    void actualizarRecalculaLosMontos() {
        tarifas.registrarTarifaBase(EXTRA, "100");
        assertEquals(d("15.00"), tarifas.montoPara(Rol.ESTUDIANTE, EXTRA));

        Resultado<Void> r = tarifas.actualizarTarifaBase(EXTRA, "200");

        assertTrue(r.isOk());
        assertEquals(Mensajes.TARIFA_ACTUALIZADA, r.getMensaje());
        assertEquals(d("30.00"), tarifas.montoPara(Rol.ESTUDIANTE, EXTRA));
        assertEquals(d("90.00"), tarifas.montoPara(Rol.EMPLEADO, EXTRA));
        assertEquals(d("200.00"), tarifas.montoPara(Rol.PUBLICO_GENERAL, EXTRA));
    }

    // ------------------------------------------------------------ cálculo de lo que paga cada tipo de usuario
    @Test
    @DisplayName("Dado la escala por defecto, cuando se calcula con T_base 100, entonces Estudiante paga 15, Empleado 45 y Público General 100")
    void montosConLosPorcentajesPorDefecto() {
        tarifas.registrarTarifaBase(EXTRA, "100");
        assertEquals(d("15.00"), tarifas.montoPara(Rol.ESTUDIANTE, EXTRA));
        assertEquals(d("45.00"), tarifas.montoPara(Rol.EMPLEADO, EXTRA));
        assertEquals(d("100.00"), tarifas.montoPara(Rol.PUBLICO_GENERAL, EXTRA));
    }

    @Test
    @DisplayName("Dado un resultado con medio centavo, cuando se calcula, entonces se redondea hacia arriba")
    void elRedondeoEsHalfUp() {
        tarifas.registrarTarifaBase(EXTRA, "12.50");
        assertEquals(d("1.88"), tarifas.montoPara(Rol.ESTUDIANTE, EXTRA)); // 1.875
        assertEquals(d("5.63"), tarifas.montoPara(Rol.EMPLEADO, EXTRA)); // 5.625
    }

    @Test
    @DisplayName("Dado conductores y administradores, cuando se calcula, entonces no pagan tarifa")
    void conductoresYAdministradoresNoPagan() {
        tarifas.registrarTarifaBase(EXTRA, "100");
        assertEquals(d("0.00"), tarifas.montoPara(Rol.CONDUCTOR, EXTRA));
        assertEquals(d("0.00"), tarifas.montoPara(Rol.ADMIN_TRANSPORTE, EXTRA));
    }

    @Test
    @DisplayName("Dado una ruta urbana, una ruta sin tarifa o una ruta inexistente, cuando se calcula, entonces no se cobra")
    void sinTarifaNoSeCobra() {
        assertEquals(d("0.00"), tarifas.montoPara(Rol.ESTUDIANTE, EXTRA)); // extraurbana pero sin tarifa
        assertEquals(d("0.00"), tarifas.montoPara(Rol.ESTUDIANTE, URBANA));
        assertEquals(d("0.00"), tarifas.montoPara(Rol.ESTUDIANTE, "Inventada"));
        assertEquals(d("0.00"), tarifas.montoPara(Rol.ESTUDIANTE, null));
    }

    @Test
    @DisplayName("Dado una ruta con tarifa, cuando se pide el desglose, entonces sale en el orden Estudiante, Empleado, Público General")
    void desgloseEnElOrdenDeLaTabla() {
        tarifas.registrarTarifaBase(EXTRA, "100");
        Map<Rol, BigDecimal> desglose = tarifas.desglose(EXTRA);
        assertEquals(List.of(Rol.ESTUDIANTE, Rol.EMPLEADO, Rol.PUBLICO_GENERAL), List.copyOf(desglose.keySet()));
        assertEquals(d("45.00"), desglose.get(Rol.EMPLEADO));
    }

    // ------------------------------------------------------------ porcentajes configurables dentro de su rango
    @Test
    @DisplayName("Dado un porcentaje dentro del rango, cuando el admin lo configura, entonces los montos lo usan")
    void configurarUnPorcentajeValido() {
        tarifas.registrarTarifaBase(EXTRA, "100");
        assertTrue(tarifas.configurarPorcentaje(Rol.ESTUDIANTE, "20").isOk());
        assertTrue(tarifas.configurarPorcentaje(Rol.EMPLEADO, "30").isOk());
        assertEquals(d("20.00"), tarifas.montoPara(Rol.ESTUDIANTE, EXTRA));
        assertEquals(d("30.00"), tarifas.montoPara(Rol.EMPLEADO, EXTRA));
    }

    @Test
    @DisplayName("Dado porcentajes en los extremos del rango, cuando se configuran, entonces se aceptan; fuera de él se rechazan")
    void losLimitesDeLosRangosSeRespetan() {
        assertTrue(tarifas.configurarPorcentaje(Rol.ESTUDIANTE, "10").isOk());
        assertTrue(tarifas.configurarPorcentaje(Rol.ESTUDIANTE, "20").isOk());
        assertTrue(tarifas.configurarPorcentaje(Rol.EMPLEADO, "30").isOk());
        assertTrue(tarifas.configurarPorcentaje(Rol.EMPLEADO, "60").isOk());

        Resultado<Void> pocoEstudiante = tarifas.configurarPorcentaje(Rol.ESTUDIANTE, "9.99");
        assertEquals(String.format(Mensajes.PORCENTAJE_FUERA_DE_RANGO, "Estudiante", "10", "20"), pocoEstudiante.getMensaje());
        assertFalse(tarifas.configurarPorcentaje(Rol.ESTUDIANTE, "20.01").isOk());
        assertFalse(tarifas.configurarPorcentaje(Rol.EMPLEADO, "29.99").isOk());
        assertFalse(tarifas.configurarPorcentaje(Rol.EMPLEADO, "60.01").isOk());
    }

    @Test
    @DisplayName("Dado un porcentaje rechazado, cuando se consulta, entonces se conserva el último valor válido")
    void unPorcentajeRechazadoNoCambiaNada() {
        tarifas.configurarPorcentaje(Rol.ESTUDIANTE, "12");
        tarifas.configurarPorcentaje(Rol.ESTUDIANTE, "50");
        assertEquals(d("12.00"), tarifas.porcentajeDe(Rol.ESTUDIANTE));
    }

    @Test
    @DisplayName("Dado el público general, conductores o un texto no numérico, cuando se configura un porcentaje, entonces se rechaza")
    void porcentajesNoConfigurables() {
        assertEquals(Mensajes.PORCENTAJE_PUBLICO_FIJO, tarifas.configurarPorcentaje(Rol.PUBLICO_GENERAL, "100").getMensaje());
        assertEquals(Mensajes.ROL_SIN_TARIFA, tarifas.configurarPorcentaje(Rol.CONDUCTOR, "15").getMensaje());
        assertEquals(Mensajes.PORCENTAJE_INVALIDO, tarifas.configurarPorcentaje(Rol.ESTUDIANTE, "mucho").getMensaje());
        assertEquals(d("100"), tarifas.porcentajeDe(Rol.PUBLICO_GENERAL));
    }

    // ------------------------------------------------------------ porcentajes de ambos tipos de usuario a la vez
    @Test
    @DisplayName("Dado porcentajes válidos para ambos tipos de usuario, cuando el admin los guarda, entonces los dos quedan configurados")
    void configurarAmbosPorcentajes() {
        Resultado<Void> r = tarifas.configurarPorcentajes("12", "50");

        assertTrue(r.isOk());
        assertEquals(Mensajes.PORCENTAJE_ACTUALIZADO, r.getMensaje());
        assertEquals(d("12.00"), tarifas.porcentajeDe(Rol.ESTUDIANTE));
        assertEquals(d("50.00"), tarifas.porcentajeDe(Rol.EMPLEADO));
    }

    @Test
    @DisplayName("Dado un campo en blanco, cuando se guardan los porcentajes, entonces ese tipo de usuario queda como estaba")
    void unCampoEnBlancoDejaElValorActual() {
        tarifas.configurarPorcentajes("18", "55");

        assertTrue(tarifas.configurarPorcentajes("", "40").isOk());
        assertTrue(tarifas.configurarPorcentajes("11", "  ").isOk());

        assertEquals(d("11.00"), tarifas.porcentajeDe(Rol.ESTUDIANTE));
        assertEquals(d("40.00"), tarifas.porcentajeDe(Rol.EMPLEADO));
    }

    @Test
    @DisplayName("Dado ambos campos en blanco, cuando se guardan los porcentajes, entonces se pide ingresar al menos uno")
    void ambosEnBlancoSeRechaza() {
        assertEquals(Mensajes.PORCENTAJES_VACIOS, tarifas.configurarPorcentajes("", "").getMensaje());
        assertEquals(Mensajes.PORCENTAJES_VACIOS, tarifas.configurarPorcentajes(null, null).getMensaje());
    }

    @Test
    @DisplayName("Dado un porcentaje válido y otro inválido, cuando se guardan, entonces no se guarda ninguno de los dos")
    void siUnoEsInvalidoNoSeGuardaNinguno() {
        Resultado<Void> r = tarifas.configurarPorcentajes("12", "99");

        assertFalse(r.isOk());
        assertEquals(String.format(Mensajes.PORCENTAJE_FUERA_DE_RANGO, "Empleado", "30", "60"), r.getMensaje());
        assertEquals(d("15"), tarifas.porcentajeDe(Rol.ESTUDIANTE)); // el 12 válido NO se guardó: sigue el valor por defecto
        assertEquals(d("45"), tarifas.porcentajeDe(Rol.EMPLEADO));

        Resultado<Void> otro = tarifas.configurarPorcentajes("25", "40"); // estudiante fuera de 10-20
        assertEquals(String.format(Mensajes.PORCENTAJE_FUERA_DE_RANGO, "Estudiante", "10", "20"), otro.getMensaje());
        assertEquals(d("45"), tarifas.porcentajeDe(Rol.EMPLEADO)); // el 40 válido tampoco se guardó
        assertEquals(Mensajes.PORCENTAJE_INVALIDO, tarifas.configurarPorcentajes("mucho", "40").getMensaje());
    }

    @Test
    @DisplayName("Dado porcentajes configurados, cuando cambia la tarifa base, entonces los montos usan los porcentajes vigentes")
    void losMontosUsanLosPorcentajesConfigurados() {
        tarifas.registrarTarifaBase(EXTRA, "200");
        tarifas.configurarPorcentajes("10", "60");

        assertEquals(d("20.00"), tarifas.montoPara(Rol.ESTUDIANTE, EXTRA));
        assertEquals(d("120.00"), tarifas.montoPara(Rol.EMPLEADO, EXTRA));
        assertEquals(d("200.00"), tarifas.montoPara(Rol.PUBLICO_GENERAL, EXTRA));
    }

    // ------------------------------------------------------------ control de acceso
    @Test
    @DisplayName("Dado un usuario que no es administrador o sin sesión, cuando configura tarifas, entonces se le niega el acceso")
    void soloElAdminConfiguraTarifas() {
        usuarios.iniciarSesion("estudiante@ucv.com", DatosSemilla.CLAVE_DEMO);
        assertEquals(Mensajes.ACCESO_DENEGADO, tarifas.registrarTarifaBase(EXTRA, "10").getMensaje());
        assertEquals(Mensajes.ACCESO_DENEGADO, tarifas.configurarPorcentaje(Rol.ESTUDIANTE, "15").getMensaje());
        assertEquals(Mensajes.ACCESO_DENEGADO, tarifas.configurarPorcentajes("15", "45").getMensaje());
        usuarios.cerrarSesion();
        assertEquals(Mensajes.ACCESO_DENEGADO, tarifas.actualizarTarifaBase(EXTRA, "10").getMensaje());
        assertNull(tarifas.tarifaBase(EXTRA));
    }
}
