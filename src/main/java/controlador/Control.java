package controlador;

import java.nio.file.Path;
import modelo.Resultado;
import modelo.Rol;
import persistencia.interfaces.IRepositorioMovimientos;
import persistencia.interfaces.IRepositorioRecargas;
import persistencia.interfaces.IRepositorioRutas;
import persistencia.interfaces.IRepositorioReservas;
import persistencia.interfaces.IRepositorioTarifas;
import persistencia.interfaces.IRepositorioUnidades;
import persistencia.interfaces.IRepositorioUsuarios;
import persistencia.memoria.DatosSemilla;
import persistencia.memoria.RepositorioMovimientos;
import persistencia.memoria.RepositorioRecargas;
import persistencia.memoria.RepositorioRutas;
import persistencia.memoria.RepositorioReservas;
import persistencia.memoria.RepositorioTarifas;
import persistencia.memoria.RepositorioUnidades;
import persistencia.memoria.RepositorioUsuarios;
import servicio.ServicioFlota;
import servicio.ServicioAsignaciones;
import servicio.ServicioMonedero;
import servicio.ServicioRecargas;
import servicio.ServicioReservas;
import servicio.ServicioRutas;
import servicio.ServicioTarifas;
import servicio.ServicioUsuarios;

/**
 * Raíz de composición y fachada de la capa de control. Crea los repositorios, los servicios y un
 * controlador por módulo; las vistas siguen llamando solo a los métodos públicos de esta clase.
 * Main crea UN solo Control y se lo pasa a las ventanas, así todas comparten usuarios y sesión.
 *
 * Módulos: ControlSesion (HU01/02), ControlFlota (HU09), ControlItinerarios (HU10/12), ControlTarifas (HU11),
 * ControlMonedero (HU04/06) y ControlRecargas (HU04/05),
 * ControlPrincipal (menú según rol). Navegador reúne las utilidades de ventanas.
 */
public class Control {

    private final ControlSesion controlSesion;
    private final ControlPrincipal controlPrincipal;

    public Control() {
        // Los datos se guardan en la carpeta "datos/" (se crea sola; la app se ejecuta desde la raíz del proyecto)
        IRepositorioUsuarios repositorioUsuarios = new RepositorioUsuarios(Path.of("datos", "usuarios.txt"));
        if (repositorioUsuarios.listar().isEmpty()) {
            DatosSemilla.sembrarUsuarios(repositorioUsuarios); // cuentas de demo, solo en el primer arranque
        }
        ServicioUsuarios servicioUsuarios = new ServicioUsuarios(repositorioUsuarios);

        IRepositorioUnidades repositorioUnidades = new RepositorioUnidades(Path.of("datos", "unidades.txt"));
        IRepositorioRutas repositorioRutas = new RepositorioRutas(Path.of("datos", "rutas.txt"));
        if (repositorioUnidades.listar().isEmpty() && repositorioRutas.listar().isEmpty()) {
            DatosSemilla.sembrarFlota(repositorioUnidades, repositorioRutas); // unidades y rutas de demo, solo en el primer arranque
        }
        ServicioFlota servicioFlota = new ServicioFlota(repositorioUnidades, servicioUsuarios);
        IRepositorioTarifas repositorioTarifas = new RepositorioTarifas(Path.of("datos", "tarifas.txt"));
        ServicioRutas servicioRutas = new ServicioRutas(repositorioRutas, repositorioUnidades, repositorioTarifas, servicioUsuarios);
        ServicioAsignaciones servicioAsignaciones =
                new ServicioAsignaciones(repositorioRutas, repositorioUnidades, servicioUsuarios, servicioUsuarios);

        Navegador navegador = new Navegador();
        this.controlSesion = new ControlSesion(servicioUsuarios, navegador, this);
        ControlFlota controlFlota = new ControlFlota(servicioFlota);
        ServicioTarifas servicioTarifas = new ServicioTarifas(repositorioTarifas, repositorioRutas, servicioUsuarios);
        ControlTarifas controlTarifas = new ControlTarifas(servicioTarifas);
        IRepositorioMovimientos repositorioMovimientos = new RepositorioMovimientos(Path.of("datos", "movimientos.txt"));
        ServicioMonedero servicioMonedero = new ServicioMonedero(repositorioMovimientos);
        IRepositorioReservas repositorioReservas = new RepositorioReservas(Path.of("datos", "reservas.txt"));
        ServicioReservas servicioReservas = new ServicioReservas(repositorioReservas, repositorioUnidades,
                repositorioRutas, servicioUsuarios, servicioTarifas, servicioMonedero);
        ControlItinerarios controlItinerarios = new ControlItinerarios(servicioRutas, servicioAsignaciones, servicioUsuarios,
                controlFlota, servicioReservas);
        IRepositorioRecargas repositorioRecargas = new RepositorioRecargas(Path.of("datos", "recargas.txt"));
        ServicioRecargas servicioRecargas = new ServicioRecargas(repositorioRecargas, servicioMonedero, servicioUsuarios, servicioUsuarios);
        ControlRecargas controlRecargas = new ControlRecargas(servicioRecargas);
        ControlMonedero controlMonedero = new ControlMonedero(servicioMonedero, servicioUsuarios, controlRecargas, navegador);
        this.controlPrincipal = new ControlPrincipal(servicioUsuarios, navegador, controlSesion, controlFlota, controlItinerarios,
                controlTarifas, controlRecargas, controlMonedero, servicioReservas, servicioMonedero);
    }

    // -------------------------------------------- Fachada que usan las vistas
    public Resultado<Rol> iniciarSesion(String correo, String clave) {
        return controlSesion.iniciarSesion(correo, clave);
    }

    public Resultado<Void> registrarUsuario(String correo, String clave, String confirmacion, String cedula, Rol rol) {
        return controlSesion.registrarUsuario(correo, clave, confirmacion, cedula, rol);
    }

    public Rol getRolSesion() {
        return controlSesion.getRolSesion();
    }

    /** Pantalla de bienvenida (MenuInicio). */
    public void abrirMenuInicio() {
        controlSesion.abrirMenuInicio();
    }

    /** HU01: ventana de registro. */
    public void abrirRegistro() {
        controlSesion.abrirRegistro();
    }

    /** HU02: tras iniciar sesión, la pantalla depende del rol. */
    public void abrirVentanaPrincipal() {
        controlPrincipal.abrirVentanaPrincipal();
    }

    public void cerrarSesion() {
        controlSesion.cerrarSesion();
    }
}
