package persistencia.memoria;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import modelo.Ruta;
import java.time.LocalTime;
import modelo.TipoRuta;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** "Base de datos" de rutas en memoria. El nombre no distingue mayúsculas de minúsculas. */
public class RepositorioRutas {

    private final Map<String, Ruta> porNombre = new LinkedHashMap<>();
    //Lectura y escritura de rutas en archivo de texto
    private static final String ARCHIVO_RUTAS = "memoria/rutasAlmacenadas.txt";
    

    public RepositorioRutas(){
        // Cargar rutas desde el archivo al iniciar el repositorio
        cargarRutasDesdeArchivo();
    }


    private void cargarRutasDesdeArchivo() {        
        try 
        {
            List<String> lineas = Files.readAllLines(Path.of(ARCHIVO_RUTAS), StandardCharsets.UTF_8);
            for (String linea : lineas) {
                //Extraemos los datos de la ruta del archivo de texto separados por comas
                String[] partes = linea.split(",");
                //Creamos la ruta con los datos extraidos del archivo de texto
                //-------------------Nombre-------------------TipoRuta----------------------------------InicioJornada-----------------------------FinJornada--------------------
                Ruta ruta = new Ruta( partes[0].trim() , TipoRuta.desdeTexto(partes[1].trim()) , LocalTime.parse(partes[2].trim()), LocalTime.parse(partes[3].trim()) );
                               
                guardar(ruta);
            }
        } catch (IOException e) {
            System.err.println("Error al cargar rutas desde el archivo: " + e.getMessage());
        }
    }

    private static String clave(String nombre) {
        return nombre.trim().toLowerCase();
    }

    public void guardar(Ruta ruta) {
        porNombre.put(clave(ruta.getNombre()), ruta);
    }

    public Ruta buscarPorNombre(String nombre) {
        return (nombre == null) ? null : porNombre.get(clave(nombre));
    }

    public boolean existeNombre(String nombre) {
        return buscarPorNombre(nombre) != null;
    }

    public void eliminar(String nombre) {
        if (nombre != null) {
            porNombre.remove(clave(nombre));
        }
    }

    public List<Ruta> listar() {
        return new ArrayList<>(porNombre.values());
    }


}
