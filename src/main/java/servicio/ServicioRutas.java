package servicio;

import java.time.LocalTime;
import java.util.List;
import modelo.Mensajes;
import modelo.Resultado;
import modelo.Rol;
import modelo.Ruta;
import modelo.TipoRuta;
import modelo.UnidadTransporte;
import persistencia.interfaces.IRepositorioRutas;
import persistencia.interfaces.IRepositorioTarifas;
import persistencia.interfaces.IRepositorioUnidades;
import utilidades.validadores.Validador;

/** HU10: definir y redefinir rutas y horarios. Solo para ADMIN_TRANSPORTE. Si una ruta cambia de nombre, sus unidades y su tarifa la siguen. */
public class ServicioRutas {

    private final IRepositorioRutas rutas;
    private final IRepositorioUnidades unidades;
    private final IRepositorioTarifas tarifas; // la tarifa de una ruta la sigue si la ruta cambia de nombre
    private final Autorizador autorizador;

    public ServicioRutas(IRepositorioRutas rutas, IRepositorioUnidades unidades,
                         IRepositorioTarifas tarifas, Autorizador autorizador) {
        this.rutas = rutas;
        this.unidades = unidades;
        this.tarifas = tarifas;
        this.autorizador = autorizador;
    }

    public Resultado<Void> crearRuta(String nombre, TipoRuta tipo, String inicio, String fin) {
        Resultado<Void> acceso = autorizador.exigirRol(Rol.ADMIN_TRANSPORTE);
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
        Resultado<Void> acceso = autorizador.exigirRol(Rol.ADMIN_TRANSPORTE);
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
        List<UnidadTransporte> asignadas = unidades.listarPorRuta(actual.getNombre());
        rutas.eliminar(actual.getNombre());
        rutas.guardar(new Ruta(nombreNuevo, tipo, Validador.parsearHora(inicio), Validador.parsearHora(fin)));
        for (UnidadTransporte unidad : asignadas) {
            unidad.setNombreRuta(nombreNuevo);
            unidades.actualizar(unidad);
        }
        if (cambiaElNombre) {
            tarifas.moverTarifaBase(actual.getNombre(), nombreNuevo);
        }
        return Resultado.exito(null, Mensajes.RUTA_REDEFINIDA); // HU10 escenario 3
    }

    public List<Ruta> listarRutas() {
        return rutas.listar();
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
