package persistencia.memoria;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import modelo.Ruta;
import java.time.LocalTime;
import modelo.TipoRuta;

//import java.io.BufferedWriter;
//import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** "Base de datos" de rutas en memoria. El nombre no distingue mayúsculas de minúsculas. */
public class RepositorioRutas {

    private final Map<String, Ruta> porNombre = new LinkedHashMap<>();
    //Lectura y escritura de rutas en archivo de texto
    private static final String ARCHIVO_RUTAS = "C:\\proyecto_transporte\\ubercv\\src\\main\\java\\persistencia\\memoria\\rutasAlmacenadas.txt";
    

    public RepositorioRutas(){
        // Cargar rutas desde el archivo al iniciar el repositorio
        cargarRutasDesdeArchivo();
        //Se limpian duplicados y se reescribe el archivo para mantenerlo limpio s
        reescribirArchivo(); // Guardar en archivo al iniciar el repositorio
    }


    private void cargarRutasDesdeArchivo() {        
        Path archivoRutas = Path.of(ARCHIVO_RUTAS);
        if (!Files.exists(archivoRutas)) {
            return; // Si el archivo no existe, no hacemos nada
        }        
        try 
        {
            List<String> lineas = Files.readAllLines(archivoRutas, StandardCharsets.UTF_8);
            if (lineas.isEmpty()) {
                //System.out.println("El archivo de rutas está vacío.");
                return;
            }
            for (String linea : lineas) {
                //Extraemos los datos de la ruta del archivo de texto separados por comas
                if (linea.isEmpty() || linea.startsWith("nombre,")) {
                    continue;
                }
                String[] partes = linea.split(",");
                if (partes.length < 4) continue;
                //Creamos la ruta con los datos extraidos del archivo de texto
                //-------------------Nombre-------------------TipoRuta------------------InicioJornada-----------FinJornada---------
                String nombre = partes[0].trim();
                TipoRuta tipo = TipoRuta.desdeTexto(partes[1].trim());
                LocalTime inicioJornada = LocalTime.parse(partes[2].trim());
                LocalTime finJornada = LocalTime.parse(partes[3].trim());

                Ruta ruta = new Ruta( nombre, tipo, inicioJornada, finJornada);          
                //Agregamos la ruta al repositorio en memoria                 
                porNombre.put(clave(ruta.getNombre()), ruta);
            }
        } catch (IOException e) {
            System.err.println("Error al cargar rutas desde el archivo: " + e.getMessage());
        }
    }

    private void reescribirArchivo() {         
        try {
            List<String> lineasAEscribir = new ArrayList<>();
            //Creamos una lista de string para escribir en el archivo de texto
            for (Ruta ruta : porNombre.values()) {
                String linea = String.format("%s,%s,%s,%s",
                    ruta.getNombre(),
                    ruta.getTipo().toString(),
                    ruta.getInicioJornada().toString(),
                    ruta.getFinJornada().toString());
                lineasAEscribir.add(linea);
            }
            // StandardOpenOption.CREATE y TRUNCATE_EXISTING limpian el archivo y escriben todo desde cero
            Files.write(
                Path.of(ARCHIVO_RUTAS),
                lineasAEscribir,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            );            
        } catch (IOException e) {
            System.err.println("Error al escribir rutas en el archivo: " + e.getMessage());
        }
    }

    private static String clave(String nombre) {
        return nombre.trim().toLowerCase();
    }

    public void guardar(Ruta ruta) {
        porNombre.put(clave(ruta.getNombre()), ruta);
        reescribirArchivo(); // Guardar en archivo al agregar o actualizar una ruta
    }
    

    public void eliminar(String nombre) {
        if (nombre != null) {
            porNombre.remove(clave(nombre));
            reescribirArchivo(); // Guardar en archivo al eliminar una ruta
        }
    }

    public Ruta buscarPorNombre(String nombre) {
        return (nombre == null) ? null : porNombre.get(clave(nombre));
    }

    public boolean existeNombre(String nombre) {
        return buscarPorNombre(nombre) != null;
    }

    public List<Ruta> listar() {
        return new ArrayList<>(porNombre.values());
    }


}
