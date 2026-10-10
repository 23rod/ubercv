package persistencia.memoria;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import modelo.Usuario;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class RepositorioUsuarios {

    // std::unordered_map<std::string, Usuario*>
    private final Map<String, Usuario> usuariosPorCorreo = new HashMap<>();
    private final Map<String, Usuario> usuariosPorCedula = new HashMap<>();

    private final static String ARCHIVO_USUARIOS = "memoria/usuariosAlmacenados.txt";

    public RepositorioUsuarios() {
        //Cargar usuarios registrados desde el archivo de texto al iniciar el repositorio
        cargarUsuariosDesdeArchivo();
        
    }

    private void cargarUsuariosDesdeArchivo() {
        // Implementación para cargar usuarios desde un archivo de texto
        // Este método debería leer un archivo y crear objetos Usuario para agregarlos a los mapas
        try{
            List<String> lineas = Files.readAllLines(Path.of(ARCHIVO_USUARIOS), StandardCharsets.UTF_8);
            for (String linea : lineas) {
                //Extraemos los datos del usuario del archivo de texto separados por comas
                String[] partes = linea.split(",");
                //Creamos el usuario con los datos extraidos del archivo de texto
                byte [] salBytes = partes[2].getBytes(StandardCharsets.UTF_8);
                //-------------------------------Correo----------HashClave----------Sal-----------Cedula-------------------Rol--------------------
                Usuario usuario = new Usuario(partes[0].trim(), partes[1].trim(), salBytes, partes[3].trim(), modelo.Rol.desdeTexto(partes[4].trim()));
                guardar(usuario);
            }

        }catch (Exception e) {
            System.err.println("Error al cargar usuarios desde el archivo: " + e.getMessage());
        }


    }

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
