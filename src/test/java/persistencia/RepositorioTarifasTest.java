package persistencia;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import modelo.Rol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import persistencia.memoria.RepositorioTarifas;

/** Las tarifas se guardan y se recuperan entre arranques; las pruebas usan una carpeta temporal. */
class RepositorioTarifasTest {

    @Test
    @DisplayName("Dado un archivo que no existe, cuando se crea el repositorio, entonces no hay tarifas ni porcentajes")
    void sinArchivoEmpiezaVacio(@TempDir Path carpeta) {
        RepositorioTarifas repo = new RepositorioTarifas(carpeta.resolve("tarifas.txt"));
        assertNull(repo.obtenerTarifaBase("UCV - Guarenas"));
        assertNull(repo.obtenerPorcentaje(Rol.ESTUDIANTE));
    }

    @Test
    @DisplayName("Dado una tarifa y un porcentaje guardados, cuando se reabre el repositorio, entonces siguen ahí")
    void lasTarifasSobrevivenAUnReinicio(@TempDir Path carpeta) {
        Path archivo = carpeta.resolve("datos").resolve("tarifas.txt");
        RepositorioTarifas repo = new RepositorioTarifas(archivo);
        repo.guardarTarifaBase("UCV - Guarenas", new BigDecimal("12.50"));
        repo.guardarPorcentaje(Rol.ESTUDIANTE, new BigDecimal("18.00"));

        RepositorioTarifas leido = new RepositorioTarifas(archivo);

        assertEquals(new BigDecimal("12.50"), leido.obtenerTarifaBase("ucv - guarenas")); // sin distinguir mayúsculas
        assertEquals(new BigDecimal("18.00"), leido.obtenerPorcentaje(Rol.ESTUDIANTE));
        assertNull(leido.obtenerPorcentaje(Rol.EMPLEADO));
    }

    @Test
    @DisplayName("Dado una tarifa, cuando se mueve a otro nombre de ruta, entonces la tarifa sigue a la ruta y se conserva al reiniciar")
    void laTarifaSigueALaRutaRenombrada(@TempDir Path carpeta) {
        Path archivo = carpeta.resolve("tarifas.txt");
        RepositorioTarifas repo = new RepositorioTarifas(archivo);
        repo.guardarTarifaBase("Ruta A", new BigDecimal("5.00"));

        repo.moverTarifaBase("Ruta A", "Ruta Nueva");

        RepositorioTarifas leido = new RepositorioTarifas(archivo);
        assertEquals(new BigDecimal("5.00"), leido.obtenerTarifaBase("Ruta Nueva"));
        assertNull(leido.obtenerTarifaBase("Ruta A"));
    }

    @Test
    @DisplayName("Dado una línea dañada en el archivo, cuando se carga, entonces se ignora y el resto se conserva")
    void unaLineaDanadaNoRompeLaCarga(@TempDir Path carpeta) throws Exception {
        Path archivo = carpeta.resolve("tarifas.txt");
        Files.write(archivo, java.util.List.of("BASE\truta buena\t7.00", "BASE\truta mala\tabc", "basura sin tabuladores"));

        RepositorioTarifas repo = new RepositorioTarifas(archivo);

        assertEquals(new BigDecimal("7.00"), repo.obtenerTarifaBase("Ruta Buena"));
        assertNull(repo.obtenerTarifaBase("Ruta Mala"));
    }

    @Test
    @DisplayName("Dado un guardado, cuando termina, entonces no queda ningún archivo temporal")
    void noQuedaArchivoTemporal(@TempDir Path carpeta) throws Exception {
        new RepositorioTarifas(carpeta.resolve("tarifas.txt")).guardarTarifaBase("Ruta A", new BigDecimal("1.00"));
        try (var archivos = Files.list(carpeta)) {
            assertTrue(archivos.noneMatch(p -> p.getFileName().toString().endsWith(".tmp")));
        }
    }
}
