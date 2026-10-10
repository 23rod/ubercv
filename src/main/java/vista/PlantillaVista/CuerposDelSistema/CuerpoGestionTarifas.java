package vista.PlantillaVista.CuerposDelSistema;

import vista.PlantillaVista.CrearSubPanel;

import java.text.NumberFormat;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.table.DefaultTableModel;

public class CuerpoGestionTarifas extends JPanel{
    
    private JFormattedTextField campoTarifa;
    private JButton campoGuardarTarifa;
    private JComboBox<String> plegableRuta;
    private JButton campoActualizarTarifa;
    private JTable tablaRutas;
    private DefaultTableModel filasTablaRutas;

    public CuerpoGestionTarifas(){
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Extencion a todo lo alto disponible de la pagina
        this.setOpaque(true);
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.setBackground(new ColorUIResource(255,255,255));
        
        // Creacion del placeholder Monto 
        JLabel tarifaLabel = new JLabel("Indique la Tarifa para la ruta Extraurbana Seleccionada: ");
        NumberFormat formatoEntero = NumberFormat.getIntegerInstance();
        formatoEntero.setGroupingUsed(false); //Evita la division de los numeros por millares para evitar porblemas e compatibilidad en distintos dispositivos

        campoTarifa = new JFormattedTextField(formatoEntero);
        campoTarifa.setColumns(20);
        campoTarifa.setValue(0); // Valor por defecto

        JPanel panelTarifa = CrearSubPanel.subPanel(); 
        panelTarifa.add(tarifaLabel);
        panelTarifa.add(campoTarifa);

        // Creacion del boton Guardar Tarifa
        campoGuardarTarifa = new JButton("Guardar Tarifa");
        JPanel panelBotonGuardarTarifa = CrearSubPanel.subPanel();
        panelBotonGuardarTarifa.add(campoGuardarTarifa);

        // Creacion del placeholder tipo de ruta
        JLabel rutaLabel = new JLabel("Defina la ruta a Modificar:"); // Punto donde el controlador mostrara las rutas creadas hasta el momento.
        String[] ruta = {"Ninguna"};
        plegableRuta = new JComboBox<>(ruta);
        JPanel panelPlegableRutaSelecionada = CrearSubPanel.subPanel();
        panelPlegableRutaSelecionada.add(rutaLabel);
        panelPlegableRutaSelecionada.add(plegableRuta);
        
        // Creacion del boton Actualizar Tarifa
        campoActualizarTarifa = new JButton("Actualizar Tarifa Seleccionada");
        JPanel panelBotonActualizarTarifa = CrearSubPanel.subPanel();
        panelBotonActualizarTarifa.add(campoActualizarTarifa);

        // Cracion de la tabla de rutas
        String[] nombreColumnas = {"Nombre de Ruta", "Tarifa", "Estudiante", "Empleado", "Publico General"};
        filasTablaRutas = new DefaultTableModel(nombreColumnas,3){ // Contenedor "dinamico" de las filas de la tabla (AQUI SE MODIFICA LA CANTIDAD DE FILAS)
            @Override                                                                // Metodo que evita que las celdas sean modificadas por el usuario
            public boolean isCellEditable(int row, int column) {
            return false;
            }
        };
        tablaRutas = new JTable(filasTablaRutas);
        tablaRutas.setRowHeight(30);
        tablaRutas.getTableHeader().setReorderingAllowed(false);
        tablaRutas.getTableHeader().setResizingAllowed(false);
        tablaRutas.getColumnModel().getColumn(0).setPreferredWidth(140); // Nombre de Ruta
        tablaRutas.getColumnModel().getColumn(1).setPreferredWidth(60);  // Tarifa
        tablaRutas.getColumnModel().getColumn(2).setPreferredWidth(80);  // Estudiante
        tablaRutas.getColumnModel().getColumn(3).setPreferredWidth(80);  // Empleado
        tablaRutas.getColumnModel().getColumn(4).setPreferredWidth(145); // Publico General
        
        JScrollPane tablaConScroll = new JScrollPane(tablaRutas); //Metodo para garantizar la graficacion y visualizacion de una tabla de N filas
        tablaConScroll.setPreferredSize(new DimensionUIResource(920, 150));
        
        JPanel panelTablaTarifas = CrearSubPanel.subPanel(); // Ingreso la tablas con scroll en un panel para modificar sus dimensiones y ubicacion.
        panelTablaTarifas.add(tablaConScroll);

        // --------------------------------------- disposicion del panel central del Cuerpo(VentanaPagoMovil) -------------------------------------------- 
        add(panelTarifa);
        add(panelBotonGuardarTarifa);
        add(panelPlegableRutaSelecionada);
        add(panelBotonActualizarTarifa);
        add(panelTablaTarifas);
    }
    // ----------------------------------------- Acceso para los controladores ---------------------------------------------------------
    public int getTarifa(){
        try { // Confirmacion de que el texto en la casilla sea un digito 
            campoTarifa.commitEdit(); // Fuerza a JFormattedTextField a guardar el texto actual como valor
        } catch (java.text.ParseException e) {
            // Si el texto escrito no es un número válido, se ignora la edición no confirmada
        }
        Object valor = campoTarifa.getValue();
        if (valor instanceof Number) {
            return ((Number) valor).intValue();
        }
        return 0; // Caso que se confime la recarga sin ingresar un monto
    } 
    public JButton getBotonGuardarTarifa(){
        return campoGuardarTarifa;
    }
    public String getRutaSeleccionada(){
        return (String) plegableRuta.getSelectedItem(); 
    }
    public JButton getBotonActualizarTarifa(){
        return campoActualizarTarifa;
    }
    public DefaultTableModel getModeloTablaRutas(){
        return filasTablaRutas;
    }
}