package controlador;

import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import modelo.RecargaPagoMovil;
import modelo.Resultado;
import servicio.ServicioRecargas;
import vista.PlantillaVista.CuerposDelSistema.CuerpoRecargasAdmin;
import vista.PlantillaVista.CuerposDelSistema.VentanaPagoMovil;

/** HU04: conecta la pantalla de pago móvil con el servicio de recargas. HU05: pantalla del administrador con las operaciones de cada usuario. */
class ControlRecargas {

    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

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
                    cuerpo.getMonto()); // texto tal cual: el servicio acepta decimales con punto o coma
            JOptionPane.showMessageDialog(cuerpo, r.getMensaje());
            if (r.isOk()) {
                cuerpo.getCampoCedula().setText("");
                cuerpo.getCampoTelefono().setText("");
                cuerpo.getCampoReferencia().setText("");
                alRecargar.run();
            }
        });
    }

    /** HU05: llena el selector con los usuarios que tienen operaciones y muestra las del usuario que el admin elige. */
    void conectarAdmin(CuerpoRecargasAdmin cuerpo) {
        Resultado<List<String>> usuarios = servicioRecargas.usuariosConRecargas();
        cuerpo.actualizarUsuarios(usuarios.isOk() ? usuarios.getValor() : List.of());
        cuerpo.getPlegableUsuarios().addActionListener(e -> mostrarOperaciones(cuerpo));
    }

    private void mostrarOperaciones(CuerpoRecargasAdmin cuerpo) {
        DefaultTableModel modelo = cuerpo.getModeloTabla();
        modelo.setRowCount(0);
        String correo = cuerpo.getUsuarioSeleccionado();
        if (correo == null) {
            return;
        }
        Resultado<List<RecargaPagoMovil>> r = servicioRecargas.recargasDe(correo);
        if (!r.isOk()) {
            JOptionPane.showMessageDialog(cuerpo, r.getMensaje());
            return;
        }
        for (RecargaPagoMovil op : r.getValor()) {
            modelo.addRow(new Object[] {
                op.getFecha().format(DIA),
                op.getFecha().format(HORA),
                op.getTelefono(),
                op.getCedula(),
                op.getBancoOrigen(),
                op.getBancoDestino(),
                op.getReferencia(),
                op.getMonto(),
                op.getDetalle()
            });
        }
    }
}
