package servicio;

import java.util.List;
import modelo.EstadoUnidad;
import modelo.Mensajes;
import modelo.Resultado;
import modelo.Rol;
import modelo.UnidadTransporte;
import persistencia.memoria.RepositorioUnidades;
import utilidades.validadores.Validador;

/** HU09: Gestión de Flota (registro de unidades y control de su estado). Solo para ADMIN_TRANSPORTE. */
public class ServicioFlota {

    private final RepositorioUnidades repositorio;
    private final ServicioUsuarios usuarios;

    public ServicioFlota(RepositorioUnidades repositorio, ServicioUsuarios usuarios) {
        this.repositorio = repositorio;
        this.usuarios = usuarios;
    }

    /** La capacidad llega como texto porque es lo que escribe el usuario; aquí se valida y se convierte. */
    public Resultado<Void> registrarUnidad(String placa, String modelo, String capacidad, EstadoUnidad estado) {
        Resultado<Void> acceso = usuarios.exigirRol(Rol.ADMIN_TRANSPORTE);
        if (!acceso.isOk()) {
            return acceso;
        }
        if (!Validador.esTextoNoVacio(placa) || !Validador.esTextoNoVacio(modelo)
                || !Validador.esTextoNoVacio(capacidad)) {
            return Resultado.error(Mensajes.CAMPOS_OBLIGATORIOS);
        }
        if (!Validador.esPlacaValida(placa)) {
            return Resultado.error(Mensajes.PLACA_INVALIDA);
        }
        if (!Validador.esCapacidadValida(capacidad)) {
            return Resultado.error(Mensajes.CAPACIDAD_INVALIDA);
        }
        if (estado == null) {
            return Resultado.error(Mensajes.ESTADO_REQUERIDO);
        }
        String placaNorm = placa.trim().toUpperCase();
        if (repositorio.existePlaca(placaNorm)) {
            return Resultado.error(Mensajes.UNIDAD_EXISTENTE); // HU09 escenario 2
        }
        repositorio.guardar(new UnidadTransporte(placaNorm, modelo.trim(), Integer.parseInt(capacidad.trim()), estado));
        return Resultado.exito(null, Mensajes.UNIDAD_GUARDADA); // HU09 escenario 1
    }

    /** Si la unidad deja de estar ACTIVA, se libera de su ruta: solo las unidades activas prestan servicio. */
    public Resultado<Void> cambiarEstado(String placa, EstadoUnidad nuevoEstado) {
        Resultado<Void> acceso = usuarios.exigirRol(Rol.ADMIN_TRANSPORTE);
        if (!acceso.isOk()) {
            return acceso;
        }
        UnidadTransporte unidad = repositorio.buscarPorPlaca(placa);
        if (unidad == null) {
            return Resultado.error(Mensajes.UNIDAD_NO_ENCONTRADA);
        }
        if (nuevoEstado == null) {
            return Resultado.error(Mensajes.ESTADO_REQUERIDO);
        }
        unidad.setEstado(nuevoEstado);
        if (nuevoEstado != EstadoUnidad.ACTIVO) {
            unidad.setNombreRuta(null);
        }
        return Resultado.exito(null, Mensajes.ESTADO_ACTUALIZADO);
    }

    public List<UnidadTransporte> listarUnidades() {
        return repositorio.listar();
    }
}
