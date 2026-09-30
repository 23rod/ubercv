package persistencia.memoria;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import modelo.Ruta;

/** "Base de datos" de rutas en memoria. El nombre no distingue mayúsculas de minúsculas. */
public class RepositorioRutas {

    private final Map<String, Ruta> porNombre = new LinkedHashMap<>();

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
