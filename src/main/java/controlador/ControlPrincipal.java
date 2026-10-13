package controlador;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import modelo.Resultado;
import modelo.Rol;
import servicio.ServicioUsuarios;
import vista.PlantillaVista.PanelEstadoUnidad;
import vista.PlantillaVista.PanelGestionRutas;
import vista.PlantillaVista.VistaConcreta;
import vista.PlantillaVista.CuerposDelSistema.CuerpoFuncionesAdmin;
import vista.PlantillaVista.CuerposDelSistema.CuerpoFuncionesConductor;
import vista.PlantillaVista.CuerposDelSistema.CuerpoFuncionesUsuario;
import vista.PlantillaVista.CuerposDelSistema.CuerpoGestionTarifas;
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

    ControlPrincipal(ServicioUsuarios servicioUsuarios, Navegador navegador, ControlSesion controlSesion,
                     ControlFlota controlFlota, ControlItinerarios controlItinerarios, ControlTarifas controlTarifas) {
        this.servicioUsuarios = servicioUsuarios;
        this.navegador = navegador;
        this.controlSesion = controlSesion;
        this.controlFlota = controlFlota;
        this.controlItinerarios = controlItinerarios;
        this.controlTarifas = controlTarifas;
    }

    /** HU02: tras iniciar sesión, la pantalla depende del rol. */
    void abrirVentanaPrincipal() {
        Rol rol = controlSesion.getRolSesion();
        if (rol == Rol.NINGUNO) { // sin sesión no hay ventana principal
            controlSesion.abrirMenuInicio();
            return;
        }

        CuerpoFuncionesAdmin menuAdmin = null;
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
            default: // ESTUDIANTE y EMPLEADO
                CuerpoFuncionesUsuario menuUsuario = new CuerpoFuncionesUsuario();
                controlItinerarios.llenarTablaRutas(menuUsuario.getModeloTablaRutas(), false);
                cuerpo = menuUsuario;
        }

        VistaConcreta vista = new VistaConcreta(cuerpo);
        navegador.prepararVentana(vista, true);
        vista.getEncabezadoGeneral().getBotonCerrarSesion().addActionListener(e -> controlSesion.cerrarSesion());
        if (menuAdmin != null) {
            conectarMenuAdmin(vista, menuAdmin);
        }
        navegador.mostrar(vista);
    }

    /** Menú del admin: Flota, Itinerarios y Tarifas (solo ADMIN_TRANSPORTE). */
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
    }

    private boolean accesoAdmin(VistaConcreta vista) {
        Resultado<Void> acceso = servicioUsuarios.exigirRol(Rol.ADMIN_TRANSPORTE);
        if (!acceso.isOk()) {
            JOptionPane.showMessageDialog(vista, acceso.getMensaje());
        }
        return acceso.isOk();
    }
}
