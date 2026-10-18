package controlador;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import modelo.Resultado;
import modelo.Rol;
import modelo.Usuario;
import servicio.ServicioMonedero;
import servicio.ServicioUsuarios;
import servicio.ServicioReservas;
import utilidades.Dinero;
import vista.PlantillaVista.EncabezadoGeneral;
import vista.PlantillaVista.PanelEstadoUnidad;
import vista.PlantillaVista.PanelGestionRutas;
import vista.PlantillaVista.VistaConcreta;
import vista.PlantillaVista.CuerposDelSistema.CuerpoFuncionesAdmin;
import vista.PlantillaVista.CuerposDelSistema.CuerpoFuncionesConductor;
import vista.PlantillaVista.CuerposDelSistema.CuerpoFuncionesUsuario;
import vista.PlantillaVista.CuerposDelSistema.CuerpoGestionTarifas;
import vista.PlantillaVista.CuerposDelSistema.CuerpoRecargasAdmin;
import vista.PlantillaVista.CuerposDelSistema.CuerpoRegistroRuta;
import vista.PlantillaVista.CuerposDelSistema.CuerpoRegistroUnidad;

/**
 * HU02: pantalla principal según el rol. Aquí se enchufan los módulos: cada botón de un menú
 * abre la pantalla de su módulo y le pasa su controlador (flota, itinerarios y, más adelante, monedero y tarifas).
 */
class ControlPrincipal {

    private final ServicioUsuarios servicioUsuarios;
    private final Navegador navegador;
    private final ControlSesion controlSesion;
    private final ControlFlota controlFlota;
    private final ControlItinerarios controlItinerarios;
    private final ControlTarifas controlTarifas;
    private final ControlRecargas controlRecargas;
    private final ControlMonedero controlMonedero;
    private final ServicioReservas servicioReservas;
    private final ServicioMonedero servicioMonedero;

    ControlPrincipal(ServicioUsuarios servicioUsuarios, Navegador navegador, ControlSesion controlSesion,
                     ControlFlota controlFlota, ControlItinerarios controlItinerarios, ControlTarifas controlTarifas,
                     ControlRecargas controlRecargas, ControlMonedero controlMonedero,
                     ServicioReservas servicioReservas, ServicioMonedero servicioMonedero) {
        this.servicioMonedero = servicioMonedero;
        this.servicioUsuarios = servicioUsuarios;
        this.navegador = navegador;
        this.controlSesion = controlSesion;
        this.controlFlota = controlFlota;
        this.controlItinerarios = controlItinerarios;
        this.controlTarifas = controlTarifas;
        this.controlRecargas = controlRecargas;
        this.controlMonedero = controlMonedero;
        this.servicioReservas = servicioReservas;
    }

    /** HU02: tras iniciar sesión, la pantalla depende del rol. */
    void abrirVentanaPrincipal() {
        Rol rol = controlSesion.getRolSesion();
        if (rol == Rol.NINGUNO) { // sin sesión no hay ventana principal
            controlSesion.abrirMenuInicio();
            return;
        }

        CuerpoFuncionesAdmin menuAdmin = null;
        CuerpoFuncionesUsuario menuPasajero = null;
        JPanel cuerpo;
        switch (rol) {
            case ADMIN_TRANSPORTE:
                menuAdmin = new CuerpoFuncionesAdmin();
                cuerpo = menuAdmin;
                break;
            case CONDUCTOR:
                CuerpoFuncionesConductor menuConductor = new CuerpoFuncionesConductor();
                controlItinerarios.llenarTablaConductor(menuConductor.getModeloTablaUnidades());
                cuerpo = menuConductor;
                break;
            default: // ESTUDIANTE, EMPLEADO y PUBLICO_GENERAL
                menuPasajero = new CuerpoFuncionesUsuario(); // la tabla de rutas la llena su controlador al conectarse
                cuerpo = menuPasajero;
        }

        VistaConcreta vista = new VistaConcreta(cuerpo);
        navegador.prepararVentana(vista, true);
        vista.getEncabezadoGeneral().getBotonCerrarSesion().addActionListener(e -> controlSesion.cerrarSesion());
        actualizarEncabezado(vista);
        if (menuAdmin != null) {
            conectarMenuAdmin(vista, menuAdmin);
        }
        if (menuPasajero != null) {
            conectarMenuPasajero(vista, menuPasajero);
        }
        navegador.mostrar(vista);
    }

    /** Menú del admin: Flota, Itinerarios, Tarifas y Recargas de Monedero (solo ADMIN_TRANSPORTE). */
    private void conectarMenuAdmin(VistaConcreta vista, CuerpoFuncionesAdmin menu) {
        Runnable volverAlMenu = () -> vista.mostrarCuerpo(menu);

        menu.getBotonFlota().addActionListener(e -> {
            if (accesoAdmin(vista)) {
                CuerpoRegistroUnidad cuerpo = new CuerpoRegistroUnidad();
                PanelEstadoUnidad panel = new PanelEstadoUnidad();
                controlFlota.conectar(cuerpo, panel);
                vista.mostrarCuerpo(navegador.conBotonVolver("Gestión de Flota", cuerpo, panel, volverAlMenu));
            }
        });
        menu.getBotonItinerario().addActionListener(e -> {
            if (accesoAdmin(vista)) {
                CuerpoRegistroRuta cuerpo = new CuerpoRegistroRuta();
                PanelGestionRutas panel = new PanelGestionRutas();
                controlItinerarios.conectar(cuerpo, panel);
                vista.mostrarCuerpo(navegador.conBotonVolver("Control de Itinerarios", cuerpo, panel, volverAlMenu));
            }
        });
        menu.getBotonTarifas().addActionListener(e -> {
            if (accesoAdmin(vista)) {
                CuerpoGestionTarifas cuerpo = new CuerpoGestionTarifas();
                controlTarifas.conectar(cuerpo);
                vista.mostrarCuerpo(navegador.conBotonVolver("Gestión de Tarifas", cuerpo, null, volverAlMenu));
            }
        });
        menu.getBotonRecargas().addActionListener(e -> {
            if (accesoAdmin(vista)) {
                CuerpoRecargasAdmin cuerpo = new CuerpoRecargasAdmin();
                controlRecargas.conectarAdmin(cuerpo);
                vista.mostrarCuerpo(navegador.conBotonVolver("Recargas de Monedero por Usuario", cuerpo, null, volverAlMenu));
            }
        });
    }

    /** Menú del pasajero: Monedero Virtual (saldo, historial y recarga por pago móvil) y reservas. */
    private void conectarMenuPasajero(VistaConcreta vista, CuerpoFuncionesUsuario menu) {
        Runnable refrescarEncabezado = () -> actualizarEncabezado(vista);
        ControladorReservas reservas = new ControladorReservas(servicioReservas, menu,
                () -> controlItinerarios.llenarTablaRutasPasajero(menu.getModeloTablaRutas()), refrescarEncabezado);
        // al volver del monedero se relee todo: saldo, disponibilidad, precio y estado de las unidades
        Runnable volverAlMenu = () -> {
            reservas.refrescar();
            refrescarEncabezado.run();
            vista.mostrarCuerpo(menu);
        };
        menu.getBotonMonederoVirtual().addActionListener(e -> controlMonedero.abrir(vista, volverAlMenu, refrescarEncabezado));
        reservas.conectar();
    }

    /**
     * Cabecera con sesión: correo (el modelo no guarda un nombre), rol legible y, para quien paga pasajes,
     * el saldo actual leído del servicio compartido. Sin sesión el panel se oculta completo.
     */
    private void actualizarEncabezado(VistaConcreta vista) {
        EncabezadoGeneral encabezado = vista.getEncabezadoGeneral();
        Usuario usuario = servicioUsuarios.getUsuarioSesion();
        if (usuario == null) {
            encabezado.mostrarInfoUsuario(false);
            return;
        }
        Rol rol = usuario.getRol();
        boolean pagaPasajes = rol == Rol.ESTUDIANTE || rol == Rol.EMPLEADO || rol == Rol.PUBLICO_GENERAL;
        String saldo = pagaPasajes ? Dinero.formatear(servicioMonedero.saldo(usuario.getCorreo())) : "-";
        encabezado.setInfoUsuario(usuario.getCorreo(), rol.getEtiqueta(), saldo);
        encabezado.mostrarSaldo(pagaPasajes);
        encabezado.mostrarInfoUsuario(true);
    }

    private boolean accesoAdmin(VistaConcreta vista) {
        Resultado<Void> acceso = servicioUsuarios.exigirRol(Rol.ADMIN_TRANSPORTE);
        if (!acceso.isOk()) {
            JOptionPane.showMessageDialog(vista, acceso.getMensaje());
        }
        return acceso.isOk();
    }
}
