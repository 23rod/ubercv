package persistencia.memoria;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import modelo.UnidadTransporte;

/** "Base de datos" de unidades en memoria: un mapa placa -> unidad (la placa se guarda en mayúsculas). */
public class RepositorioUnidades {
    private static final String ARCHIVO_UNIDADES = "memoria/unidadesAlmacenadas.txt";
    private final Map<String, UnidadTransporte> porPlaca = new LinkedHashMap<>();

    public RepositorioUnidades() {
        // Constructor vacío
        cargarUnidadesDesdeArchivo();
    }

    private void cargarUnidadesDesdeArchivo() {
        
        try{
            List<String> lineas = Files.readAllLines(Path.of(ARCHIVO_UNIDADES), StandardCharsets.UTF_8);
            for (String linea : lineas) {
                //Extraemos los datos de la unidad del archivo de texto separados por comas
                String[] partes = linea.split(",");
                //Creamos la unidad con los datos extraidos del archivo de texto
                //---------------------------------------------------Placa------------Modelo-----------------------Capacidad----------------------------------Estado-----------------------
                UnidadTransporte unidad = new UnidadTransporte( partes[0].trim() , partes[1].trim() , Integer.parseInt(partes[2].trim()), modelo.EstadoUnidad.desdeTexto(partes[3].trim()) );
                //nombreRuta
                if(partes.length == 4) unidad.setNombreRuta(partes[4].trim());  
                //cedulaConductor
                if(partes.length == 5) {
                    unidad.setNombreRuta(partes[4].trim());  
                    unidad.setCedulaConductor(partes[5].trim());
                }
                guardar(unidad);
            }

        }catch (Exception e) {
            System.err.println("Error al cargar unidades desde el archivo: " + e.getMessage());
        }
        
    }
    public void guardar(UnidadTransporte unidad) {
        porPlaca.put(unidad.getPlaca(), unidad);
    }

    public UnidadTransporte buscarPorPlaca(String placa) {
        return (placa == null) ? null : porPlaca.get(placa.trim().toUpperCase());
    }

    public boolean existePlaca(String placa) {
        return buscarPorPlaca(placa) != null;
    }

    public List<UnidadTransporte> listar() {
        return new ArrayList<>(porPlaca.values());
    }
}
