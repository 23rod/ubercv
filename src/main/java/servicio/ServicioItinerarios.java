package servicio;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import modelo.EstadoUnidad;
import modelo.Mensajes;
import modelo.Resultado;
import modelo.Rol;
import modelo.Ruta;
import modelo.TipoRuta;
import modelo.UnidadTransporte;
import modelo.Usuario;
import persistencia.memoria.RepositorioRutas;
import persistencia.memoria.RepositorioUnidades;
import utilidades.validadores.Validador;

/**
 * HU10 (definir y redefinir rutas y horarios) y HU12 (asignar unidades a rutas).
 * Solo para ADMIN_TRANSPORTE. Los cupos de una ruta son la suma de las capacidades de sus unidades.
 */
public class ServicioItinerarios {

    private final RepositorioRutas rutas;
    private final RepositorioUnidades unidades;
    private final ServicioUsuarios usuarios;

    public ServicioItinerarios(RepositorioRutas rutas, RepositorioUnidades unidades, ServicioUsuarios usuarios) {
        this.rutas = rutas;
        this.unidades = unidades;
        this.usuarios = usuarios;
    }

    // --------------------------------------------------------------- HU10
    public Resultado<Void> crearRuta(String nombre, TipoRuta tipo, String inicio, String fin) {
        Resultado<Void> acceso = usuarios.exigirRol(Rol.ADMIN_TRANSPORTE);
        if (!acceso.isOk()) {
            return acceso;
        }
        Resultado<Void> invalido = validar(nombre, tipo, inicio, fin);
        if (invalido != null) {
            return invalido;
        }
        if (rutas.existeNombre(nombre)) {
            return Resultado.error(Mensajes.RUTA_EXISTENTE); // HU10 escenario 2
        }
        rutas.guardar(new Ruta(nombre.trim(), tipo, Validador.parsearHora(inicio), Validador.parsearHora(fin)));
        return Resultado.exito(null, Mensajes.RUTA_GUARDADA); // HU10 escenario 1
    }

    /** Cambia nombre, tipo y horario de una ruta. Si cambia el nombre, sus unidades la siguen. */
    public Resultado<Void> redefinirRuta(String nombreActual, String nuevoNombre, TipoRuta tipo, String inicio, String fin) {
        Resultado<Void> acceso = usuarios.exigirRol(Rol.ADMIN_TRANSPORTE);
        if (!acceso.isOk()) {
            return acceso;
        }
        Ruta actual = rutas.buscarPorNombre(nombreActual);
        if (actual == null) {
            return Resultado.error(Mensajes.RUTA_NO_ENCONTRADA);
        }
        Resultado<Void> invalido = validar(nuevoNombre, tipo, inicio, fin);
        if (invalido != null) {
            return invalido;
        }
        boolean cambiaElNombre = !actual.getNombre().equalsIgnoreCase(nuevoNombre.trim());
        if (cambiaElNombre && rutas.existeNombre(nuevoNombre)) {
            return Resultado.error(Mensajes.RUTA_EXISTENTE);
        }
        String nombreNuevo = nuevoNombre.trim();
        List<UnidadTransporte> asignadas = unidadesDeRuta(actual.getNombre());
        rutas.eliminar(actual.getNombre());
        rutas.guardar(new Ruta(nombreNuevo, tipo, Validador.parsearHora(inicio), Validador.parsearHora(fin)));
        for (UnidadTransporte unidad : asignadas) {
            unidad.setNombreRuta(nombreNuevo);
        }
        return Resultado.exito(null, Mensajes.RUTA_REDEFINIDA); // HU10 escenario 3
    }

    // --------------------------------------------------------------- HU12
    /** true si la unidad ya está en OTRA ruta: la vista debe preguntar "¿Está seguro del cambio?". */
    public boolean requiereConfirmacion(String placa, String nombreRuta) {
        UnidadTransporte unidad = unidades.buscarPorPlaca(placa);
        return unidad != null && unidad.estaAsignada() && !unidad.getNombreRuta().equalsIgnoreCase(nombreRuta.trim());
    }

    /** Asigna una unidad a una ruta. Para mover una unidad de una ruta a otra hay que pasar confirmado = true. */
    public Resultado<Void> asignarUnidad(String placa, String nombreRuta, boolean confirmado) {
        Resultado<Void> acceso = usuarios.exigirRol(Rol.ADMIN_TRANSPORTE);
        if (!acceso.isOk()) {
            return acceso;
        }
        UnidadTransporte unidad = unidades.buscarPorPlaca(placa);
        if (unidad == null) {
            return Resultado.error(Mensajes.UNIDAD_NO_ENCONTRADA);
        }
        Ruta ruta = rutas.buscarPorNombre(nombreRuta);
        if (ruta == null) {
            return Resultado.error(Mensajes.RUTA_NO_ENCONTRADA);
        }
        if (unidad.getEstado() != EstadoUnidad.ACTIVO) {
            return Resultado.error(Mensajes.UNIDAD_NO_ACTIVA);
        }
        if (unidad.estaAsignada()) {
            if (unidad.getNombreRuta().equalsIgnoreCase(ruta.getNombre())) {
                return Resultado.error(Mensajes.UNIDAD_YA_ASIGNADA); // HU12 escenario 3
            }
            if (!confirmado) {
                return Resultado.error(Mensajes.CONFIRMAR_CAMBIO); // HU12 escenario 2
            }
            unidad.setNombreRuta(ruta.getNombre());
            return Resultado.exito(null, Mensajes.UNIDAD_REASIGNADA);
        }
        unidad.setNombreRuta(ruta.getNombre()); // HU12 escenario 1
        return Resultado.exito(null, Mensajes.UNIDAD_ASIGNADA);
    }

    // ----------------------------------------------------- Conductores
    /**
     * Asigna un conductor a la unidad que actualmente presta servicio en una ruta.
     * La relación del modelo es conductor -> unidad -> ruta.
     */
    public Resultado<Void> asignarConductor(String cedulaConductor, String placa, String nombreRuta) {
        Resultado<Void> acceso = usuarios.exigirRol(Rol.ADMIN_TRANSPORTE);
        if (!acceso.isOk()) {
            return acceso;
        }

        Usuario conductor = usuarios.buscarPorCedula(cedulaConductor);
        if (conductor == null) {
            return Resultado.error(Mensajes.CONDUCTOR_NO_ENCONTRADO);
        }

        if (conductor.getRol() != Rol.CONDUCTOR) {
            return Resultado.error(Mensajes.USUARIO_NO_ES_CONDUCTOR);
        }

        UnidadTransporte unidad = unidades.buscarPorPlaca(placa);
        if (unidad == null) {
            return Resultado.error(Mensajes.UNIDAD_NO_ENCONTRADA);
        }

        Ruta ruta = rutas.buscarPorNombre(nombreRuta);
        if (ruta == null) {
            return Resultado.error(Mensajes.RUTA_NO_ENCONTRADA);
        }

        if (!unidad.estaAsignada()) {
            return Resultado.error(Mensajes.UNIDAD_SIN_RUTA);
        }

        if (!unidad.getNombreRuta().equalsIgnoreCase(ruta.getNombre())) {
            return Resultado.error(Mensajes.UNIDAD_NO_PERTENECE_RUTA);
        }

        if (unidad.tieneConductor()
                && unidad.getCedulaConductor().equals(conductor.getCedula())) {
            return Resultado.error(Mensajes.CONDUCTOR_YA_ASIGNADO);
        }
        if (unidad.tieneConductor()
                && unidad.getCedulaConductor().equals(conductor.getCedula())) {
            return Resultado.error(Mensajes.CONDUCTOR_YA_ASIGNADO);
        }
        // Regla 1:1 -> Verificar que el conductor no esté asignado a otra unidad
        for (UnidadTransporte otra : unidades.listar()) {
            if (!otra.getPlaca().equalsIgnoreCase(unidad.getPlaca())
                    && otra.tieneConductor()
                    && otra.getCedulaConductor().equals(conductor.getCedula())) {
                return Resultado.error(Mensajes.CONDUCTOR_OCUPADO);
            }
        }
        boolean habiaConductor = unidad.tieneConductor();
        unidad.setCedulaConductor(conductor.getCedula());

        if (habiaConductor) {
            return Resultado.exito(null, Mensajes.CONDUCTOR_REASIGNADO);
        }

        return Resultado.exito(null, Mensajes.CONDUCTOR_ASIGNADO);
    }

    public List<Usuario> listarConductores() {
        return usuarios.listarPorRol(Rol.CONDUCTOR);
    }

    // ------------------------------------------------------------ Consultas
    public List<Ruta> listarRutas() {
        return rutas.listar();
    }

    public List<UnidadTransporte> unidadesDeRuta(String nombreRuta) {
        List<UnidadTransporte> resultado = new ArrayList<>();
        for (UnidadTransporte unidad : unidades.listar()) {
            if (unidad.estaAsignada() && unidad.getNombreRuta().equalsIgnoreCase(nombreRuta.trim())) {
                resultado.add(unidad);
            }
        }
        return resultado;
    }

    public List<UnidadTransporte> unidadesDeConductor(String cedulaConductor) {
        List<UnidadTransporte> resultado = new ArrayList<>();
        if (cedulaConductor == null) {
            return resultado;
        }
        for (UnidadTransporte unidad : unidades.listar()) {
            if (unidad.tieneConductor() && unidad.getCedulaConductor().equals(cedulaConductor.trim())) {
                resultado.add(unidad);
            }
        }
        return resultado;
    }

    /** Cupos de la ruta = suma de las capacidades de las unidades asignadas. */
    public int cuposDeRuta(String nombreRuta) {
        int cupos = 0;
        for (UnidadTransporte unidad : unidadesDeRuta(nombreRuta)) {
            cupos += unidad.getCapacidad();
        }
        return cupos;
    }

    /** Devuelve un error si los datos no son válidos, o null si todo está bien. */
    private Resultado<Void> validar(String nombre, TipoRuta tipo, String inicio, String fin) {
        if (!Validador.esTextoNoVacio(nombre) || !Validador.esTextoNoVacio(inicio) || !Validador.esTextoNoVacio(fin)) {
            return Resultado.error(Mensajes.CAMPOS_OBLIGATORIOS);
        }
        if (!Validador.cabe(nombre, Validador.MAX_NOMBRE_RUTA)) {
            return Resultado.error(Mensajes.NOMBRE_RUTA_LARGO);
        }
        if (tipo == null) {
            return Resultado.error(Mensajes.TIPO_RUTA_REQUERIDO);
        }
        LocalTime horaInicio = Validador.parsearHora(inicio);
        LocalTime horaFin = Validador.parsearHora(fin);
        if (horaInicio == null || horaFin == null || !horaInicio.isBefore(horaFin)) {
            return Resultado.error(Mensajes.HORARIO_INVALIDO);
        }
        return null;
    }
}
