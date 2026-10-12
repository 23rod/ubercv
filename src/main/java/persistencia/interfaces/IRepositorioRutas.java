package persistencia.interfaces;

import java.util.List;
import modelo.Ruta;

public interface IRepositorioRutas {
    void guardar(Ruta ruta);
    
    void eliminar(String nombre);
    
    Ruta buscarPorNombre(String nombre);
    
    boolean existeNombre(String nombre);
    
    List<Ruta> listar();
}