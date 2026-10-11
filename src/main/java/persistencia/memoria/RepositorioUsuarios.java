package persistencia.memoria;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import modelo.Rol;
import modelo.Usuario;

public class RepositorioUsuarios {

    private final Map<String, Usuario> usuariosPorCorreo = new LinkedHashMap<>();
    private final Map<String, Usuario> usuariosPorCedula = new LinkedHashMap<>();

    private static final String ARCHIVO_USUARIOS = "C:\\proyecto_transporte\\ubercv\\src\\main\\java\\persistencia\\memoria\\usuariosAlmacenados.txt";

    public RepositorioUsuarios() {
        cargarUsuariosDesdeArchivo();
        reescribirArchivo(); // Limpia los duplicados existentes en el archivo al arrancar
    }

    private void cargarUsuariosDesdeArchivo() {
        Path ruta = Path.of(ARCHIVO_USUARIOS);
        if (!Files.exists(ruta)) {
            return;
        }

        try {
            List<String> lineas = Files.readAllLines(ruta, StandardCharsets.UTF_8);
            for (String linea : lineas) {
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("correo,")) {
                    continue; // Ignorar líneas vacías o cabeceras
                }

                String[] partes = linea.split(",");
                
                // Formato con sal persistida: correo,hashClave,salBase64,cedula,rol
                if (partes.length == 5) {
                    String correo = partes[0].trim().toLowerCase();
                    String hash = partes[1].trim();
                    byte[] sal;
                    try {
                        sal = Base64.getDecoder().decode(partes[2].trim());
                    } catch (IllegalArgumentException ex) {
                        sal = new byte[16]; // Fallback si venía corrupta
                    }
                    String cedula = partes[3].trim();
                    Rol rol = Rol.desdeTexto(partes[4].trim());

                    Usuario usuario = new Usuario(correo, hash, sal, cedula, rol);
                    usuariosPorCorreo.put(correo, usuario);
                    usuariosPorCedula.put(cedula, usuario);
                } 
                // Compatibilidad con registros viejos de 4 columnas (sin sal explícita)
                else if (partes.length == 4) {
                    String correo = partes[0].trim().toLowerCase();
                    String hash = partes[1].trim();
                    String cedula = partes[2].trim();
                    Rol rol = Rol.desdeTexto(partes[3].trim());

                    Usuario usuario = new Usuario(correo, hash, new byte[16], cedula, rol);
                    usuariosPorCorreo.put(correo, usuario);
                    usuariosPorCedula.put(cedula, usuario);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al cargar usuarios desde el archivo: " + e.getMessage());
        }
    }

    private void reescribirArchivo() {
        try {
            List<String> lineas = new ArrayList<>();
            for (Usuario u : usuariosPorCorreo.values()) {
                String salBase64 = (u.getSal() != null) ? Base64.getEncoder().encodeToString(u.getSal()) : "";
                String linea = String.format("%s,%s,%s,%s,%s",
                        u.getCorreo(),
                        u.getHashClave(),
                        salBase64,
                        u.getCedula(),
                        u.getRol()
                );
                lineas.add(linea);
            }

            Files.write(
                Path.of(ARCHIVO_USUARIOS),
                lineas,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            System.err.println("Error al reescribir usuarios en archivo: " + e.getMessage());
        }
    }

    public void guardar(Usuario usuario) {
        if (usuario == null) return;
        usuariosPorCorreo.put(usuario.getCorreo().toLowerCase(), usuario);
        usuariosPorCedula.put(usuario.getCedula(), usuario);
        reescribirArchivo();
    }

    public void guardarInicial(Usuario usuario) {
        if (usuario == null) return;
        usuariosPorCorreo.put(usuario.getCorreo().toLowerCase(), usuario);
        usuariosPorCedula.put(usuario.getCedula(), usuario);
    }

    public Usuario buscarPorCorreo(String correo) {
        return (correo == null) ? null : usuariosPorCorreo.get(correo.trim().toLowerCase());
    }

    public Usuario buscarPorCedula(String cedula) {
        return (cedula == null) ? null : usuariosPorCedula.get(cedula.trim());
    }

    public boolean existeCorreo(String correo) {
        return (correo != null) && usuariosPorCorreo.containsKey(correo.trim().toLowerCase());
    }

    public boolean existeCedula(String cedula) {
        return (cedula != null) && usuariosPorCedula.containsKey(cedula.trim());
    }

    public List<Usuario> listar() {
        return new ArrayList<>(usuariosPorCorreo.values());
    }
}