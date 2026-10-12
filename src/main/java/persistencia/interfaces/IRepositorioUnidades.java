package persistencia.interfaces;

import java.util.List;
import modelo.UnidadTransporte;
import modelo.EstadoUnidad;

public interface IRepositorioUnidades {
    void guardar(UnidadTransporte unidad);
    
    void modificar(UnidadTransporte unidad, EstadoUnidad nuevoEstado);
    
    void actualizar(UnidadTransporte unidad);
    
    UnidadTransporte buscarPorPlaca(String placa);
    
    boolean existePlaca(String placa);
    
    List<UnidadTransporte> listar();
}