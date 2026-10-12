package persistencia.memoria;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Escritura segura de archivos de texto para los repositorios.
 * Escribe primero en un archivo temporal y luego lo renombra: si el programa se cierra a mitad
 * de la escritura, el archivo original queda intacto (nunca a medias).
 */
final class AlmacenArchivo {

    private AlmacenArchivo() {
    }

    /**
     * Lee un valor de enum guardado por nombre (ej. "ADMIN_TRANSPORTE"). Devuelve null si no existe.
     * Los repositorios guardan el nombre técnico del valor, no la etiqueta que se ve en pantalla.
     */
    static <E extends Enum<E>> E enumONulo(Class<E> tipo, String texto) {
        if (texto == null) {
            return null;
        }
        try {
            return Enum.valueOf(tipo, texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    static void escribirAtomico(Path destino, List<String> lineas) throws IOException {
        Path carpeta = destino.toAbsolutePath().getParent();
        if (carpeta != null) {
            Files.createDirectories(carpeta);
        }
        Path temporal = destino.resolveSibling(destino.getFileName() + ".tmp");
        Files.write(temporal, lineas, StandardCharsets.UTF_8);
        try {
            Files.move(temporal, destino, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
