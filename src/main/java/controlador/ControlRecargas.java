package controlador;

import javax.swing.JOptionPane;
import modelo.Resultado;
import servicio.ServicioRecargas;
import vista.PlantillaVista.CuerposDelSistema.VentanaPagoMovil;

/** HU04: conecta la pantalla de pago móvil con el servicio de recargas. */
class ControlRecargas {

    private final ServicioRecargas servicioRecargas;

    ControlRecargas(ServicioRecargas servicioRecargas) {
        this.servicioRecargas = servicioRecargas;
    }

    /**
     * Conecta el botón "Confirmar Recarga". Tras una recarga exitosa limpia los campos y ejecuta
     * {@code alRecargar} (por ejemplo, para refrescar el saldo que se muestra).
     */
    void conectar(VentanaPagoMovil cuerpo, Runnable alRecargar) {
        cuerpo.getBotonConfirmar().addActionListener(e -> {
            Resultado<Void> r = servicioRecargas.recargar(
                    cuerpo.getOrigenSeleccionado(),
                    cuerpo.getDestinoSeleccionado(),
                    cuerpo.getCampoCedula().getText(),
                    cuerpo.getCampoTelefono().getText(),
                    cuerpo.getCampoReferencia().getText(),
                    String.valueOf(cuerpo.getMonto())); // el campo de monto de la vista es de enteros; el servicio acepta decimales
            JOptionPane.showMessageDialog(cuerpo, r.getMensaje());
            if (r.isOk()) {
                cuerpo.getCampoCedula().setText("");
                cuerpo.getCampoTelefono().setText("");
                cuerpo.getCampoReferencia().setText("");
                alRecargar.run();
            }
        });
    }
}
