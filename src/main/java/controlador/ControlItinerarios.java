package controlador;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import modelo.EstadoUnidad;
import modelo.Mensajes;
import modelo.Resultado;
import modelo.Ruta;
import modelo.TipoRuta;
import modelo.UnidadTransporte;
import modelo.Usuario;
import servicio.ServicioAsignaciones;
import servicio.ServicioReservas;
import servicio.ServicioRutas;
import servicio.ServicioUsuarios;
import vista.PlantillaVista.PanelGestionRutas;
import vista.PlantillaVista.CuerposDelSistema.CuerpoRegistroRuta;

/** HU10 y HU12: Control de Itinerarios (rutas, asignación de unidades y de conductores) y sus tablas. */
class ControlItinerarios {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final ServicioRutas servicioRutas;
    private final ServicioAsignaciones servicioAsignaciones;
    private final ServicioUsuarios servicioUsuarios;
    private final ControlFlota controlFlota; // solo para listar las placas en el panel
    private final ServicioReservas servicioReservas; // disponibilidad que ve el pasajero

    ControlItinerarios(ServicioRutas servicioRutas, ServicioAsignaciones servicioAsignaciones,
                       ServicioUsuarios servicioUsuarios, ControlFlota controlFlota, ServicioReservas servicioReservas) {
        this.servicioReservas = servicioReservas;
        this.servicioRutas = servicioRutas;
        this.servicioAsignaciones = servicioAsignaciones;
        this.servicioUsuarios = servicioUsuarios;
        this.controlFlota = controlFlota;
    }

    void conectar(CuerpoRegistroRuta cuerpo, PanelGestionRutas panel) {
        Runnable refrescar = () -> {
            llenarTablaRutas(cuerpo.getModeloTablaRutas(), true);
            cuerpo.actualizarRutasRedefinir(nombresDeRutas());
            panel.actualizarRutas(nombresDeRutas());
            panel.actualizarUnidades(controlFlota.placasDeUnidades());
            panel.actualizarConductores(cedulasDeConductores());
        };
        refrescar.run();

        // Autocompletar campos del formulario al seleccionar una ruta en el combo box de redefinir
        cuerpo.getComboRutasRedefinir().addActionListener(e -> autocompletarRuta(cuerpo, cuerpo.getRutaRedefinirElegida()));

        // Seleccionar una fila de la tabla equivale a elegir esa ruta: combo de redefinición, formulario y panel de asignación
        JTable tabla = cuerpo.getTablaRutas();
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int fila = tabla.getSelectedRow();
            if (fila < 0) {
                return;
            }
            Object valor = cuerpo.getModeloTablaRutas().getValueAt(tabla.convertRowIndexToModel(fila), 0);
            if (valor == null) {
                return;
            }
            String nombre = valor.toString();
            cuerpo.getComboRutasRedefinir().setSelectedItem(nombre);
            autocompletarRuta(cuerpo, nombre); // explícito: si el combo ya tenía esa ruta, setSelectedItem no dispara su listener
            panel.seleccionarRuta(nombre);
        });

        cuerpo.getBotonLimpiar().addActionListener(e -> cuerpo.limpiarCampos());

        // HU10: crear ruta
        cuerpo.getBotonGuardarRuta().addActionListener(e -> {
            Resultado<Void> r = servicioRutas.crearRuta(
                    cuerpo.getcampoNombreRuta().getText(),
                    TipoRuta.desdeTexto(cuerpo.getTipoRutaSeleccionada()),
                    cuerpo.getInicioJornadaSeleccionada(),
                    cuerpo.getFinalJornadaSeleccionada());
            JOptionPane.showMessageDialog(cuerpo, r.getMensaje());
            if (r.isOk()) {
                limpiarFormularioRuta(cuerpo);
                refrescar.run();
            }
        });

        // HU10: redefinir la ruta elegida con los datos del formulario usando su combo box exclusivo
        cuerpo.getBotonRedefinir().addActionListener(e -> {
            Resultado<Void> r = servicioRutas.redefinirRuta(
                    cuerpo.getRutaRedefinirElegida(),
                    cuerpo.getcampoNombreRuta().getText(),
                    TipoRuta.desdeTexto(cuerpo.getTipoRutaSeleccionada()),
                    cuerpo.getInicioJornadaSeleccionada(),
                    cuerpo.getFinalJornadaSeleccionada());
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
            if (placa != null && ruta != null && servicioAsignaciones.requiereConfirmacion(placa, ruta)) {
                int opcion = JOptionPane.showConfirmDialog(cuerpo, Mensajes.CONFIRMAR_CAMBIO,
                        "Confirmar cambio", JOptionPane.YES_NO_OPTION);
                if (opcion != JOptionPane.YES_OPTION) {
                    return;
                }
                confirmado = true;
            }
            Resultado<Void> r = servicioAsignaciones.asignarUnidad(placa, ruta, confirmado);
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

            Resultado<Void> r = servicioAsignaciones.asignarConductor(
                    cedulaConductor,
                    placa,
                    ruta);

            JOptionPane.showMessageDialog(cuerpo, r.getMensaje());

            if (r.isOk()) {
                refrescar.run();
            }
        });
    }

    private void autocompletarRuta(CuerpoRegistroRuta cuerpo, String nombreRuta) {
        if (nombreRuta == null) {
            return;
        }
        for (Ruta r : servicioRutas.listarRutas()) {
            if (r.getNombre().equalsIgnoreCase(nombreRuta)) {
                cuerpo.getcampoNombreRuta().setText(r.getNombre());
                cuerpo.setInicioJornada(r.getInicioJornada().format(HORA));
                cuerpo.setFinalJornada(r.getFinJornada().format(HORA));
                cuerpo.getPlegableRuta().setSelectedItem(r.getTipo().getEtiqueta());
                break;
            }
        }
    }

    /**
     * Tabla del pasajero: además de las placas muestra la capacidad total (solo unidades activas) de la ruta y, por separado, los puestos
     * disponibles hoy (solo unidades activas y descontando las reservas activas).
     */
    void llenarTablaRutasPasajero(DefaultTableModel modelo) {
        modelo.setRowCount(0);
        for (Ruta ruta : servicioRutas.listarRutas()) {
            List<UnidadTransporte> asignadas = servicioAsignaciones.unidadesDeRuta(ruta.getNombre());
            List<String> placas = new ArrayList<>();
            int capacidadActiva = 0; // las unidades inactivas no aportan capacidad
            for (UnidadTransporte unidad : asignadas) {
                placas.add(unidad.getPlaca());
                if (unidad.getEstado() == EstadoUnidad.ACTIVO) {
                    capacidadActiva += unidad.getCapacidad();
                }
            }
            modelo.addRow(new Object[] {
                ruta.getNombre(),
                ruta.getTipo().getEtiqueta(),
                ruta.getInicioJornada().format(HORA),
                ruta.getFinJornada().format(HORA),
                placas.isEmpty() ? "Sin unidades" : String.join(", ", placas),
                capacidadActiva,
                servicioReservas.cuposDisponiblesRutaHoy(ruta.getNombre())
            });
        }
    }

    /** conColumnaConductores: la tabla del admin tiene una sexta columna "Modificar Ruta". */
    void llenarTablaRutas(DefaultTableModel modelo, boolean conColumnaConductores) {
        modelo.setRowCount(0);
        for (Ruta ruta : servicioRutas.listarRutas()) {
            List<UnidadTransporte> asignadas = servicioAsignaciones.unidadesDeRuta(ruta.getNombre());
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
                textoUnidades = String.join(", ", listaPlacas) + " (Cupos: " + servicioAsignaciones.cuposDeRuta(ruta.getNombre()) + ")";
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
        for (UnidadTransporte u : servicioAsignaciones.unidadesDeConductor(sesion.getCedula())) {
            String nombreRuta = u.estaAsignada() ? u.getNombreRuta() : "Sin ruta";
            String horario = "-";
            if (u.estaAsignada()) {
                for (Ruta r : servicioRutas.listarRutas()) {
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
    }

    private List<String> nombresDeRutas() {
        List<String> nombres = new ArrayList<>();
        for (Ruta ruta : servicioRutas.listarRutas()) {
            nombres.add(ruta.getNombre());
        }
        return nombres;
    }

    private List<String> cedulasDeConductores() {
        List<String> cedulas = new ArrayList<>();
        for (Usuario conductor : servicioAsignaciones.listarConductores()) {
            cedulas.add(conductor.getCedula());
        }
        return cedulas;
    }
}