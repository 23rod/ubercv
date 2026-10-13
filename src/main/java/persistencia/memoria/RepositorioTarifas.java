package persistencia.memoria;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import modelo.Rol;
import persistencia.interfaces.IRepositorioTarifas;

/**
 * Tarifas en memoria. Con una ruta de archivo también las guarda y las recupera entre arranques.
 * Formato del archivo (una línea por dato, separado por tabulador porque los nombres de ruta pueden llevar comas):
 *   BASE [tab] nombre de ruta en minúsculas [tab] monto
 *   PCT  [tab] ROL [tab] porcentaje
 */
public class RepositorioTarifas implements IRepositorioTarifas {

    private final Map<String, BigDecimal> basePorRuta = new LinkedHashMap<>();
    private final Map<Rol, BigDecimal> porcentajes = new EnumMap<>(Rol.class);
    private final Path archivo; // null = solo memoria (lo usan las pruebas)

    /** Solo en memoria: no lee ni escribe archivos (lo usan las pruebas). */
    public RepositorioTarifas() {
        this(null);
    }

    /** Guarda en el archivo indicado. Si el archivo no existe, empieza vacío. */
    public RepositorioTarifas(Path archivo) {
        this.archivo = archivo;
        cargar();
    }

    private static String clave(String nombreRuta) {
        return nombreRuta.trim().toLowerCase();
    }

    @Override
    public BigDecimal obtenerTarifaBase(String nombreRuta) {
        return (nombreRuta == null) ? null : basePorRuta.get(clave(nombreRuta));
    }

    @Override
    public void guardarTarifaBase(String nombreRuta, BigDecimal monto) {
        basePorRuta.put(clave(nombreRuta), monto);
        escribir();
    }

    @Override
    public void moverTarifaBase(String nombreAnterior, String nombreNuevo) {
        BigDecimal monto = basePorRuta.remove(clave(nombreAnterior));
        if (monto != null) {
            basePorRuta.put(clave(nombreNuevo), monto);
            escribir();
        }
    }

    @Override
    public BigDecimal obtenerPorcentaje(Rol rol) {
        return porcentajes.get(rol);
    }

    @Override
    public void guardarPorcentaje(Rol rol, BigDecimal porcentaje) {
        porcentajes.put(rol, porcentaje);
        escribir();
    }

    private void cargar() {
        if (archivo == null || !Files.exists(archivo)) {
            return;
        }
        try {
            for (String linea : Files.readAllLines(archivo, StandardCharsets.UTF_8)) {
                String[] partes = linea.split("\t");
                if (partes.length < 3) {
                    continue;
                }
                try {
                    if (partes[0].equals("BASE")) {
                        basePorRuta.put(partes[1], new BigDecimal(partes[2]));
                    } else if (partes[0].equals("PCT")) {
                        Rol rol = AlmacenArchivo.enumONulo(Rol.class, partes[1]);
                        if (rol != null) {
                            porcentajes.put(rol, new BigDecimal(partes[2]));
                        }
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Advertencia: monto no válido en la línea: " + linea);
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer las tarifas: " + archivo);
        }
    }

    private void escribir() {
        if (archivo == null) {
            return;
        }
        try {
            List<String> lineas = new ArrayList<>();
            for (Map.Entry<String, BigDecimal> e : basePorRuta.entrySet()) {
                lineas.add("BASE\t" + e.getKey() + "\t" + e.getValue().toPlainString());
            }
            for (Map.Entry<Rol, BigDecimal> e : porcentajes.entrySet()) {
                lineas.add("PCT\t" + e.getKey().name() + "\t" + e.getValue().toPlainString());
            }
            AlmacenArchivo.escribirAtomico(archivo, lineas);
        } catch (IOException e) {
            System.err.println("Error al escribir las tarifas: " + archivo);
        }
    }
}
