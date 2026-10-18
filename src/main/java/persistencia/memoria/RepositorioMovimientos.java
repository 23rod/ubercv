package persistencia.memoria;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import modelo.Movimiento;
import modelo.TipoMovimiento;
import persistencia.interfaces.IRepositorioMovimientos;

/**
 * Historial del monedero en memoria. Con una ruta de archivo también lo guarda y lo recupera entre arranques.
 * Formato (una línea por movimiento, separado por tabulador):
 *   correo [tab] TIPO [tab] monto [tab] fecha ISO [tab] referencia [tab] descripción
 */
public class RepositorioMovimientos implements IRepositorioMovimientos {

    private final List<Movimiento> movimientos = new ArrayList<>();
    private final Path archivo; // null = solo memoria (lo usan las pruebas)

    /** Solo en memoria: no lee ni escribe archivos (lo usan las pruebas). */
    public RepositorioMovimientos() {
        this(null);
    }

    /** Guarda en el archivo indicado. Si el archivo no existe, empieza vacío. */
    public RepositorioMovimientos(Path archivo) {
        this.archivo = archivo;
        cargar();
    }

    private static String clave(String correo) {
        return correo.trim().toLowerCase();
    }

    @Override
    public synchronized void agregar(Movimiento movimiento) {
        if (movimiento == null) {
            throw new IllegalArgumentException("El movimiento es obligatorio.");
        }
        movimientos.add(movimiento);
        try {
            escribir();
        } catch (RuntimeException e) {
            movimientos.remove(movimiento);
            throw e;
        }
    }

    @Override
    public synchronized List<Movimiento> listarPorUsuario(String correo) {
        List<Movimiento> resultado = new ArrayList<>();
        if (correo == null) {
            return resultado;
        }
        for (Movimiento m : movimientos) {
            if (clave(m.getCorreo()).equals(clave(correo))) {
                resultado.add(m);
            }
        }
        return resultado;
    }

    @Override
    public synchronized boolean existeReferencia(String referencia) {
        if (referencia == null || referencia.trim().isEmpty()) {
            return false;
        }
        for (Movimiento m : movimientos) {
            if (m.tieneReferencia() && m.getReferencia().equals(referencia.trim())) {
                return true;
            }
        }
        return false;
    }

    private void cargar() {
        if (archivo == null || !Files.exists(archivo)) {
            return;
        }
        try {
            for (String linea : Files.readAllLines(archivo, StandardCharsets.UTF_8)) {
                String[] p = linea.split("\t", -1); // -1 conserva los campos vacíos del final
                if (p.length < 6) {
                    continue;
                }
                TipoMovimiento tipo = AlmacenArchivo.enumONulo(TipoMovimiento.class, p[1]);
                if (tipo == null) {
                    System.err.println("Advertencia: tipo de movimiento no válido en la línea: " + linea);
                    continue;
                }
                try {
                    movimientos.add(new Movimiento(p[0], tipo, new BigDecimal(p[2]), LocalDateTime.parse(p[3]), p[5], p[4]));
                } catch (NumberFormatException | DateTimeParseException e) {
                    System.err.println("Advertencia: movimiento dañado en la línea: " + linea);
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer los movimientos: " + archivo);
        }
    }

    private void escribir() {
        if (archivo == null) {
            return;
        }
        try {
            List<String> lineas = new ArrayList<>();
            for (Movimiento m : movimientos) {
                lineas.add(String.join("\t",
                        m.getCorreo(), m.getTipo().name(), m.getMonto().toPlainString(), m.getFecha().toString(),
                        m.getReferencia(), limpiar(m.getDescripcion())));
            }
            AlmacenArchivo.escribirAtomico(archivo, lineas);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudieron persistir los movimientos en " + archivo, e);
        }
    }

    /** Un tabulador o un salto de línea en la descripción rompería el formato del archivo. */
    private static String limpiar(String texto) {
        return texto.replaceAll("[\\t\\r\\n]+", " ");
    }
}
