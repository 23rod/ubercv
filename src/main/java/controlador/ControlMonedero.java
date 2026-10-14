package controlador;

import java.time.format.DateTimeFormatter;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import modelo.Movimiento;
import modelo.Usuario;
import servicio.ServicioMonedero;
import servicio.SesionActual;
import utilidades.Dinero;
import vista.PlantillaVista.VistaConcreta;
import vista.PlantillaVista.CuerposDelSistema.CuerpoConsultarSaldo;
import vista.PlantillaVista.CuerposDelSistema.VentanaPagoMovil;

/**
 * Monedero Virtual del pasajero: saldo e historial de operaciones (HU06) y el acceso a la recarga por pago móvil (HU04).
 * Solo muestra y navega: el saldo y el historial los calcula el servicio.
 */
class ControlMonedero {

    private static final int OPERACIONES_VISIBLES = 10;
    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final ServicioMonedero servicioMonedero;
    private final SesionActual sesion;
    private final ControlRecargas controlRecargas;
    private final Navegador navegador;

    ControlMonedero(ServicioMonedero servicioMonedero, SesionActual sesion, ControlRecargas controlRecargas, Navegador navegador) {
        this.servicioMonedero = servicioMonedero;
        this.sesion = sesion;
        this.controlRecargas = controlRecargas;
        this.navegador = navegador;
    }

    /** Muestra el monedero del usuario con sesión dentro de la ventana principal. */
    void abrir(VistaConcreta vista, Runnable volverAlMenu) {
        CuerpoConsultarSaldo cuerpo = new CuerpoConsultarSaldo();
        JPanel pantalla = navegador.conBotonVolver("Monedero Virtual", cuerpo, null, volverAlMenu);
        refrescar(cuerpo);

        cuerpo.getBotonREcargaMondero().addActionListener(e -> {
            VentanaPagoMovil pago = new VentanaPagoMovil();
            // tras una recarga exitosa se vuelve al monedero con el saldo y el historial ya actualizados
            controlRecargas.conectar(pago, () -> {
                refrescar(cuerpo);
                vista.mostrarCuerpo(pantalla);
            });
            vista.mostrarCuerpo(navegador.conBotonVolver("Recargar Monedero Virtual", pago, null, () -> vista.mostrarCuerpo(pantalla)));
        });
        vista.mostrarCuerpo(pantalla);
    }

    /** Pone el saldo actual y las últimas operaciones (recargas, cobros y reintegros, la más reciente primero). */
    private void refrescar(CuerpoConsultarSaldo cuerpo) {
        Usuario usuario = sesion.getUsuarioSesion();
        if (usuario == null) {
            return;
        }
        cuerpo.mostrarSaldo(Dinero.formatear(servicioMonedero.saldo(usuario.getCorreo())));
        DefaultTableModel modelo = cuerpo.getModeloTablaOperaciones();
        modelo.setRowCount(0);
        for (Movimiento m : servicioMonedero.historial(usuario.getCorreo(), OPERACIONES_VISIBLES)) {
            String signo = m.getTipo().aumentaElSaldo() ? "+" : "-";
            modelo.addRow(new Object[] {
                m.getFecha().format(DIA),
                m.getFecha().format(HORA),
                m.getTipo().getEtiqueta(),
                m.getDescripcion(),
                signo + Dinero.formatear(m.getMonto())
            });
        }
    }
}
