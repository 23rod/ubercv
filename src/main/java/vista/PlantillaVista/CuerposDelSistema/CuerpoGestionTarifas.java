package vista.PlantillaVista.CuerposDelSistema;

import vista.PlantillaVista.CrearSubPanel;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.table.DefaultTableModel;

public class CuerpoGestionTarifas extends JPanel{
    
    private JTextField campoTarifa;
    private JButton campoGuardarTarifa;
    private JComboBox<String> plegableRuta;
    private JButton campoActualizarTarifa;
    private JTextField campoPorcentajeEstudiante;
    private JTextField campoPorcentajeEmpleado;
    private JButton campoGuardarPorcentaje;
    private JTable tablaRutas;
    private DefaultTableModel filasTablaRutas;

    public CuerpoGestionTarifas(){
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Extencion a todo lo alto disponible de la pagina
        this.setOpaque(true);
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.setBackground(new ColorUIResource(255,255,255));
        
        // Creacion del placeholder Monto 
        JLabel tarifaLabel = new JLabel("Indique la Tarifa para la ruta Extraurbana Seleccionada: ");
        campoTarifa = new JTextField(20);
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
        
        // Creacion del campo de descuento para Estudiante (10-20%)
        JLabel estudiantesLabel = new JLabel("Descuento para estudiantes (%): ");
        campoPorcentajeEstudiante = new JTextField(20);
        JPanel panelEstudiantePorcentaje = CrearSubPanel.subPanel(); 
        panelEstudiantePorcentaje.add(estudiantesLabel);
        panelEstudiantePorcentaje.add(campoPorcentajeEstudiante);
        
        // Creacion del campo de descuento para Empleado (30-60%)
        JLabel empleadosLabel = new JLabel("Descuento para empleados (%): ");
        campoPorcentajeEmpleado = new JTextField(20);
        JPanel panelEmpleadoTarifa = CrearSubPanel.subPanel(); 
        panelEmpleadoTarifa.add(empleadosLabel);
        panelEmpleadoTarifa.add(campoPorcentajeEmpleado);
        
        // Creacion del boton Guardar Descuentos
        campoGuardarPorcentaje = new JButton("Guardar Descuentos");
        JPanel panelBotonGuardarPorcentaje = CrearSubPanel.subPanel();
        panelBotonGuardarPorcentaje.add(campoGuardarPorcentaje);

        // Creacion de la tabla de rutas con importes finales a pagar
        String[] nombreColumnas = {"Nombre de Ruta", "Tarifa Base", "Importe Estudiante", "Importe Empleado", "Importe Público General"};
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
        tablaRutas.getColumnModel().getColumn(1).setPreferredWidth(80);  // Tarifa Base
        tablaRutas.getColumnModel().getColumn(2).setPreferredWidth(110); // Importe Estudiante
        tablaRutas.getColumnModel().getColumn(3).setPreferredWidth(110); // Importe Empleado
        tablaRutas.getColumnModel().getColumn(4).setPreferredWidth(155); // Importe Público General
        
        JScrollPane tablaConScroll = new JScrollPane(tablaRutas); //Metodo para garantizar la graficacion y visualizacion de una tabla de N filas
        tablaConScroll.setPreferredSize(new DimensionUIResource(920, 150));
        
        JPanel panelTablaTarifas = CrearSubPanel.subPanel(); // Ingreso la tablas con scroll en un panel para modificar sus dimensiones y ubicacion.
        panelTablaTarifas.add(tablaConScroll);

        // --------------------------------------- disposicion del panel central del Cuerpo(VentanaPagoMovil) -------------------------------------------- 
        add(panelTarifa);
        add(panelBotonGuardarTarifa);
        add(panelPlegableRutaSelecionada);
        add(panelBotonActualizarTarifa);
        add(panelEstudiantePorcentaje);
        add(panelEmpleadoTarifa);
        add(panelBotonGuardarPorcentaje);
        add(panelTablaTarifas);
    }
    // ----------------------------------------- Acceso para los controladores ---------------------------------------------------------
    public String getTarifa(){
        return campoTarifa.getText();
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
    public String getPorcentajeEstudiante(){
        return campoPorcentajeEstudiante.getText();
    } 
    public String getPorcentajeEmpleado(){
        return campoPorcentajeEmpleado.getText();
    } 
    public JButton getBotonGuardarPorcentajes(){
        return campoGuardarPorcentaje;
    }
    // El controlador muestra aqui los porcentajes de descuento vigentes (por ejemplo "15" y "45")
    public void mostrarPorcentajes(String estudiante, String empleado){
        campoPorcentajeEstudiante.setText(estudiante);
        campoPorcentajeEmpleado.setText(empleado);
    }
    public DefaultTableModel getModeloTablaRutas(){
        return filasTablaRutas;
    }
    public void setPorcentajes(int indice, String estudiante, String empleado){
        if (indice >= 0 && indice < filasTablaRutas.getRowCount()) {
            filasTablaRutas.setValueAt(estudiante, indice, 2);
            filasTablaRutas.setValueAt(empleado, indice, 3);
        }
    }
    // Lo llama el controlador para listar las rutas extraurbanas; conserva la ruta elegida si sigue existiendo
    public void actualizarRutas(java.util.List<String> nombres){
        Object elegida = plegableRuta.getSelectedItem();
        plegableRuta.removeAllItems();
        if (nombres.isEmpty()) {
            plegableRuta.addItem("Ninguna");
            return;
        }
        for (String nombre : nombres) {
            plegableRuta.addItem(nombre);
        }
        if (elegida != null && nombres.contains(elegida)) {
            plegableRuta.setSelectedItem(elegida);
        }
    }
}