package servicio;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import modelo.MensajesMonedero;
import modelo.Movimiento;
import modelo.Resultado;
import modelo.TipoMovimiento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistencia.memoria.RepositorioMovimientos;

/** Núcleo del monedero: saldo, historial, recarga, cobro y reintegro (base de HU04, HU06, HU07 y HU07b). */
class ServicioMonederoTest {

    private static final String ANA = "ana@ucv.com";
    private static final ZoneId ZONA = ZoneId.of("America/Caracas");
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-10-14T15:30:00Z"), ZONA);

    private RepositorioMovimientos repositorio;
    private ServicioMonedero monedero;

    @BeforeEach
    void preparar() {
        repositorio = new RepositorioMovimientos();
        monedero = new ServicioMonedero(repositorio, RELOJ);
    }

    private static BigDecimal d(String valor) {
        return new BigDecimal(valor);
    }

    // ------------------------------------------------------------ saldo e historial vacíos
    @Test
    @DisplayName("Dado un usuario sin movimientos, cuando consulta, entonces el saldo es 0.00 y no hay historial")
    void unUsuarioNuevoTieneSaldoCeroYHistorialVacio() {
        assertEquals(d("0.00"), monedero.saldo(ANA));
        assertTrue(monedero.historial(ANA, 10).isEmpty());
    }

    // ------------------------------------------------------------ recarga (acreditar)
    @Test
    @DisplayName("Dado una recarga validada, cuando se acredita, entonces el saldo sube y queda anotada con su referencia y fecha")
    void acreditarSumaAlSaldoYAnotaLaRecarga() {
        Resultado<Void> r = monedero.acreditar(ANA, d("25.50"), "Recarga por pago móvil", "123456789012");

        assertTrue(r.isOk());
        assertEquals(d("25.50"), monedero.saldo(ANA));
        Movimiento m = monedero.historial(ANA, 5).get(0);
        assertEquals(TipoMovimiento.RECARGA, m.getTipo());
        assertEquals(d("25.50"), m.getMonto());
        assertEquals("123456789012", m.getReferencia());
        assertEquals(LocalDateTime.now(RELOJ), m.getFecha());
    }

    // ------------------------------------------------------------ cobro (debitar)
    @Test
    @DisplayName("Dado saldo suficiente, cuando se cobra, entonces el saldo baja y queda anotado el cobro")
    void debitarConSaldoSuficienteDescuenta() {
        monedero.acreditar(ANA, d("50.00"), "Recarga", "111111111111");

        Resultado<Void> r = monedero.debitar(ANA, d("12.25"), "Reserva UCV - Guarenas");

        assertTrue(r.isOk());
        assertEquals(d("37.75"), monedero.saldo(ANA));
        assertEquals(TipoMovimiento.COBRO, monedero.historial(ANA, 1).get(0).getTipo());
    }

    @Test
    @DisplayName("Dado un saldo exactamente igual al cobro, cuando se cobra, entonces se permite y el saldo queda en 0.00")
    void sePuedeGastarElSaldoExacto() {
        monedero.acreditar(ANA, d("10.00"), "Recarga", "111111111111");
        assertTrue(monedero.debitar(ANA, d("10.00"), "Reserva").isOk());
        assertEquals(d("0.00"), monedero.saldo(ANA));
    }

    @Test
    @DisplayName("Dado saldo insuficiente, cuando se cobra, entonces se rechaza con el mensaje de la HU y no cambia nada")
    void debitarSinSaldoSuficienteFalla() {
        monedero.acreditar(ANA, d("5.00"), "Recarga", "111111111111");

        Resultado<Void> r = monedero.debitar(ANA, d("5.01"), "Reserva");

        assertFalse(r.isOk());
        assertEquals(MensajesMonedero.SALDO_INSUFICIENTE, r.getMensaje());
        assertEquals(d("5.00"), monedero.saldo(ANA));
        assertEquals(1, monedero.historial(ANA, 10).size()); // solo la recarga: no se anotó ningún cobro
    }

    @Test
    @DisplayName("Dado un usuario sin saldo, cuando se le cobra, entonces se rechaza")
    void noSePuedeCobrarAUnaCuentaVacia() {
        assertEquals(MensajesMonedero.SALDO_INSUFICIENTE, monedero.debitar(ANA, d("1.00"), "Reserva").getMensaje());
        assertEquals(d("0.00"), monedero.saldo(ANA));
    }

    // ------------------------------------------------------------ reintegro
    @Test
    @DisplayName("Dado un cobro previo, cuando se reintegra, entonces el saldo vuelve al valor original")
    void reintegrarDevuelveElDinero() {
        monedero.acreditar(ANA, d("30.00"), "Recarga", "111111111111");
        monedero.debitar(ANA, d("7.50"), "Reserva");

        Resultado<Void> r = monedero.reintegrar(ANA, d("7.50"), "Cancelación de reserva");

        assertTrue(r.isOk());
        assertEquals(d("30.00"), monedero.saldo(ANA));
        assertEquals(TipoMovimiento.REINTEGRO, monedero.historial(ANA, 1).get(0).getTipo());
    }

    // ------------------------------------------------------------ validaciones
    @Test
    @DisplayName("Dado un monto nulo, cero o negativo, cuando se intenta recargar, cobrar o reintegrar, entonces se rechaza")
    void losMontosInvalidosSeRechazanEnLasTresOperaciones() {
        monedero.acreditar(ANA, d("10.00"), "Recarga", "111111111111");
        for (BigDecimal malo : new BigDecimal[] {null, d("0"), d("0.00"), d("-1.00")}) {
            assertEquals(MensajesMonedero.MONTO_INVALIDO, monedero.acreditar(ANA, malo, "x", "222222222222").getMensaje());
            assertEquals(MensajesMonedero.MONTO_INVALIDO, monedero.debitar(ANA, malo, "x").getMensaje());
            assertEquals(MensajesMonedero.MONTO_INVALIDO, monedero.reintegrar(ANA, malo, "x").getMensaje());
        }
        assertEquals(d("10.00"), monedero.saldo(ANA));
        assertEquals(1, monedero.historial(ANA, 10).size());
    }

    @Test
    @DisplayName("Dado un correo vacío o nulo, cuando se intenta mover dinero, entonces se rechaza")
    void laCuentaDebeSerValida() {
        assertEquals(MensajesMonedero.CUENTA_INVALIDA, monedero.acreditar(null, d("1.00"), "x", "1").getMensaje());
        assertEquals(MensajesMonedero.CUENTA_INVALIDA, monedero.debitar("  ", d("1.00"), "x").getMensaje());
        assertEquals(MensajesMonedero.CUENTA_INVALIDA, monedero.reintegrar("", d("1.00"), "x").getMensaje());
    }

    // ------------------------------------------------------------ exactitud y aislamiento
    @Test
    @DisplayName("Dado tres recargas de 0.10, cuando se suman, entonces el saldo es exactamente 0.30 (sin errores de double)")
    void elDineroNoTieneErroresDeRedondeo() {
        for (int i = 0; i < 3; i++) {
            monedero.acreditar(ANA, d("0.10"), "Recarga", "10000000000" + i);
        }
        assertEquals(d("0.30"), monedero.saldo(ANA));
    }

    @Test
    @DisplayName("Dado un monto con más de 2 decimales, cuando se acredita, entonces se redondea a 2 decimales (HALF_UP)")
    void losMontosSeRedondeanADosDecimales() {
        monedero.acreditar(ANA, d("1.005"), "Recarga", "111111111111");
        assertEquals(d("1.01"), monedero.saldo(ANA));
    }

    @Test
    @DisplayName("Dado dos usuarios, cuando uno recarga, entonces el otro no ve cambios en su saldo")
    void cadaUsuarioTieneSuPropiaCuenta() {
        monedero.acreditar(ANA, d("20.00"), "Recarga", "111111111111");
        assertEquals(d("0.00"), monedero.saldo("luis@ucv.com"));
        assertTrue(monedero.historial("luis@ucv.com", 10).isEmpty());
    }

    @Test
    @DisplayName("Dado el mismo correo escrito con mayúsculas y espacios, cuando se consulta, entonces es la misma cuenta")
    void elCorreoNoDistingueMayusculasNiEspacios() {
        monedero.acreditar("  ANA@ucv.com ", d("9.00"), "Recarga", "111111111111");
        assertEquals(d("9.00"), monedero.saldo(ANA));
    }

    // ------------------------------------------------------------ historial
    @Test
    @DisplayName("Dado varios movimientos, cuando se pide el historial, entonces sale del más reciente al más antiguo y respeta el máximo")
    void elHistorialSaleDelMasRecienteAlMasAntiguoYConLimite() {
        monedero.acreditar(ANA, d("40.00"), "Primera", "111111111111");
        monedero.debitar(ANA, d("10.00"), "Segunda");
        monedero.reintegrar(ANA, d("10.00"), "Tercera");

        List<Movimiento> ultimos = monedero.historial(ANA, 2);

        assertEquals(2, ultimos.size());
        assertEquals("Tercera", ultimos.get(0).getDescripcion());
        assertEquals("Segunda", ultimos.get(1).getDescripcion());
        assertEquals(3, monedero.historial(ANA, 100).size());
        assertTrue(monedero.historial(ANA, 0).isEmpty());
        assertTrue(monedero.historial(ANA, -3).isEmpty());
    }

    // ------------------------------------------------------------ referencia de pago repetida
    @Test
    @DisplayName("Dado una referencia ya usada, cuando se intenta acreditar otra recarga con ella, entonces se rechaza y no se anota nada")
    void unaReferenciaRepetidaSeRechaza() {
        monedero.acreditar(ANA, d("10.00"), "Recarga", "123456789012");

        Resultado<Void> r = monedero.acreditar("luis@ucv.com", d("10.00"), "Recarga", " 123456789012 ");

        assertFalse(r.isOk());
        assertEquals(MensajesMonedero.REFERENCIA_REPETIDA, r.getMensaje());
        assertEquals(d("0.00"), monedero.saldo("luis@ucv.com"));
        assertEquals(d("10.00"), monedero.saldo(ANA));
    }

    @Test
    @DisplayName("Dado 50 recargas simultáneas con la misma referencia, cuando se ejecutan a la vez, entonces solo una se acredita")
    void cincuentaRecargasConLaMismaReferenciaSoloAcreditanUna() throws Exception {
        // Con el repositorio lento, sin comprobar y abonar dentro del mismo bloqueo pasarían todas.
        ServicioMonedero lento = new ServicioMonedero(new RepositorioLento(), RELOJ);

        int exitos = ejecutarEnParalelo(50, () -> lento.acreditar(ANA, d("5.00"), "Recarga", "555555555555").isOk());

        assertEquals(1, exitos);
        assertEquals(d("5.00"), lento.saldo(ANA));
    }

    // ------------------------------------------------------------ integridad con solicitudes simultáneas
    @Test
    @DisplayName("Dado 100 cobros simultáneos de 1.00 sobre un saldo de 40.00, cuando se ejecutan a la vez, entonces solo 40 tienen éxito y el saldo nunca es negativo")
    void cienCobrosSimultaneosNoGastanMasDeLoQueHay() throws Exception {
        // Repositorio lento: ensancha el hueco entre "comprobar el saldo" y "anotar el cobro". Sin el bloqueo del
        // servicio, todos los hilos verían saldo suficiente y gastarían de más (verificado quitando el bloqueo).
        ServicioMonedero lento = new ServicioMonedero(new RepositorioLento(), RELOJ);
        lento.acreditar(ANA, d("40.00"), "Recarga", "111111111111");

        int exitos = ejecutarEnParalelo(100, () -> lento.debitar(ANA, d("1.00"), "Reserva").isOk());

        assertEquals(40, exitos);
        assertEquals(d("0.00"), lento.saldo(ANA));
        assertEquals(41, lento.historial(ANA, 1000).size()); // 1 recarga + 40 cobros
    }

    /** Repositorio que tarda 1 ms en cada consulta, como lo haría un disco lento. */
    private static class RepositorioLento extends RepositorioMovimientos {
        @Override
        public List<Movimiento> listarPorUsuario(String correo) {
            List<Movimiento> lectura = super.listarPorUsuario(correo);
            demorar();
            return lectura;
        }

        @Override
        public boolean existeReferencia(String referencia) {
            boolean existe = super.existeReferencia(referencia);
            demorar();
            return existe;
        }

        private static void demorar() {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Test
    @DisplayName("Dado 100 recargas simultáneas de 1.00, cuando se ejecutan a la vez, entonces no se pierde ninguna")
    void cienRecargasSimultaneasNoPierdenNinguna() throws Exception {
        int exitos = ejecutarEnParalelo(100, () -> monedero.acreditar(ANA, d("1.00"), "Recarga", null).isOk());

        assertEquals(100, exitos);
        assertEquals(d("100.00"), monedero.saldo(ANA));
    }

    /** Lanza N tareas que arrancan al mismo tiempo y devuelve cuántas devolvieron true. */
    private static int ejecutarEnParalelo(int n, java.util.concurrent.Callable<Boolean> tarea) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(n);
        CountDownLatch salida = new CountDownLatch(1);
        List<Future<Boolean>> resultados = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            resultados.add(pool.submit(() -> {
                salida.await();
                return tarea.call();
            }));
        }
        salida.countDown();
        int exitos = 0;
        for (Future<Boolean> f : resultados) {
            if (f.get(20, TimeUnit.SECONDS)) {
                exitos++;
            }
        }
        pool.shutdown();
        return exitos;
    }
}
