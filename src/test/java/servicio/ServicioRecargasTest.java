package servicio;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import modelo.Mensajes;
import modelo.MensajesMonedero;
import modelo.RecargaPagoMovil;
import modelo.Resultado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistencia.memoria.DatosSemilla;
import persistencia.memoria.RepositorioMovimientos;
import persistencia.memoria.RepositorioRecargas;
import persistencia.memoria.RepositorioUsuarios;

/** HU04 y HU05: recarga del monedero por pago móvil simulada, con registro de operaciones por usuario. */
class ServicioRecargasTest {

    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-10-14T15:30:00Z"), ZoneId.of("America/Caracas"));
    private static final String ORIGEN = "0102 - Banco de Venezuela";
    private static final String DESTINO = "0134 - Banesco";
    private static final String CEDULA = "12345678";
    private static final String TELEFONO = "04121234567";
    private static final String REFERENCIA = "123456789012";

    private ServicioUsuarios usuarios;
    private ServicioMonedero monedero;
    private ServicioRecargas recargas;

    @BeforeEach
    void preparar() {
        RepositorioUsuarios repoUsuarios = new RepositorioUsuarios();
        DatosSemilla.sembrarUsuarios(repoUsuarios);
        usuarios = new ServicioUsuarios(repoUsuarios);
        monedero = new ServicioMonedero(new RepositorioMovimientos(), RELOJ);
        recargas = new ServicioRecargas(new RepositorioRecargas(), monedero, usuarios, usuarios, RELOJ);
        entrar("estudiante@ucv.com");
    }

    private void entrar(String correo) {
        usuarios.cerrarSesion();
        usuarios.iniciarSesion(correo, DatosSemilla.CLAVE_DEMO);
    }

    private Resultado<Void> recargarBien(String referencia, String monto) {
        return recargas.recargar(ORIGEN, DESTINO, CEDULA, TELEFONO, referencia, monto);
    }

    private static BigDecimal d(String valor) {
        return new BigDecimal(valor);
    }

    private List<RecargaPagoMovil> mias() {
        return recargas.misRecargas(100).getValor();
    }

    // ------------------------------------------------------------ HU04 escenario 1: datos válidos
    @Test
    @DisplayName("Dado datos válidos, cuando el pasajero recarga, entonces se acepta, se suma al monedero y se muestran los mensajes de las HU")
    void unaRecargaValidaSumaAlMonedero() {
        Resultado<Void> r = recargarBien(REFERENCIA, "25.50");

        assertTrue(r.isOk());
        assertTrue(r.getMensaje().contains(MensajesMonedero.SOLICITUD_ACEPTADA));
        assertTrue(r.getMensaje().contains(MensajesMonedero.RECARGA_EXITOSA));
        assertTrue(r.getMensaje().contains("25.50"));
        assertEquals(d("25.50"), monedero.saldo("estudiante@ucv.com"));
    }

    @Test
    @DisplayName("Dado una recarga exitosa, cuando se consulta el registro, entonces queda anotada con todos sus datos y la fecha")
    void laRecargaExitosaQuedaRegistrada() {
        recargarBien(REFERENCIA, "25,5");

        RecargaPagoMovil registro = mias().get(0);

        assertTrue(registro.isExitosa());
        assertEquals("estudiante@ucv.com", registro.getCorreo());
        assertEquals(ORIGEN, registro.getBancoOrigen());
        assertEquals(DESTINO, registro.getBancoDestino());
        assertEquals(CEDULA, registro.getCedula());
        assertEquals(TELEFONO, registro.getTelefono());
        assertEquals(REFERENCIA, registro.getReferencia());
        assertEquals("25.50", registro.getMonto());
        assertEquals(MensajesMonedero.RECARGA_EXITOSA, registro.getDetalle());
        assertEquals(LocalDateTime.now(RELOJ), registro.getFecha());
    }

    @Test
    @DisplayName("Dado un estudiante, un empleado y un público general, cuando recargan, entonces cada uno suma solo a su propia cuenta")
    void recargaCadaTipoDePasajeroEnSuPropiaCuenta() {
        String[] correos = {"estudiante@ucv.com", "empleado@ucv.com", "publico@ucv.com"};
        for (int i = 0; i < correos.length; i++) {
            entrar(correos[i]);
            assertTrue(recargarBien("10000000000" + i, "10").isOk(), correos[i]);
        }
        for (String correo : correos) {
            assertEquals(d("10.00"), monedero.saldo(correo));
        }
    }

    // ------------------------------------------------------------ quién puede recargar
    @Test
    @DisplayName("Dado un conductor, un administrador o nadie con sesión, cuando intentan recargar, entonces se les niega el acceso y no se registra nada")
    void soloLosPasajerosRecargan() {
        for (String correo : new String[] {"conductor@ucv.com", "admin@ucv.com", null}) {
            usuarios.cerrarSesion();
            if (correo != null) {
                usuarios.iniciarSesion(correo, DatosSemilla.CLAVE_DEMO);
            }
            Resultado<Void> r = recargarBien(REFERENCIA, "10");
            assertFalse(r.isOk(), String.valueOf(correo));
            assertEquals(Mensajes.ACCESO_DENEGADO, r.getMensaje());
        }
        entrar("admin@ucv.com");
        assertTrue(recargas.usuariosConRecargas().getValor().isEmpty());
        assertEquals(d("0.00"), monedero.saldo("conductor@ucv.com"));
    }

    // ------------------------------------------------------------ HU04 escenario 2: datos incorrectos
    private void assertRechazo(Resultado<Void> r, String lineaEsperada) {
        assertFalse(r.isOk());
        assertTrue(r.getMensaje().startsWith(MensajesMonedero.DATOS_PAGO_INCORRECTOS), r.getMensaje());
        assertTrue(r.getMensaje().contains(lineaEsperada), r.getMensaje());
    }

    @Test
    @DisplayName("Dado un teléfono que no tiene 11 dígitos, cuando recarga, entonces se rechaza")
    void telefonoInvalido() {
        for (String malo : new String[] {"", "0412123456", "041212345678", "0412abc4567", "0412 123 4567", null}) {
            assertRechazo(recargas.recargar(ORIGEN, DESTINO, CEDULA, malo, REFERENCIA, "10"), MensajesMonedero.TELEFONO_INVALIDO);
        }
        assertEquals(d("0.00"), monedero.saldo("estudiante@ucv.com"));
    }

    @Test
    @DisplayName("Dado una cédula con letras o fuera de 5 a 10 dígitos, cuando recarga, entonces se rechaza")
    void cedulaInvalida() {
        for (String mala : new String[] {"", "1234", "12345678901", "12a45678", "V-1234567", null}) {
            assertRechazo(recargas.recargar(ORIGEN, DESTINO, mala, TELEFONO, REFERENCIA, "10"), MensajesMonedero.CEDULA_PAGO_INVALIDA);
        }
    }

    @Test
    @DisplayName("Dado una referencia que no tiene entre 6 y 12 dígitos, cuando recarga, entonces se rechaza; los límites 6 y 12 se aceptan")
    void referenciaInvalidaYLimites() {
        for (String mala : new String[] {"", "12345", "1234567890123", "12345a", "123 456", null}) {
            assertRechazo(recargas.recargar(ORIGEN, DESTINO, CEDULA, TELEFONO, mala, "10"), MensajesMonedero.REFERENCIA_INVALIDA);
        }
        assertTrue(recargarBien("123456", "10").isOk());
        assertTrue(recargarBien("654321654321", "10").isOk());
    }

    @Test
    @DisplayName("Dado un monto cero, negativo, con letras o con más de 2 decimales, cuando recarga, entonces se rechaza")
    void montoInvalido() {
        for (String malo : new String[] {"", "0", "0.00", "-5", "abc", "1.234", null}) {
            assertRechazo(recargas.recargar(ORIGEN, DESTINO, CEDULA, TELEFONO, REFERENCIA, malo), MensajesMonedero.MONTO_PAGO_INVALIDO);
        }
        assertTrue(recargarBien("111111111111", "0.01").isOk());
    }

    @Test
    @DisplayName("Dado un banco que no está en la lista (o un banco de origen usado como destino), cuando recarga, entonces se rechaza")
    void bancoInvalido() {
        assertRechazo(recargas.recargar("Banco inventado", DESTINO, CEDULA, TELEFONO, REFERENCIA, "10"), MensajesMonedero.BANCO_ORIGEN_INVALIDO);
        assertRechazo(recargas.recargar(null, DESTINO, CEDULA, TELEFONO, REFERENCIA, "10"), MensajesMonedero.BANCO_ORIGEN_INVALIDO);
        assertRechazo(recargas.recargar(ORIGEN, "Banco inventado", CEDULA, TELEFONO, REFERENCIA, "10"), MensajesMonedero.BANCO_DESTINO_INVALIDO);
        // 0108 sirve como banco de origen, pero la institución no recibe pagos en él
        assertTrue(recargas.recargar("0108 - Banco Provincial", DESTINO, CEDULA, TELEFONO, REFERENCIA, "10").isOk());
        assertRechazo(recargas.recargar(ORIGEN, "0108 - Banco Provincial", CEDULA, TELEFONO, "999999999999", "10"), MensajesMonedero.BANCO_DESTINO_INVALIDO);
    }

    @Test
    @DisplayName("Dado todos los campos mal, cuando recarga, entonces el mensaje lista cada campo incorrecto en su propia línea")
    void elMensajeListaTodosLosErrores() {
        Resultado<Void> r = recargas.recargar("", "", "", "", "", "");

        String[] lineas = r.getMensaje().split("\n");
        assertEquals(MensajesMonedero.DATOS_PAGO_INCORRECTOS, lineas[0]);
        assertEquals(7, lineas.length); // el encabezado + 6 campos
    }

    @Test
    @DisplayName("Dado espacios alrededor de los datos, cuando recarga, entonces se ignoran")
    void losEspaciosAlrededorSeIgnoran() {
        assertTrue(recargas.recargar(" " + ORIGEN + " ", DESTINO, " " + CEDULA, TELEFONO + " ", " " + REFERENCIA + " ", " 10 ").isOk());
        assertEquals(REFERENCIA, mias().get(0).getReferencia());
    }

    @Test
    @DisplayName("Dado una recarga rechazada, cuando se consulta, entonces el saldo no cambia, el monedero no anota nada y queda registrada como fallida")
    void unaRecargaFallidaNoTocaElMonederoPeroQuedaRegistrada() {
        recargas.recargar(ORIGEN, DESTINO, CEDULA, "123", REFERENCIA, "10");

        assertEquals(d("0.00"), monedero.saldo("estudiante@ucv.com"));
        assertTrue(monedero.historial("estudiante@ucv.com", 10).isEmpty());
        assertEquals(1, mias().size());
        assertFalse(mias().get(0).isExitosa());
        assertEquals(MensajesMonedero.RECARGA_FALLIDA, mias().get(0).getDetalle());
    }

    // ------------------------------------------------------------ referencia repetida
    @Test
    @DisplayName("Dado una referencia ya usada, cuando se vuelve a usar, entonces se rechaza aunque venga de otro usuario y no se acredita dos veces")
    void laReferenciaSoloSePuedeUsarUnaVez() {
        assertTrue(recargarBien(REFERENCIA, "10").isOk());

        Resultado<Void> repetida = recargarBien(REFERENCIA, "10");
        assertFalse(repetida.isOk());
        assertEquals(MensajesMonedero.REFERENCIA_REPETIDA, repetida.getMensaje());

        entrar("empleado@ucv.com");
        assertEquals(MensajesMonedero.REFERENCIA_REPETIDA, recargarBien(REFERENCIA, "10").getMensaje());

        assertEquals(d("10.00"), monedero.saldo("estudiante@ucv.com"));
        assertEquals(d("0.00"), monedero.saldo("empleado@ucv.com"));
        entrar("estudiante@ucv.com");
        assertEquals(2, mias().size());
        assertFalse(mias().get(0).isExitosa()); // la más reciente es el intento repetido
        assertEquals(MensajesMonedero.REFERENCIA_REPETIDA, mias().get(0).getDetalle());
    }

    // ------------------------------------------------------------ consulta del propio usuario
    @Test
    @DisplayName("Dado varias operaciones de dos usuarios, cuando uno consulta las suyas, entonces ve solo las suyas, de la más reciente a la más antigua y con límite")
    void misRecargasSonSoloMiasConLimite() {
        recargarBien("100000000001", "1");
        recargarBien("100000000002", "2");
        recargarBien("100000000003", "3");
        entrar("empleado@ucv.com");
        recargarBien("200000000001", "9");
        entrar("estudiante@ucv.com");

        List<RecargaPagoMovil> ultimas = recargas.misRecargas(2).getValor();

        assertEquals(2, ultimas.size());
        assertEquals("3.00", ultimas.get(0).getMonto());
        assertEquals("2.00", ultimas.get(1).getMonto());
        assertEquals(3, recargas.misRecargas(100).getValor().size());
        assertTrue(recargas.misRecargas(0).getValor().isEmpty());
    }

    @Test
    @DisplayName("Dado un usuario sin operaciones, cuando consulta las suyas, entonces la lista está vacía")
    void sinOperacionesLaListaEstaVacia() {
        assertTrue(mias().isEmpty());
    }

    // ------------------------------------------------------------ HU05: el administrador consulta por usuario
    @Test
    @DisplayName("Dado operaciones de varios usuarios, cuando el administrador consulta, entonces ve la lista de usuarios y las operaciones de cada uno")
    void elAdminConsultaLasRecargasPorUsuario() {
        recargarBien("100000000001", "5");
        recargas.recargar(ORIGEN, DESTINO, CEDULA, "123", REFERENCIA, "5"); // fallida
        entrar("empleado@ucv.com");
        recargarBien("200000000001", "7");
        entrar("admin@ucv.com");

        Resultado<List<String>> usuariosConRecargas = recargas.usuariosConRecargas();
        assertTrue(usuariosConRecargas.isOk());
        assertEquals(List.of("empleado@ucv.com", "estudiante@ucv.com"), usuariosConRecargas.getValor());

        List<RecargaPagoMovil> delEstudiante = recargas.recargasDe("estudiante@ucv.com").getValor();
        assertEquals(2, delEstudiante.size());
        assertFalse(delEstudiante.get(0).isExitosa()); // la fallida es la más reciente
        assertTrue(delEstudiante.get(1).isExitosa());
        assertEquals(1, recargas.recargasDe("EMPLEADO@ucv.com").getValor().size());
        assertTrue(recargas.recargasDe("nadie@ucv.com").getValor().isEmpty());
    }

    @Test
    @DisplayName("Dado un pasajero o nadie con sesión, cuando intenta ver las operaciones de otros usuarios, entonces se le niega el acceso")
    void soloElAdminVeLasRecargasDeOtros() {
        recargarBien("100000000001", "5");

        assertEquals(Mensajes.ACCESO_DENEGADO, recargas.usuariosConRecargas().getMensaje());
        assertEquals(Mensajes.ACCESO_DENEGADO, recargas.recargasDe("estudiante@ucv.com").getMensaje());
        usuarios.cerrarSesion();
        assertEquals(Mensajes.ACCESO_DENEGADO, recargas.recargasDe("estudiante@ucv.com").getMensaje());
        assertEquals(Mensajes.ACCESO_DENEGADO, recargas.misRecargas(5).getMensaje());
    }
}
