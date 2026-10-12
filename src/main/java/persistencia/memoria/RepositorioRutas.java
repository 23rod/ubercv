package persistencia.memoria;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import modelo.Ruta;
import java.time.LocalTime;
import modelo.TipoRuta;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import persistencia.interfaces.IRepositorioRutas; // Importante

/** Rutas en memoria. Con una ruta de archivo también las guarda y las recupera entre arranques. */
public class RepositorioRutas implements IRepositorioRutas {

    private final Map<String, Ruta> porNombre = new LinkedHashMap<>();
    private final Path archivo;

    /** Solo en memoria: no lee ni escribe archivos (lo usan las pruebas). */
    public RepositorioRutas() {
        this(null);
    }

    /** Guarda en el archivo indicado. Si el archivo no existe, empieza vacío. */
    public RepositorioRutas(Path archivo) {
        this.archivo = archivo;
        cargarRutasDesdeArchivo();
    }

    private void cargarRutasDesdeArchivo() {        
        if (archivo == null || !Files.exists(archivo)) {
            return;
        }        
        try {
            List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
            if (lineas.isEmpty()) {
                return;
            }
            for (String linea : lineas) {
                if (linea.isEmpty() || linea.startsWith("nombre,")) {
                    continue;
                }
                String[] partes = linea.split(",");
                if (partes.length < 4) continue;
                
                String nombre = partes[0].trim();
                TipoRuta tipo = AlmacenArchivo.enumONulo(TipoRuta.class, partes[1]);
                if (tipo == null) {
                    tipo = TipoRuta.desdeTexto(partes[1]);
                }
                LocalTime inicioJornada = LocalTime.parse(partes[2].trim());
                LocalTime finJornada = LocalTime.parse(partes[3].trim());

                Ruta ruta = new Ruta(nombre, tipo, inicioJornada, finJornada);          
                porNombre.put(clave(ruta.getNombre()), ruta);
            }
        } catch (IOException e) {
            System.err.println("Error al cargar rutas desde el archivo: " + e.getMessage());
        }
    }

    private void reescribirArchivo() {
        if (archivo == null) {
            return;
        }
        try {
            List<String> lineasAEscribir = new ArrayList<>();
            for (Ruta ruta : porNombre.values()) {
                String linea = String.format("%s,%s,%s,%s",
                    ruta.getNombre(),
                    ruta.getTipo().toString(),
                    ruta.getInicioJornada().toString(),
                    ruta.getFinJornada().toString());
                lineasAEscribir.add(linea);
            }
            AlmacenArchivo.escribirAtomico(archivo, lineasAEscribir);
        } catch (IOException e) {
            System.err.println("Error al escribir rutas en el archivo: " + e.getMessage());
        }
    }

    private static String clave(String nombre) {
        return nombre.trim().toLowerCase();
    }

    @Override
    public void guardar(Ruta ruta) {
        porNombre.put(clave(ruta.getNombre()), ruta);
        reescribirArchivo();
    }

    @Override
    public void eliminar(String nombre) {
        if (nombre != null) {
            porNombre.remove(clave(nombre));
            reescribirArchivo();
        }
    }

    @Override
    public Ruta buscarPorNombre(String nombre) {
        return (nombre == null) ? null : porNombre.get(clave(nombre));
    }

    @Override
    public boolean existeNombre(String nombre) {
        return buscarPorNombre(nombre) != null;
    }

    @Override
    public List<Ruta> listar() {
        return new ArrayList<>(porNombre.values());
    }
}