package vista.PlantillaVista.CuerposDelSistema;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import vista.PlantillaVista.CrearSubPanel;

public class CuerpoFuncionesUsuario extends JPanel {

    /** Sufijo textual (no solo color) que marca una unidad que ya no está operativa. */
    public static final String MARCA_NO_OPERATIVA = " (no operativa)";
    private static final java.awt.Color COLOR_ALERTA = new java.awt.Color(200, 0, 0);
    
    private JButton botonMonederoVirtual;
    private JTable tablaRutas;
    private DefaultTableModel filasTablaRutas;
    
    // Componentes nuevos basados en el borrador[cite: 2]
    private JComboBox<String> comboPlaca;
    private JButton botonConfirmarReserva;
    private JButton botonEliminarReservaActual;
    private JLabel labelReservaActualDia;
    private JTable tablaReservaActual;
    private DefaultTableModel filasTablaReservaActual;
    private JLabel labelPrecioReserva;
    private JButton botonMasInformacionTicket;
    private JTable tablaTicketActual;
    private DefaultTableModel filasTablaTicketActual;
    
    public CuerpoFuncionesUsuario(){
        // Se configura el panel principal con BorderLayout para albergar el JScrollPane general
        this.setLayout(new BorderLayout());
        this.setOpaque(true);
        this.setBackground(new ColorUIResource(255,255,255));

        // Panel de contenido interno que mantiene la distribución vertical de los componentes
        JPanel panelContenido = new JPanel();
        panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS)); 
        panelContenido.setOpaque(true);
        panelContenido.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelContenido.setBackground(new ColorUIResource(255,255,255));

        // Creacion del boton Recarga Monedero
        botonMonederoVirtual = new JButton("Monedero Virtual");
        JPanel panelMVirtual = CrearSubPanel.subPanel();
        panelMVirtual.add(botonMonederoVirtual);

        // Creacion de la tabla de rutas[cite: 1]
        String[] nombreColumnasRutas = {"Nombre de Ruta", "Tipo de Ruta", "Inicio de Jornada", "Final de Jornada", "Unidades Asignadas(Placa)", "Capacidad total", "Puestos disponibles"};
        filasTablaRutas = new DefaultTableModel(nombreColumnasRutas, 3){ 
            @Override                                                                
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaRutas = new JTable(filasTablaRutas);
        tablaRutas.setRowHeight(30);
        tablaRutas.getTableHeader().setReorderingAllowed(false);
        tablaRutas.getTableHeader().setResizingAllowed(false);
        
        JScrollPane tablaConScrollRutas = new JScrollPane(tablaRutas); 
        tablaConScrollRutas.setPreferredSize(new DimensionUIResource(800, 150));
        
        JPanel panelTablaRutas = CrearSubPanel.subPanel(); 
        panelTablaRutas.add(tablaConScrollRutas);

        // --------------------------------------- Creación de elementos: (1) Reservar Cupos (JComboBox para Placa) --------------------------------------------
        String[] placa = {"Ninguna"};
        comboPlaca = new JComboBox<>(placa);
        JPanel panelComboPlaca = CrearSubPanel.subPanel();
        panelComboPlaca.add(new JLabel("Unidad deseada (Placa): "));
        panelComboPlaca.add(comboPlaca);

        // --------------------------------------- Creación de elementos: (2) Confirmar Reserva --------------------------------------------
        botonConfirmarReserva = new JButton("Confirmar Reserva");
        JPanel panelBotonConfirmarReserva = CrearSubPanel.subPanel();
        panelBotonConfirmarReserva.add(botonConfirmarReserva);
        labelPrecioReserva = new JLabel("Precio a pagar: -");
        panelBotonConfirmarReserva.add(labelPrecioReserva);

        //--------------------------------------- Creación del botón eliminar la reserva actual -----------------------------------------------------------------
        botonEliminarReservaActual = new JButton("Eliminar Reserva");
        JPanel panelbotonEliminarReservaActual = CrearSubPanel.subPanel();
        panelbotonEliminarReservaActual.add(botonEliminarReservaActual);

        // --------------------------------------- Creación de elementos: (3) Reserva Actual para Día Actual --------------------------------------------
        labelReservaActualDia = new JLabel("Reserva Actual para Día Actual");
        JPanel panelLabelReservaActual = CrearSubPanel.subPanel();
        panelLabelReservaActual.add(labelReservaActualDia);

        String[] nombreColumnasReservaActual = {"Nombre de Ruta", "Tipo de Ruta", "Inicio", "Final", "Placa", "Estado actual de la Unidad"};
        filasTablaReservaActual = new DefaultTableModel(nombreColumnasReservaActual, 1){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaReservaActual = new JTable(filasTablaReservaActual);
        tablaReservaActual.setRowHeight(35);
        tablaReservaActual.getTableHeader().setReorderingAllowed(false);
        tablaReservaActual.getTableHeader().setResizingAllowed(false);
        
        
        // Cálculo del ancho basado en contenido/texto para "Tipo de Ruta" + espacio de un caracter a cada lado
        TableColumn colTipoRuta = tablaReservaActual.getColumnModel().getColumn(1);
        FontMetrics fm = tablaReservaActual.getFontMetrics(tablaReservaActual.getFont());
        String sampleTipo = (String) colTipoRuta.getHeaderValue();
        int charWidth = fm.stringWidth("M"); // Ancho estimado de un carácter estándar
        int textWidth = fm.stringWidth(sampleTipo);
        colTipoRuta.setPreferredWidth(textWidth + (charWidth * 2));
        
        // Columnas Inicio, Final y Placa más pequeñas
        tablaReservaActual.getColumnModel().getColumn(2).setPreferredWidth(65); // Inicio
        tablaReservaActual.getColumnModel().getColumn(3).setPreferredWidth(65); // Final
        tablaReservaActual.getColumnModel().getColumn(4).setPreferredWidth(65); // Placa
        tablaReservaActual.getColumnModel().getColumn(5).setPreferredWidth(200); // Estado actual de la Unidad
        
        // El espacio liberado se le asigna a "Nombre de Ruta" (columna 0) para abarcar el excedente hasta completar los 800px de ancho preferido
        int anchoOtrasColumnas = colTipoRuta.getPreferredWidth() + 65 + 65 + 65 + 200;
        tablaReservaActual.getColumnModel().getColumn(0).setPreferredWidth(800 - anchoOtrasColumnas);
        
        // La columna de estado actual se pinta en rojo cuando la unidad no está operativa (además lleva texto)
        tablaReservaActual.getColumnModel().getColumn(5).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                    boolean foco, int fila, int columna) {
                java.awt.Component c = super.getTableCellRendererComponent(tabla, valor, seleccionada, foco, fila, columna);
                boolean alerta = valor != null && valor.toString().endsWith(MARCA_NO_OPERATIVA);
                if (alerta) {
                    c.setForeground(COLOR_ALERTA);
                } else {
                    c.setForeground(seleccionada ? tabla.getSelectionForeground() : tabla.getForeground());
                }
                return c;
            }
        });

        JScrollPane tablaConScrollReservaActual = new JScrollPane(tablaReservaActual);
        tablaConScrollReservaActual.setPreferredSize(new DimensionUIResource(800, 58));
        tablaConScrollReservaActual.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tablaConScrollReservaActual.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        JPanel panelTablaReservaActual = CrearSubPanel.subPanel();
        panelTablaReservaActual.add(tablaConScrollReservaActual);


        // --------------------------------------- Creación de elementos: (4 y 6) Ticket actual y Código/Fecha --------------------------------------------
        botonMasInformacionTicket = new JButton("Ticket Actual: ?");

        // Configuración del panel desplegable flotante hacia la derecha
        // El JWindow se crea al primer uso: así la vista puede construirse sin entorno gráfico (pruebas)
        final JWindow[] ventanaPopup = new JWindow[1];
        JPanel panelFlotante = new JPanel();
        panelFlotante.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(java.awt.Color.GRAY),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        panelFlotante.setBackground(new ColorUIResource(255, 255, 224)); 
        panelFlotante.add(new JLabel("Muestre el código, ruta y fecha de la reserva actual para abordar la unidad."));
        java.util.function.Supplier<JWindow> popup = () -> {
            if (ventanaPopup[0] == null) {
                ventanaPopup[0] = new JWindow();
                ventanaPopup[0].add(panelFlotante);
                ventanaPopup[0].pack();
            }
            return ventanaPopup[0];
        };

        // Evento para mostrar y alternar el panel flotante al presionar el botón ?
        botonMasInformacionTicket.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JWindow popupInformacion = popup.get();
                if (!popupInformacion.isVisible()) {
                    java.awt.Point loc = botonMasInformacionTicket.getLocationOnScreen();
                    popupInformacion.setLocation(loc.x + botonMasInformacionTicket.getWidth() + 5, loc.y);
                    popupInformacion.setVisible(true);
                } else {
                    popupInformacion.setVisible(false);
                }
            }
        });

        // Evento para ocultar el panel cuando el cursor sale del botón
        botonMasInformacionTicket.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                java.awt.Point p = e.getPoint();
                java.awt.Component comp = botonMasInformacionTicket.getComponentAt(p);
                if (comp == null && ventanaPopup[0] != null) {
                    ventanaPopup[0].setVisible(false);
                }
            }
        });
        
        // Evento para ocultar el panel si el cursor sale del panel flotante
        panelFlotante.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                if (ventanaPopup[0] != null) ventanaPopup[0].setVisible(false);
            }
        });

        JPanel panelTicketInfo = CrearSubPanel.subPanel();
        panelTicketInfo.add(botonMasInformacionTicket);

        String[] nombreColumnasTicketActual = {"Código", "Fecha de Reserva", "Ruta", "Importe"};
        filasTablaTicketActual = new DefaultTableModel(nombreColumnasTicketActual, 1){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaTicketActual = new JTable(filasTablaTicketActual);
        tablaTicketActual.setRowHeight(30);
        tablaTicketActual.getTableHeader().setReorderingAllowed(false);
        tablaTicketActual.getTableHeader().setResizingAllowed(false);
        
        JScrollPane tablaConScrollTicketActual = new JScrollPane(tablaTicketActual);
        tablaConScrollTicketActual.setPreferredSize(new DimensionUIResource(800, 150));
        
        JPanel panelTablaTicketActual = CrearSubPanel.subPanel();
        panelTablaTicketActual.add(tablaConScrollTicketActual);

        // --------------------------------------- Disposición en el panel de contenido interno -------------------------------------------- 
        panelContenido.add(panelMVirtual);
        panelContenido.add(panelTablaRutas);
        panelContenido.add(panelComboPlaca);
        panelContenido.add(panelBotonConfirmarReserva);        
        panelContenido.add(panelLabelReservaActual);
        panelContenido.add(panelTablaReservaActual);
        panelContenido.add(botonEliminarReservaActual);
        panelContenido.add(panelTicketInfo);
        panelContenido.add(panelTablaTicketActual);

        // Envolver todo el panel en un JScrollPane general para prevenir solapamientos
        JScrollPane scrollPaneGeneral = new JScrollPane(panelContenido);
        scrollPaneGeneral.setBorder(null);
        scrollPaneGeneral.getVerticalScrollBar().setUnitIncrement(16);
        
        this.add(scrollPaneGeneral, BorderLayout.CENTER);
    }
    
    // ----------------------------------------- Acceso para los controladores ---------------------------------------------------------
    public String getPlacaSeleccionada(){
        return (String)comboPlaca.getSelectedItem();
    }
    
    public JButton getBotonMonederoVirtual(){
        return botonMonederoVirtual;
    }
    
    public DefaultTableModel getModeloTablaRutas(){
        return filasTablaRutas;
    }

    public JTable getTablaRutas(){
        return tablaRutas;
    }

    public JComboBox<String> getComboPlaca() {
        return comboPlaca;
    }

    public JButton getBotonConfirmarReserva() {
        return botonConfirmarReserva;
    }

    public JButton getBotonEliminarReserva(){
        return botonEliminarReservaActual;
    }

    public JLabel getLabelReservaActualDia() {
        return labelReservaActualDia;
    }

    public JTable getTablaReservaActual() {
        return tablaReservaActual;
    }

    public DefaultTableModel getFilasTablaReservaActual() {
        return filasTablaReservaActual;
    }

    public JLabel getLabelPrecioReserva() {
        return labelPrecioReserva;
    }

    public JButton getBotonMasInformacionTicket() {
        return botonMasInformacionTicket;
    }

    public JTable getTablaTicketActual() {
        return tablaTicketActual;
    }

    public DefaultTableModel getFilasTablaTicketActual() {
        return filasTablaTicketActual;
    }
}