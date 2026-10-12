package persistencia.interfaces;

import java.util.List;
import modelo.Usuario;

/** Contrato de almacenamiento de usuarios. Los servicios dependen de esta interfaz, no de cómo se guarda. */
public interface IRepositorioUsuarios {

    void guardar(Usuario usuario);

    Usuario buscarPorCorreo(String correo);

    Usuario buscarPorCedula(String cedula);

    boolean existeCorreo(String correo);

    boolean existeCedula(String cedula);

    List<Usuario> listar();
}
