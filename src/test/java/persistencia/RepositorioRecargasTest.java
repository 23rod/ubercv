package persistencia;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import modelo.RecargaPagoMovil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import persistencia.memoria.RepositorioRecargas;

/** El registro de operaciones de pago móvil se guarda y se recupera entre arranques; las pruebas usan una carpeta temporal. */
class RepositorioRecargasTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 10, 14, 11, 30, 15, 123_000_000);

    private static RecargaPagoMovil op(String correo, String referencia, boolean exitosa, String detalle) {
        return new RecargaPagoMovil(correo, FECHA, "0169 - R4, Banco Microfinanciero, C.A.", "0134 - Banesco",
                "12345678", "04121234567", referencia, "25.50", exitosa, detalle);
    }

    @Test
    @DisplayName("Dado un archivo que no existe, cuando se crea el repositorio, entonces no hay operaciones ni usuarios")
    void sinArchivoEmpiezaVacio(@TempDir Path carpeta) {
        RepositorioRecargas repo = new RepositorioRecargas(carpeta.resolve("recargas.txt"));
        assertTrue(repo.listarPorUsuario("ana@ucv.com").isEmpty());
        assertTrue(repo.listarCorreos().isEmpty());
    }

    @Test
    @DisplayName("Dado operaciones guardadas, cuando se reabre el repositorio, entonces siguen ahí con todos sus datos y en el mismo orden")
    void lasOperacionesSobrevivenAUnReinicio(@TempDir Path carpeta) {
        Path archivo = carpeta.resolve("datos").resolve("recargas.txt");
        RepositorioRecargas repo = new RepositorioRecargas(archivo);
        repo.agregar(op("ana@ucv.com", "123456789012", true, "Recarga exitosa"));
        repo.agregar(op("ana@ucv.com", "111111", false, "Recarga fallida, datos inválidos"));

        List<RecargaPagoMovil> leidas = new RepositorioRecargas(archivo).listarPorUsuario("ANA@ucv.com");

        assertEquals(2, leidas.size());
        RecargaPagoMovil primera = leidas.get(0);
        assertTrue(primera.isExitosa());
        assertEquals("0169 - R4, Banco Microfinanciero, C.A.", primera.getBancoOrigen()); // con comas
        assertEquals("0134 - Banesco", primera.getBancoDestino());
        assertEquals("12345678", primera.getCedula());
        assertEquals("04121234567", primera.getTelefono());
        assertEquals("123456789012", primera.getReferencia());
        assertEquals("25.50", primera.getMonto());
        assertEquals(FECHA, primera.getFecha());
        assertEquals("Recarga exitosa", primera.getDetalle());
        assertFalse(leidas.get(1).isExitosa());
        assertEquals("Recarga fallida, datos inválidos", leidas.get(1).getDetalle());
    }

    @Test
    @DisplayName("Dado operaciones de varios usuarios, cuando se listan los correos, entonces salen sin repetir y en orden alfabético")
    void losCorreosSalenSinRepetirYOrdenados(@TempDir Path carpeta) {
        RepositorioRecargas repo = new RepositorioRecargas(carpeta.resolve("recargas.txt"));
        repo.agregar(op("luis@ucv.com", "100000", true, "ok"));
        repo.agregar(op("ANA@ucv.com", "200000", true, "ok"));
        repo.agregar(op("ana@ucv.com", "300000", true, "ok"));

        assertEquals(List.of("ana@ucv.com", "luis@ucv.com"), repo.listarCorreos());
        assertEquals(2, repo.listarPorUsuario("ana@ucv.com").size());
        assertEquals(1, repo.listarPorUsuario("luis@ucv.com").size());
    }

    @Test
    @DisplayName("Dado datos con tabulador, salto de línea o demasiado largos, cuando se guardan, entonces no rompen el archivo")
    void lasEntradasRarasNoRompenElFormato(@TempDir Path carpeta) {
        Path archivo = carpeta.resolve("recargas.txt");
        String largo = "9".repeat(500);
        new RepositorioRecargas(archivo).agregar(new RecargaPagoMovil("ana@ucv.com", FECHA, "banco\tcon\ttabs", "0134 - Banesco",
                "12\n34", "0412", largo, "abc", false, "Recarga fallida, datos inválidos"));

        List<RecargaPagoMovil> leidas = new RepositorioRecargas(archivo).listarPorUsuario("ana@ucv.com");

        assertEquals(1, leidas.size());
        assertEquals("banco con tabs", leidas.get(0).getBancoOrigen());
        assertEquals("12 34", leidas.get(0).getCedula());
        assertEquals(80, leidas.get(0).getReferencia().length());
    }

    @Test
    @DisplayName("Dado una línea dañada en el archivo, cuando se carga, entonces se ignora y el resto se conserva")
    void unaLineaDanadaNoRompeLaCarga(@TempDir Path carpeta) throws Exception {
        Path archivo = carpeta.resolve("recargas.txt");
        Files.write(archivo, List.of(
                "ana@ucv.com\t2026-10-14T11:30:15\tb1\tb2\t123\t0412\t111111\t10.00\tEXITOSA\tbuena",
                "ana@ucv.com\tno-es-fecha\tb1\tb2\t123\t0412\t111111\t10.00\tEXITOSA\tfecha mala",
                "basura sin tabuladores"));

        List<RecargaPagoMovil> leidas = new RepositorioRecargas(archivo).listarPorUsuario("ana@ucv.com");

        assertEquals(1, leidas.size());
        assertEquals("buena", leidas.get(0).getDetalle());
    }

    @Test
    @DisplayName("Dado un guardado, cuando termina, entonces no queda ningún archivo temporal")
    void noQuedaArchivoTemporal(@TempDir Path carpeta) throws Exception {
        new RepositorioRecargas(carpeta.resolve("recargas.txt")).agregar(op("ana@ucv.com", "111111", true, "ok"));
        try (var archivos = Files.list(carpeta)) {
            assertTrue(archivos.noneMatch(p -> p.getFileName().toString().endsWith(".tmp")));
        }
    }
}
