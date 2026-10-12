package persistencia.interfaces;

import java.util.List;
import modelo.Ruta;

/** Contrato de almacenamiento de rutas. El nombre de la ruta no distingue mayúsculas de minúsculas. */
public interface IRepositorioRutas {

    void guardar(Ruta ruta);

    void eliminar(String nombre);

    Ruta buscarPorNombre(String nombre);

    boolean existeNombre(String nombre);

    List<Ruta> listar();
}
