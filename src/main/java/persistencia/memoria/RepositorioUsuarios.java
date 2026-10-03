package persistencia.memoria;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public Usuario buscarPorCedula(String cedula) {
        return (cedula == null) ? null : usuariosPorCedula.get(cedula.trim());
    }

    public boolean existeCorreo(String correo) {
        return usuariosPorCorreo.containsKey(correo);
    }

    public boolean existeCedula(String cedula) {
        return usuariosPorCedula.containsKey(cedula);
    }

    public List<Usuario> listar() {
        return new ArrayList<>(usuariosPorCorreo.values());
    }
}
