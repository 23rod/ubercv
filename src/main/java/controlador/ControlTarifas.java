package controlador;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import modelo.Resultado;
import modelo.Rol;
import modelo.Ruta;
import servicio.ServicioTarifas;
import utilidades.Dinero;
import vista.PlantillaVista.CuerposDelSistema.CuerpoGestionTarifas;

/** HU11: Configuración de Tarifas del cobro solidario (solo rutas extraurbanas). */
class ControlTarifas {

    private final ServicioTarifas servicioTarifas;

    ControlTarifas(ServicioTarifas servicioTarifas) {
        this.servicioTarifas = servicioTarifas;
    }

    void conectar(CuerpoGestionTarifas cuerpo) {
        Runnable refrescar = () -> {
            llenarTabla(cuerpo.getModeloTablaRutas());
            cuerpo.actualizarRutas(nombresDeRutasExtraurbanas());
        };
        refrescar.run();
        mostrarPorcentajesVigentes(cuerpo);

        // HU11 escenario 1: registrar la tarifa base de la ruta elegida
        cuerpo.getBotonGuardarTarifa().addActionListener(e -> {
            Resultado<Void> r = servicioTarifas.registrarTarifaBase(
                    cuerpo.getRutaSeleccionada(), cuerpo.getTarifa());
            JOptionPane.showMessageDialog(cuerpo, r.getMensaje());
            if (r.isOk()) {
                refrescar.run();
            }
        });

        // HU11 escenario 2: actualizar la tarifa base de la ruta elegida
        cuerpo.getBotonActualizarTarifa().addActionListener(e -> {
            Resultado<Void> r = servicioTarifas.actualizarTarifaBase(
                    cuerpo.getRutaSeleccionada(), cuerpo.getTarifa());
            JOptionPane.showMessageDialog(cuerpo, r.getMensaje());
            if (r.isOk()) {
                refrescar.run();
            }
        });

        // Porcentajes por tipo de usuario (Estudiante 10-20 %, Empleado 30-60 %; Público General es siempre 100 %)
        cuerpo.getBotonGuardarPorcentajes().addActionListener(e -> {
            Resultado<Void> r = servicioTarifas.configurarPorcentajes(
                    cuerpo.getPorcentajeEstudiante(), cuerpo.getPorcentajeEmpleado());
            JOptionPane.showMessageDialog(cuerpo, r.getMensaje());
            if (r.isOk()) {
                refrescar.run(); // los montos de la tabla se recalculan con los porcentajes nuevos
                mostrarPorcentajesVigentes(cuerpo);
            }
        });
    }

    /** Pone en los campos los porcentajes que están vigentes, sin ceros de más ("15" y no "15.00"). */
    private void mostrarPorcentajesVigentes(CuerpoGestionTarifas cuerpo) {
        cuerpo.mostrarPorcentajes(
                servicioTarifas.porcentajeDe(Rol.ESTUDIANTE).stripTrailingZeros().toPlainString(),
                servicioTarifas.porcentajeDe(Rol.EMPLEADO).stripTrailingZeros().toPlainString());
    }

    private List<String> nombresDeRutasExtraurbanas() {
        List<String> nombres = new ArrayList<>();
        for (Ruta ruta : servicioTarifas.rutasExtraurbanas()) {
            nombres.add(ruta.getNombre());
        }
        return nombres;
    }

    /** Una fila por ruta extraurbana: nombre, tarifa base y lo que paga cada tipo de usuario. */
    private void llenarTabla(DefaultTableModel modelo) {
        modelo.setRowCount(0);
        for (Ruta ruta : servicioTarifas.rutasExtraurbanas()) {
            BigDecimal base = servicioTarifas.tarifaBase(ruta.getNombre());
            if (base == null) {
                modelo.addRow(new Object[] {ruta.getNombre(), "Sin tarifa", "-", "-", "-"});
                continue;
            }
            Map<Rol, BigDecimal> montos = servicioTarifas.desglose(ruta.getNombre());
            modelo.addRow(new Object[] {
                ruta.getNombre(),
                Dinero.formatear(base),
                Dinero.formatear(montos.get(Rol.ESTUDIANTE)),
                Dinero.formatear(montos.get(Rol.EMPLEADO)),
                Dinero.formatear(montos.get(Rol.PUBLICO_GENERAL))
            });
        }
    }
}
