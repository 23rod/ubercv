package persistencia.memoria;

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
 */
public class DatosSemilla {

    public static final String CLAVE_DEMO = "Ucv12345";

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
