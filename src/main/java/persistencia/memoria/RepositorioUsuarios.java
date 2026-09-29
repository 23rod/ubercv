package persistencia.memoria;

import java.util.HashMap;
import java.util.Map;
import modelo.Usuario;

public class RepositorioUsuarios {
    
    // std::unordered_map<std::string, Usuario*>
    private final Map<String, Usuario> usuariosPorCorreo = new HashMap<>();
    private final Map<String, Usuario> usuariosPorCedula = new HashMap<>();

    public void guardar(Usuario usuario) {
        usuariosPorCorreo.put(usuario.getCorreo(), usuario);
        usuariosPorCedula.put(usuario.getCedula(), usuario);
    }

    public Usuario buscarPorCorreo(String correo) {
        return usuariosPorCorreo.get(correo);
    }

    public boolean existeCorreo(String correo) {
        return usuariosPorCorreo.containsKey(correo);
    }

    public boolean existeCedula(String cedula) {
        return usuariosPorCedula.containsKey(cedula);
    }
}