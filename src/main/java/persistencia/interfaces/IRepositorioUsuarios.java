package persistencia.interfaces;

import java.util.List;
import modelo.Usuario;

public interface IRepositorioUsuarios {
    void guardar(Usuario usuario);
    
    /** Guarda el usuario en memoria sin disparar la escritura en disco (útil para DatosSemilla) */
    void guardarInicial(Usuario usuario);
    
    Usuario buscarPorCorreo(String correo);
    
    Usuario buscarPorCedula(String cedula);
    
    boolean existeCorreo(String correo);
    
    boolean existeCedula(String cedula);
    
    List<Usuario> listar();
}