package controlador;

import java.awt.Component;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import modelo.EstadoUnidad;
import modelo.Reserva;
import modelo.Resultado;
import modelo.Ticket;
import servicio.ServicioReservas;
import utilidades.Dinero;
import vista.PlantillaVista.CuerposDelSistema.CuerpoFuncionesUsuario;

/** Adaptador Swing del módulo de reservas. Las reglas de negocio viven en ServicioReservas. */
public final class ControladorReservas {

    /** Diálogos que usa el controlador; se separan para poder probar el flujo sin ventanas reales. */
    interface Dialogos {
        void mensaje(Component padre, String texto);

        boolean confirmar(Component padre, String texto, String titulo);
    }

    private static final Dialogos DIALOGOS_SWING = new Dialogos() {
        @Override
        public void mensaje(Component padre, String texto) {
            JOptionPane.showMessageDialog(padre, texto);
        }

        @Override
        public boolean confirmar(Component padre, String texto, String titulo) {
            return JOptionPane.showConfirmDialog(padre, texto, titulo, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        }
    };

    private final ServicioReservas servicioReservas;
    private final CuerpoFuncionesUsuario vista;
    private final Runnable cargarTablaRutas;
    private final Runnable alCambiarSaldo;
    private final Dialogos dialogos;
    private boolean conectado;
    private boolean actualizando; // ignora los listeners mientras se recargan las tablas
    private List<Reserva> mostradas = new ArrayList<>(); // reservas en el mismo orden que las filas de las tablas

    /**
     * @param cargarTablaRutas rellena la tabla de rutas del pasajero (capacidad y puestos disponibles)
     * @param alCambiarSaldo   se ejecuta cuando una reserva o cancelación mueve dinero, para refrescar el encabezado
     */
    public ControladorReservas(ServicioReservas servicioReservas, CuerpoFuncionesUsuario vista,
                               Runnable cargarTablaRutas, Runnable alCambiarSaldo) {
        this(servicioReservas, vista, cargarTablaRutas, alCambiarSaldo, DIALOGOS_SWING);
    }

    ControladorReservas(ServicioReservas servicioReservas, CuerpoFuncionesUsuario vista,
                        Runnable cargarTablaRutas, Runnable alCambiarSaldo, Dialogos dialogos) {
        this.servicioReservas = java.util.Objects.requireNonNull(servicioReservas);
        this.vista = java.util.Objects.requireNonNull(vista);
        this.cargarTablaRutas = java.util.Objects.requireNonNull(cargarTablaRutas);
        this.alCambiarSaldo = java.util.Objects.requireNonNull(alCambiarSaldo);
        this.dialogos = java.util.Objects.requireNonNull(dialogos);
    }

    public void conectar() {
        if (conectado) return;
        conectado = true;
        vista.getTablaRutas().getSelectionModel().addListSelectionListener(this::alCambiarSeleccionRuta);
        vista.getBotonConfirmarReserva().addActionListener(e -> reservarSeleccionada());
        vista.getBotonEliminarReserva().addActionListener(e -> cancelarReservaSeleccionada());

        // Sincronizar selección entre la tabla de reservas y la de tickets
        vista.getTablaReservaActual().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !actualizando) {
                sincronizarSeleccion(vista.getTablaReservaActual(), vista.getTablaTicketActual());
            }
        });
        vista.getTablaTicketActual().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !actualizando) {
                sincronizarSeleccion(vista.getTablaTicketActual(), vista.getTablaReservaActual());
            }
        });

        refrescar();
    }

    /** Vuelve a leer rutas (disponibilidad), unidades, precio y reservas; se usa al abrir y al volver a la pantalla. */
    public void refrescar() {
        refrescarTablaRutas();
        actualizarReservaActual();
    }

    private void sincronizarSeleccion(JTable origen, JTable destino) {
        int fila = origen.getSelectedRow();
        if (fila >= 0 && fila < destino.getRowCount() && destino.getSelectedRow() != fila) {
            actualizando = true;
            try {
                destino.setRowSelectionInterval(fila, fila);
            } finally {
                actualizando = false;
            }
        }
        actualizarDetalleSeleccionado();
    }

    private void alCambiarSeleccionRuta(ListSelectionEvent evento) {
        if (evento.getValueIsAdjusting() || actualizando) return;
        actualizarPlacasDisponibles();
        actualizarPrecio();
    }

    private String rutaSeleccionada() {
        int fila = vista.getTablaRutas().getSelectedRow();
        if (fila < 0) return null;
        int filaModelo = vista.getTablaRutas().convertRowIndexToModel(fila);
        Object valor = vista.getModeloTablaRutas().getValueAt(filaModelo, 0);
        return valor == null ? null : valor.toString();
    }

    /** Recarga la tabla de rutas conservando la ruta elegida (recargar vacía la selección de la tabla). */
    private void refrescarTablaRutas() {
        String previa = rutaSeleccionada();
        actualizando = true;
        try {
            cargarTablaRutas.run();
            JTable tabla = vista.getTablaRutas();
            int fila = -1;
            for (int i = 0; previa != null && i < vista.getModeloTablaRutas().getRowCount(); i++) {
                if (previa.equals(String.valueOf(vista.getModeloTablaRutas().getValueAt(i, 0)))) {
                    fila = i;
                    break;
                }
            }
            if (fila < 0 && tabla.getRowCount() > 0) fila = 0;
            if (fila >= 0) {
                int filaVista = tabla.convertRowIndexToView(fila);
                tabla.setRowSelectionInterval(filaVista, filaVista);
            }
        } finally {
            actualizando = false;
        }
        actualizarPlacasDisponibles();
        actualizarPrecio();
    }

    private void actualizarPlacasDisponibles() {
        List<String> disponibles = servicioReservas.placasDisponiblesHoy(rutaSeleccionada());
        DefaultComboBoxModel<String> modelo = new DefaultComboBoxModel<>();
        modelo.addElement("Ninguna");
        for (String placa : disponibles) modelo.addElement(placa);
        vista.getComboPlaca().setModel(modelo);
        if (!disponibles.isEmpty()) vista.getComboPlaca().setSelectedIndex(1);
    }

    /** Muestra el importe final calculado por el servicio para el usuario autenticado y la ruta elegida. */
    private void actualizarPrecio() {
        String ruta = rutaSeleccionada();
        if (ruta == null) {
            vista.getLabelPrecioReserva().setText("Precio a pagar: -");
            return;
        }
        BigDecimal importe = servicioReservas.importeEstimado(ruta);
        vista.getLabelPrecioReserva().setText(importe.signum() <= 0
                ? "Precio a pagar: 0.00 (sin cobro)"
                : "Precio a pagar: " + Dinero.formatear(importe));
    }

    private void reservarSeleccionada() {
        Resultado<Reserva> resultado = servicioReservas.reservar(rutaSeleccionada(), vista.getPlacaSeleccionada());
        dialogos.mensaje(vista, resultado.getMensaje());
        if (resultado.isOk()) {
            refrescar();
            alCambiarSaldo.run();
        }
    }

    /** Reserva elegida en la tabla, o null si no hay una selección válida (nunca se asume la primera). */
    private Reserva reservaSeleccionada() {
        JTable tabla = vista.getTablaReservaActual();
        int fila = tabla.getSelectedRow();
        if (fila < 0) return null;
        int filaModelo = tabla.convertRowIndexToModel(fila);
        return filaModelo < mostradas.size() ? mostradas.get(filaModelo) : null;
    }

    private void cancelarReservaSeleccionada() {
        if (mostradas.isEmpty()) {
            dialogos.mensaje(vista, "No tienes reservas activas para hoy.");
            return;
        }
        Reserva seleccionada = reservaSeleccionada();
        if (seleccionada == null) {
            dialogos.mensaje(vista, "Seleccione en la tabla la reserva que desea cancelar.");
            return;
        }
        boolean confirmado = dialogos.confirmar(vista,
                "¿Deseas cancelar la reserva de " + seleccionada.getNombreRuta()
                        + " (Ticket: " + seleccionada.getTicketDeLaReserva().getCodigo() + ")?",
                "Confirmar cancelación");
        if (!confirmado) return;

        Resultado<Reserva> resultado = servicioReservas.cancelar(seleccionada.getTicketDeLaReserva().getCodigo());
        dialogos.mensaje(vista, resultado.getMensaje());
        if (resultado.isOk()) {
            refrescar();
            alCambiarSaldo.run();
        }
    }

    /** Texto del estado ACTUAL de la unidad (no la instantánea guardada al reservar). */
    private String estadoActualTexto(Reserva reserva) {
        EstadoUnidad estado = servicioReservas.estadoActualDeUnidad(reserva.getPlacaUnidad());
        if (estado == null) return reserva.getEstadoUnidad() + " (sin datos actuales)";
        return estado == EstadoUnidad.ACTIVO ? estado.getEtiqueta()
                : estado.getEtiqueta() + CuerpoFuncionesUsuario.MARCA_NO_OPERATIVA;
    }

    private void actualizarDetalleSeleccionado() {
        Reserva r = reservaSeleccionada();
        if (r == null) {
            vista.getBotonMasInformacionTicket().setText("Ticket Actual: ?");
        } else {
            vista.getBotonMasInformacionTicket().setText("Ticket Actual: " + r.getTicketDeLaReserva().getCodigo());
        }
        vista.getBotonEliminarReserva().setEnabled(r != null);
    }

    private void actualizarReservaActual() {
        Reserva previa = reservaSeleccionada();
        String codigoPrevio = previa == null ? null : previa.getTicketDeLaReserva().getCodigo();

        mostradas = new ArrayList<>(servicioReservas.reservasActivasDelDia());
        DefaultTableModel tablaReserva = vista.getFilasTablaReservaActual();
        DefaultTableModel tablaTicket = vista.getFilasTablaTicketActual();
        actualizando = true;
        try {
            tablaReserva.setRowCount(0);
            tablaTicket.setRowCount(0);
            if (mostradas.isEmpty()) {
                tablaReserva.addRow(new Object[] {"-", "-", "-", "-", "-", "-"});
                tablaTicket.addRow(new Object[] {"-", "-", "-", "-"});
            }
            int seleccion = mostradas.size() == 1 ? 0 : -1; // con una sola reserva no hay ambigüedad
            for (int i = 0; i < mostradas.size(); i++) {
                Reserva actual = mostradas.get(i);
                Ticket ticket = actual.getTicketDeLaReserva();
                tablaReserva.addRow(new Object[] {
                        actual.getNombreRuta(), actual.getTipoRuta(), actual.getInicio(), actual.getFin(),
                        actual.getPlacaUnidad(), estadoActualTexto(actual)
                });
                tablaTicket.addRow(new Object[] {
                        ticket.getCodigo(), ticket.getFechaReserva(), ticket.getNombreRuta(),
                        Dinero.formatear(actual.getImporteCobrado())
                });
                if (ticket.getCodigo().equals(codigoPrevio)) seleccion = i;
            }
            vista.getTablaReservaActual().clearSelection();
            vista.getTablaTicketActual().clearSelection();
            if (seleccion >= 0) {
                vista.getTablaReservaActual().setRowSelectionInterval(seleccion, seleccion);
                vista.getTablaTicketActual().setRowSelectionInterval(seleccion, seleccion);
            }
        } finally {
            actualizando = false;
        }
        actualizarDetalleSeleccionado();
    }
}
