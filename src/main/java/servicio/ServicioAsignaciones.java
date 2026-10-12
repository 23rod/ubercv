
package servicio;

import java.util.List;

import modelo.EstadoUnidad;
import modelo.Mensajes;
import modelo.Resultado;
import modelo.Rol;
import modelo.Ruta;
import modelo.UnidadTransporte;
import modelo.Usuario;
import persistencia.interfaces.IRepositorioRutas;
import persistencia.interfaces.IRepositorioUnidades;

/**
 * HU12: asignación de unidades a rutas y de conductores a unidades.
 * También consulta los cupos y las relaciones de asignación.
 */
public class ServicioAsignaciones {

    private final IRepositorioRutas rutas;
    private final IRepositorioUnidades unidades;
    private final Autorizador autorizador;
    private final ConsultaUsuarios consultaUsuarios;

    public ServicioAsignaciones(
            IRepositorioRutas rutas,
            IRepositorioUnidades unidades,
            Autorizador autorizador,
            ConsultaUsuarios consultaUsuarios) {

        this.rutas = rutas;
        this.unidades = unidades;
        this.autorizador = autorizador;
        this.consultaUsuarios = consultaUsuarios;
    }

    /**
     * Indica si mover la unidad a otra ruta requiere confirmación.
     */
    public boolean requiereConfirmacion(
            String placa, String nombreRuta) {

        if (placa == null || nombreRuta == null) {
            return false;
        }

        UnidadTransporte unidad = unidades.buscarPorPlaca(placa);

        return unidad != null
                && unidad.estaAsignada()
                && !unidad.getNombreRuta()
                        .equalsIgnoreCase(nombreRuta.trim());
    }

    /**
     * Asigna una unidad a una ruta.
     * Cambiar una unidad de ruta requiere confirmación explícita.
     */
    public Resultado<Void> asignarUnidad(
            String placa, String nombreRuta, boolean confirmado) {

        Resultado<Void> acceso =
                autorizador.exigirRol(Rol.ADMIN_TRANSPORTE);

        if (!acceso.isOk()) {
            return acceso;
        }

        UnidadTransporte unidad =
                unidades.buscarPorPlaca(placa);

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

            if (unidad.getNombreRuta()
                    .equalsIgnoreCase(ruta.getNombre())) {
                return Resultado.error(Mensajes.UNIDAD_YA_ASIGNADA);
            }

            if (!confirmado) {
                return Resultado.error(Mensajes.CONFIRMAR_CAMBIO);
            }

            unidad.setNombreRuta(ruta.getNombre());
            unidades.actualizar(unidad);

            return Resultado.exito(
                    null, Mensajes.UNIDAD_REASIGNADA);
        }

        unidad.setNombreRuta(ruta.getNombre());
        unidades.actualizar(unidad);

        return Resultado.exito(
                null, Mensajes.UNIDAD_ASIGNADA);
    }

    /**
     * Asigna un conductor a una unidad que presta servicio en la ruta indicada.
     * Un conductor no puede quedar asignado a dos unidades diferentes.
     */
    public Resultado<Void> asignarConductor(
            String cedulaConductor,
            String placa,
            String nombreRuta) {

        Resultado<Void> acceso =
                autorizador.exigirRol(Rol.ADMIN_TRANSPORTE);

        if (!acceso.isOk()) {
            return acceso;
        }

        Usuario conductor =
                consultaUsuarios.buscarPorCedula(cedulaConductor);

        if (conductor == null) {
            return Resultado.error(Mensajes.CONDUCTOR_NO_ENCONTRADO);
        }

        if (conductor.getRol() != Rol.CONDUCTOR) {
            return Resultado.error(Mensajes.USUARIO_NO_ES_CONDUCTOR);
        }

        UnidadTransporte unidad =
                unidades.buscarPorPlaca(placa);

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

        if (!unidad.getNombreRuta()
                .equalsIgnoreCase(ruta.getNombre())) {
            return Resultado.error(Mensajes.UNIDAD_NO_PERTENECE_RUTA);
        }

        if (unidad.tieneConductor()
                && unidad.getCedulaConductor()
                        .equals(conductor.getCedula())) {
            return Resultado.error(Mensajes.CONDUCTOR_YA_ASIGNADO);
        }

        // La consulta del repositorio evita recorrer todas las unidades.
        for (UnidadTransporte otra
                : unidades.listarPorConductor(conductor.getCedula())) {

            if (!otra.getPlaca().equalsIgnoreCase(unidad.getPlaca())) {
                return Resultado.error(Mensajes.CONDUCTOR_OCUPADO);
            }
        }

        boolean habiaConductor = unidad.tieneConductor();

        unidad.setCedulaConductor(conductor.getCedula());
        unidades.actualizar(unidad);

        if (habiaConductor) {
            return Resultado.exito(
                    null, Mensajes.CONDUCTOR_REASIGNADO);
        }

        return Resultado.exito(
                null, Mensajes.CONDUCTOR_ASIGNADO);
    }

    public List<Usuario> listarConductores() {
        return consultaUsuarios.listarPorRol(Rol.CONDUCTOR);
    }

    public List<UnidadTransporte> unidadesDeRuta(String nombreRuta) {
        if (nombreRuta == null) {
            return List.of();
        }

        return unidades.listarPorRuta(nombreRuta);
    }

    public List<UnidadTransporte> unidadesDeConductor(
            String cedulaConductor) {

        if (cedulaConductor == null) {
            return List.of();
        }

        return unidades.listarPorConductor(cedulaConductor);
    }

    /**
     * Los cupos de una ruta son la suma de las capacidades
     * de las unidades asignadas a ella.
     */
    public int cuposDeRuta(String nombreRuta) {
        int cupos = 0;

        for (UnidadTransporte unidad : unidadesDeRuta(nombreRuta)) {
            cupos += unidad.getCapacidad();
        }

        return cupos;
    }
}