package persistencia;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import modelo.Movimiento;
import modelo.TipoMovimiento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import persistencia.memoria.RepositorioMovimientos;

/** El historial del monedero se guarda y se recupera entre arranques; las pruebas usan una carpeta temporal. */
class RepositorioMovimientosTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 10, 14, 11, 30, 15, 123_000_000);

    private static Movimiento mov(String correo, TipoMovimiento tipo, String monto, String descripcion, String referencia) {
        return new Movimiento(correo, tipo, new BigDecimal(monto), FECHA, descripcion, referencia);
    }

    @Test
    @DisplayName("Dado un archivo que no existe, cuando se crea el repositorio, entonces el historial está vacío")
    void sinArchivoEmpiezaVacio(@TempDir Path carpeta) {
        RepositorioMovimientos repo = new RepositorioMovimientos(carpeta.resolve("movimientos.txt"));
        assertTrue(repo.listarPorUsuario("ana@ucv.com").isEmpty());
        assertFalse(repo.existeReferencia("123456789012"));
    }

    @Test
    @DisplayName("Dado movimientos guardados, cuando se reabre el repositorio, entonces siguen ahí con todos sus datos y en el mismo orden")
    void elHistorialSobreviveAUnReinicio(@TempDir Path carpeta) {
        Path archivo = carpeta.resolve("datos").resolve("movimientos.txt");
        RepositorioMovimientos repo = new RepositorioMovimientos(archivo);
        repo.agregar(mov("ana@ucv.com", TipoMovimiento.RECARGA, "25.50", "Recarga por pago móvil", "123456789012"));
        repo.agregar(mov("ana@ucv.com", TipoMovimiento.COBRO, "12.25", "Reserva UCV - Guarenas", null));

        List<Movimiento> leidos = new RepositorioMovimientos(archivo).listarPorUsuario("ANA@ucv.com");

        assertEquals(2, leidos.size());
        assertEquals(TipoMovimiento.RECARGA, leidos.get(0).getTipo());
        assertEquals(new BigDecimal("25.50"), leidos.get(0).getMonto());
        assertEquals("123456789012", leidos.get(0).getReferencia());
        assertEquals(FECHA, leidos.get(0).getFecha());
        assertEquals("Recarga por pago móvil", leidos.get(0).getDescripcion());
        assertEquals(TipoMovimiento.COBRO, leidos.get(1).getTipo());
        assertFalse(leidos.get(1).tieneReferencia());
    }

    @Test
    @DisplayName("Dado una descripción con tabulador o salto de línea, cuando se guarda, entonces no rompe el archivo")
    void unaDescripcionConCaracteresDeControlNoRompeElFormato(@TempDir Path carpeta) {
        Path archivo = carpeta.resolve("movimientos.txt");
        new RepositorioMovimientos(archivo)
                .agregar(mov("ana@ucv.com", TipoMovimiento.REINTEGRO, "5.00", "Cancelación\tde\nreserva", null));

        List<Movimiento> leidos = new RepositorioMovimientos(archivo).listarPorUsuario("ana@ucv.com");

        assertEquals(1, leidos.size());
        assertEquals("Cancelación de reserva", leidos.get(0).getDescripcion());
    }

    @Test
    @DisplayName("Dado una referencia ya usada, cuando se consulta, entonces se detecta aunque sea de otro usuario y tras reiniciar")
    void laReferenciaUsadaSeDetecta(@TempDir Path carpeta) {
        Path archivo = carpeta.resolve("movimientos.txt");
        new RepositorioMovimientos(archivo)
                .agregar(mov("ana@ucv.com", TipoMovimiento.RECARGA, "10.00", "Recarga", "123456789012"));

        RepositorioMovimientos repo = new RepositorioMovimientos(archivo);

        assertTrue(repo.existeReferencia("123456789012"));
        assertTrue(repo.existeReferencia(" 123456789012 "));
        assertFalse(repo.existeReferencia("999999999999"));
        assertFalse(repo.existeReferencia(null));
        assertFalse(repo.existeReferencia(""));
    }

    @Test
    @DisplayName("Dado una línea dañada en el archivo, cuando se carga, entonces se ignora y el resto se conserva")
    void unaLineaDanadaNoRompeLaCarga(@TempDir Path carpeta) throws Exception {
        Path archivo = carpeta.resolve("movimientos.txt");
        Files.write(archivo, List.of(
                "ana@ucv.com\tRECARGA\t10.00\t2026-10-14T11:30:15\t111111111111\tbuena",
                "ana@ucv.com\tRECARGA\tabc\t2026-10-14T11:30:15\t\tmonto malo",
                "ana@ucv.com\tINVENTADO\t1.00\t2026-10-14T11:30:15\t\ttipo malo",
                "ana@ucv.com\tRECARGA\t1.00\tno-es-fecha\t\tfecha mala",
                "basura"));

        List<Movimiento> leidos = new RepositorioMovimientos(archivo).listarPorUsuario("ana@ucv.com");

        assertEquals(1, leidos.size());
        assertEquals("buena", leidos.get(0).getDescripcion());
    }

    @Test
    @DisplayName("Dado un guardado, cuando termina, entonces no queda ningún archivo temporal")
    void noQuedaArchivoTemporal(@TempDir Path carpeta) throws Exception {
        new RepositorioMovimientos(carpeta.resolve("movimientos.txt"))
                .agregar(mov("ana@ucv.com", TipoMovimiento.RECARGA, "1.00", "Recarga", null));
        try (var archivos = Files.list(carpeta)) {
            assertTrue(archivos.noneMatch(p -> p.getFileName().toString().endsWith(".tmp")));
        }
    }
}
