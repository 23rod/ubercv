package persistencia.memoria;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import modelo.RecargaPagoMovil;
import persistencia.interfaces.IRepositorioRecargas;

/**
 * Registro de operaciones de pago móvil en memoria. Con una ruta de archivo también las guarda y las recupera entre arranques.
 * Formato (una línea por operación, separado por tabulador):
 *   correo, fecha ISO, banco origen, banco destino, cédula, teléfono, referencia, monto, EXITOSA o FALLIDA, detalle
 */
public class RepositorioRecargas implements IRepositorioRecargas {

    private static final int CAMPOS = 10;
    private static final int LARGO_MAXIMO_CAMPO = 80;

    private final List<RecargaPagoMovil> recargas = new ArrayList<>();
    private final Path archivo; // null = solo memoria (lo usan las pruebas)

    /** Solo en memoria: no lee ni escribe archivos (lo usan las pruebas). */
    public RepositorioRecargas() {
        this(null);
    }

    /** Guarda en el archivo indicado. Si el archivo no existe, empieza vacío. */
    public RepositorioRecargas(Path archivo) {
        this.archivo = archivo;
        cargar();
    }

    private static String clave(String correo) {
        return correo.trim().toLowerCase();
    }

    @Override
    public synchronized void agregar(RecargaPagoMovil recarga) {
        recargas.add(recarga);
        escribir();
    }

    @Override
    public synchronized List<RecargaPagoMovil> listarPorUsuario(String correo) {
        List<RecargaPagoMovil> resultado = new ArrayList<>();
        if (correo == null) {
            return resultado;
        }
        for (RecargaPagoMovil r : recargas) {
            if (clave(r.getCorreo()).equals(clave(correo))) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    @Override
    public synchronized List<String> listarCorreos() {
        TreeSet<String> correos = new TreeSet<>();
        for (RecargaPagoMovil r : recargas) {
            correos.add(clave(r.getCorreo()));
        }
        return new ArrayList<>(correos);
    }

    private void cargar() {
        if (archivo == null || !Files.exists(archivo)) {
            return;
        }
        try {
            for (String linea : Files.readAllLines(archivo, StandardCharsets.UTF_8)) {
                String[] p = linea.split("\t", -1); // -1 conserva los campos vacíos del final
                if (p.length < CAMPOS) {
                    continue;
                }
                try {
                    recargas.add(new RecargaPagoMovil(p[0], LocalDateTime.parse(p[1]), p[2], p[3], p[4], p[5], p[6], p[7],
                            p[8].equals("EXITOSA"), p[9]));
                } catch (DateTimeParseException e) {
                    System.err.println("Advertencia: operación dañada en la línea: " + linea);
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer las recargas: " + archivo);
        }
    }

    private void escribir() {
        if (archivo == null) {
            return;
        }
        try {
            List<String> lineas = new ArrayList<>();
            for (RecargaPagoMovil r : recargas) {
                lineas.add(String.join("\t",
                        limpiar(r.getCorreo()), r.getFecha().toString(), limpiar(r.getBancoOrigen()), limpiar(r.getBancoDestino()),
                        limpiar(r.getCedula()), limpiar(r.getTelefono()), limpiar(r.getReferencia()), limpiar(r.getMonto()),
                        r.isExitosa() ? "EXITOSA" : "FALLIDA", limpiar(r.getDetalle())));
            }
            AlmacenArchivo.escribirAtomico(archivo, lineas);
        } catch (IOException e) {
            System.err.println("Error al escribir las recargas: " + archivo);
        }
    }

    /** Un tabulador o salto de línea rompería el formato; además se acota el largo de lo que el usuario escribió. */
    private static String limpiar(String texto) {
        String limpio = (texto == null) ? "" : texto.replaceAll("[\\t\\r\\n]+", " ");
        return limpio.length() > LARGO_MAXIMO_CAMPO ? limpio.substring(0, LARGO_MAXIMO_CAMPO) : limpio;
    }
}
