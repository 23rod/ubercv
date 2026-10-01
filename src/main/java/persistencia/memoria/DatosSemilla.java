package persistencia.memoria;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import modelo.EstadoUnidad;
import modelo.Rol;
import modelo.Ruta;
import modelo.TipoRuta;
import modelo.UnidadTransporte;
import modelo.Usuario;
import utilidades.seguridad.HashClave;
import utilidades.validadores.Validador;

/**
 * Datos de arranque del backend falso (todo en memoria, cédulas ficticias).
 *
 * CUENTAS DE DEMO (ya creadas, clave "Ucv12345" para todas):
 *   admin@ucv.com      -> ADMIN_TRANSPORTE
 *   estudiante@ucv.com -> ESTUDIANTE
 *   empleado@ucv.com   -> EMPLEADO
 *   conductor@ucv.com  -> CONDUCTOR
 *
 * CÉDULAS LIBRES PARA PROBAR EL REGISTRO (el rol elegido debe coincidir):
 *   10000001 y 10000002 -> Estudiante | 10000003 -> Empleado
 *   10000004 -> Conductor | 10000005 -> Admin de Transporte
 *
 * PERSONAS REALES (profesores, preparadores, el equipo): NO se escriben aquí. Se ponen en el archivo
 * padron-local.csv, que está en .gitignore y por tanto nunca llega a GitHub (ver padron-local.ejemplo.csv).
 */
public class DatosSemilla {

    public static final String CLAVE_DEMO = "Ucv12345";
    public static final String ARCHIVO_PADRON_LOCAL = "padron-local.csv";

    // Padrón institucional simulado: cédula -> rol que le corresponde
    private static final Map<String, Rol> PADRON = new HashMap<>();

    static {
        // Libres (para registrarse)
        PADRON.put("10000001", Rol.ESTUDIANTE);
        PADRON.put("10000002", Rol.ESTUDIANTE);
        PADRON.put("10000003", Rol.EMPLEADO);
        PADRON.put("10000004", Rol.CONDUCTOR);
        PADRON.put("10000005", Rol.ADMIN_TRANSPORTE);
        // De las cuentas de demo
        PADRON.put("12345678", Rol.ADMIN_TRANSPORTE);
        PADRON.put("20000001", Rol.ESTUDIANTE);
        PADRON.put("20000002", Rol.EMPLEADO);
        PADRON.put("20000003", Rol.CONDUCTOR);
        // Personas reales que cada integrante guarda solo en su computador (si el archivo existe)
        cargarPadronLocal(Path.of(ARCHIVO_PADRON_LOCAL));
    }

    /**
     * Agrega al padrón las líneas "cédula,rol" de un archivo, si existe. Acepta coma o punto y coma
     * (Excel en español guarda con ;), ignora líneas vacías, comentarios (#) y líneas mal formadas.
     * El rol puede escribirse "Estudiante", "Empleado", "Conductor", "Admin de Transporte" o el nombre
     * interno (ESTUDIANTE, ADMIN_TRANSPORTE...). Devuelve cuántas cédulas se cargaron.
     */
    public static int cargarPadronLocal(Path archivo) {
        if (archivo == null || !Files.isRegularFile(archivo)) {
            return 0;
        }
        int cargadas = 0;
        try {
            for (String linea : Files.readAllLines(archivo, StandardCharsets.UTF_8)) {
                String limpia = linea.replace("\uFEFF", "").trim(); // el Bloc de notas puede agregar una marca (BOM)
                if (limpia.isEmpty() || limpia.startsWith("#")) {
                    continue;
                }
                String[] partes = limpia.split("[,;]");
                if (partes.length < 2) {
                    continue;
                }
                String cedula = partes[0].trim();
                Rol rol = interpretarRol(partes[1]);
                if (Validador.esCedulaValida(cedula) && rol != Rol.NINGUNO) {
                    PADRON.put(cedula, rol);
                    cargadas++;
                }
            }
        } catch (IOException e) {
            return cargadas; // un archivo ilegible no debe impedir que la app arranque
        }
        return cargadas;
    }

    private static Rol interpretarRol(String texto) {
        String limpio = texto.trim();
        try {
            return Rol.valueOf(limpio.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Rol.desdeTexto(limpio);
        }
    }

    /** Rol que el padrón le asigna a esa cédula, o NINGUNO si no está registrada. */
    public static Rol validarEnPadron(String cedula) {
        return PADRON.getOrDefault(cedula, Rol.NINGUNO);
    }

    /** Crea las cuentas de demo en el repositorio. */
    public static void sembrarUsuarios(RepositorioUsuarios repositorio) {
        crear(repositorio, "admin@ucv.com", "12345678", Rol.ADMIN_TRANSPORTE);
        crear(repositorio, "estudiante@ucv.com", "20000001", Rol.ESTUDIANTE);
        crear(repositorio, "empleado@ucv.com", "20000002", Rol.EMPLEADO);
        crear(repositorio, "conductor@ucv.com", "20000003", Rol.CONDUCTOR);
    }

    /**
     * Flota y rutas de demo: 3 unidades (una en mantenimiento) y 2 rutas.
     * La unidad ABC123 arranca asignada a "UCV - Altamira".
     */
    public static void sembrarFlota(RepositorioUnidades unidades, RepositorioRutas rutas) {
        rutas.guardar(new Ruta("UCV - Altamira", TipoRuta.URBANA, LocalTime.of(6, 0), LocalTime.of(20, 0)));
        rutas.guardar(new Ruta("UCV - Guarenas", TipoRuta.EXTRAURBANA, LocalTime.of(5, 30), LocalTime.of(19, 30)));

        UnidadTransporte primera = new UnidadTransporte("ABC123", "Yutong ZK6", 40, EstadoUnidad.ACTIVO);
        primera.setNombreRuta("UCV - Altamira");
        unidades.guardar(primera);
        unidades.guardar(new UnidadTransporte("DEF456", "Encava E-NT610", 35, EstadoUnidad.ACTIVO));
        unidades.guardar(new UnidadTransporte("GHI789", "Mercedes Benz O500", 45, EstadoUnidad.EN_MANTENIMIENTO));
    }

    private static void crear(RepositorioUsuarios repositorio, String correo, String cedula, Rol rol) {
        byte[] sal = HashClave.generarSal();
        String hash = HashClave.hashearClave(CLAVE_DEMO, sal);
        repositorio.guardar(new Usuario(correo, hash, sal, cedula, rol));
    }
}
