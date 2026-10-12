package servicio;

import java.util.List;
import modelo.Rol;
import modelo.Usuario;

/** Consultas de solo lectura sobre usuarios (por ejemplo, para elegir un conductor). */
public interface ConsultaUsuarios {

    Usuario buscarPorCedula(String cedula);

    List<Usuario> listarPorRol(Rol rol);
}
