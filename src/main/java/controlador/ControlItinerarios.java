package controlador;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import modelo.Mensajes;
import modelo.Resultado;
import modelo.Ruta;
import modelo.TipoRuta;
import modelo.UnidadTransporte;
import modelo.Usuario;
import servicio.ServicioItinerarios;
import servicio.ServicioUsuarios;
import vista.PlantillaVista.PanelGestionRutas;
import vista.PlantillaVista.CuerposDelSistema.CuerpoRegistroRuta;

/** HU10 y HU12: Control de Itinerarios (rutas, asignación de unidades y de conductores) y sus tablas. */
class ControlItinerarios {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final ServicioItinerarios servicioItinerarios;
    private final ServicioUsuarios servicioUsuarios;
    private final ControlFlota controlFlota; // solo para listar las placas en el panel

    ControlItinerarios(ServicioItinerarios servicioItinerarios, ServicioUsuarios servicioUsuarios, ControlFlota controlFlota) {
        this.servicioItinerarios = servicioItinerarios;
        this.servicioUsuarios = servicioUsuarios;
        this.controlFlota = controlFlota;
    }

    void conectar(CuerpoRegistroRuta cuerpo, PanelGestionRutas panel) {
        Runnable refrescar = () -> {
            llenarTablaRutas(cuerpo.getModeloTablaRutas(), true);
            panel.actualizarRutas(nombresDeRutas());
            panel.actualizarUnidades(controlFlota.placasDeUnidades());
            panel.actualizarConductores(cedulasDeConductores());
        };
        refrescar.run();

        // HU10: crear ruta
        cuerpo.getBotonConfirmar().addActionListener(e -> {
            Resultado<Void> r = servicioItinerarios.crearRuta(
                    cuerpo.getcampoNombreRuta().getText(),
                    TipoRuta.desdeTexto(cuerpo.getTipoRutaSeleccionada()),
                    cuerpo.getcampoInicioJornada().getText(),
                    cuerpo.getcampoFinalJornada().getText());
            JOptionPane.showMessageDialog(cuerpo, r.getMensaje());
            if (r.isOk()) {
                limpiarFormularioRuta(cuerpo);
                refrescar.run();
            }
        });

        // HU10: redefinir la ruta elegida con los datos del formulario
        panel.getBotonRedefinir().addActionListener(e -> {
            Resultado<Void> r = servicioItinerarios.redefinirRuta(
                    panel.getRutaElegida(),
                    cuerpo.getcampoNombreRuta().getText(),
                    TipoRuta.desdeTexto(cuerpo.getTipoRutaSeleccionada()),
                    cuerpo.getcampoInicioJornada().getText(),
                    cuerpo.getcampoFinalJornada().getText());
            JOptionPane.showMessageDialog(cuerpo, r.getMensaje());
            if (r.isOk()) {
                limpiarFormularioRuta(cuerpo);
                refrescar.run();
            }
        });

        // HU12: asignar unidad a ruta (pide confirmación si la unidad ya está en otra ruta)
        panel.getBotonAsignar().addActionListener(e -> {
            String placa = panel.getUnidadElegida();
            String ruta = panel.getRutaElegida();
            boolean confirmado = false;
            if (placa != null && ruta != null && servicioItinerarios.requiereConfirmacion(placa, ruta)) {
                int opcion = JOptionPane.showConfirmDialog(cuerpo, Mensajes.CONFIRMAR_CAMBIO,
                        "Confirmar cambio", JOptionPane.YES_NO_OPTION);
                if (opcion != JOptionPane.YES_OPTION) {
                    return;
                }
                confirmado = true;
            }
            Resultado<Void> r = servicioItinerarios.asignarUnidad(placa, ruta, confirmado);
            JOptionPane.showMessageDialog(cuerpo, r.getMensaje());
            if (r.isOk()) {
                refrescar.run();
            }
        });

        // Asignación del conductor responsable de la unidad en la ruta seleccionada
        panel.getBotonAsignarConductor().addActionListener(e -> {
            String ruta = panel.getRutaElegida();
            String placa = panel.getUnidadElegida();
            String cedulaConductor = panel.getConductorElegido();

            Resultado<Void> r = servicioItinerarios.asignarConductor(
                    cedulaConductor,
                    placa,
                    ruta);

            JOptionPane.showMessageDialog(cuerpo, r.getMensaje());

            if (r.isOk()) {
                refrescar.run();
            }
        });
    }

    /** conColumnaConductores: la tabla del admin tiene una sexta columna "Modificar Ruta". */
    void llenarTablaRutas(DefaultTableModel modelo, boolean conColumnaConductores) {
        modelo.setRowCount(0);
        for (Ruta ruta : servicioItinerarios.listarRutas()) {
            List<UnidadTransporte> asignadas = servicioItinerarios.unidadesDeRuta(ruta.getNombre());
            String textoUnidades;
            String textoConductores;

            if (asignadas.isEmpty()) {
                textoUnidades = "Sin unidades";
                textoConductores = "Sin conductores";
            } else {
                List<String> listaPlacas = new ArrayList<>();
                List<String> listaConductores = new ArrayList<>();
                for (UnidadTransporte unidad : asignadas) {
                    listaPlacas.add(unidad.getPlaca());
                    String cond = unidad.tieneConductor() ? unidad.getCedulaConductor() : "Sin asignar";
                    listaConductores.add(unidad.getPlaca() + ": " + cond);
                }
                textoUnidades = String.join(", ", listaPlacas) + " (Cupos: " + servicioItinerarios.cuposDeRuta(ruta.getNombre()) + ")";
                textoConductores = String.join(" | ", listaConductores);
            }

            Object[] fila = {
                ruta.getNombre(),
                ruta.getTipo().getEtiqueta(),
                ruta.getInicioJornada().format(HORA),
                ruta.getFinJornada().format(HORA),
                textoUnidades
            };

            if (conColumnaConductores) {
                fila = new Object[] {fila[0], fila[1], fila[2], fila[3], fila[4], textoConductores};
            }
            modelo.addRow(fila);
        }
    }

    void llenarTablaConductor(DefaultTableModel modelo) {
        modelo.setRowCount(0);
        Usuario sesion = servicioUsuarios.getUsuarioSesion();
        if (sesion == null) {
            return;
        }
        for (UnidadTransporte u : servicioItinerarios.unidadesDeConductor(sesion.getCedula())) {
            String nombreRuta = u.estaAsignada() ? u.getNombreRuta() : "Sin ruta";
            String horario = "-";
            if (u.estaAsignada()) {
                for (Ruta r : servicioItinerarios.listarRutas()) {
                    if (r.getNombre().equalsIgnoreCase(nombreRuta)) {
                        horario = r.getInicioJornada().format(HORA) + " - " + r.getFinJornada().format(HORA);
                        break;
                    }
                }
            }
            modelo.addRow(new Object[] {
                u.getPlaca(),
                u.getModelo(),
                u.getCapacidad(),
                u.getEstado().getEtiqueta(),
                nombreRuta,
                horario
            });
        }
    }

    private void limpiarFormularioRuta(CuerpoRegistroRuta cuerpo) {
        cuerpo.getcampoNombreRuta().setText("");
        cuerpo.getcampoInicioJornada().setText("");
        cuerpo.getcampoFinalJornada().setText("");
    }

    private List<String> nombresDeRutas() {
        List<String> nombres = new ArrayList<>();
        for (Ruta ruta : servicioItinerarios.listarRutas()) {
            nombres.add(ruta.getNombre());
        }
        return nombres;
    }

    private List<String> cedulasDeConductores() {
        List<String> cedulas = new ArrayList<>();
        for (Usuario conductor : servicioItinerarios.listarConductores()) {
            cedulas.add(conductor.getCedula());
        }
        return cedulas;
    }
}
