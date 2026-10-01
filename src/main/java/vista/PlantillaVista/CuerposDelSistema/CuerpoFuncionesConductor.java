package vista.PlantillaVista.CuerposDelSistema;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.table.DefaultTableModel;

import vista.PlantillaVista.CrearSubPanel;

public class CuerpoFuncionesConductor extends JPanel {
    
    private JTable tablaUnidades;
    private DefaultTableModel filasTablaUnidades;
    

    public CuerpoFuncionesConductor(){
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Extencion a todo lo alto disponible de la pagina
        this.setOpaque(true);
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.setBackground(new ColorUIResource(255,255,255));

        // Creacion de la tabla de rutas
        String[] nombreColumnas = {"Placa", "Modelo", "Capacidad", "Estado Operativo", "Confirmar Actividad"};
        filasTablaUnidades = new DefaultTableModel(nombreColumnas,3){ // Contenedor "dinamico" de las filas de la tabla (AQUI SE MODIFICA LA CANTIDAD DE FILAS)
            @Override                                                                // Metodo que evita que las celdas sean modificadas por el usuario
            public boolean isCellEditable(int row, int column) {
            return false;
            }
        };
        tablaUnidades = new JTable(filasTablaUnidades);
        tablaUnidades.setRowHeight(30);
        tablaUnidades.getTableHeader().setReorderingAllowed(false);
        tablaUnidades.getTableHeader().setResizingAllowed(false);
        
        JScrollPane tablaConScroll = new JScrollPane(tablaUnidades); //Metodo para garantizar la graficacion y visualizacion de una tabla de N filas
        tablaConScroll.setPreferredSize(new DimensionUIResource(800, 150));
        
        JPanel panelTablaUnidades = CrearSubPanel.subPanel(); // Ingraso la tablas con scroll en un panel para modificar sus dumensiones y ubicacion.
        panelTablaUnidades.add(tablaConScroll);

        // --------------------------------------- disposicion del panel central del Cuerpo(FuncionesConductor) -------------------------------------------- 
        add(panelTablaUnidades);
    }
    public DefaultTableModel getModeloTablaUnidades(){
        return filasTablaUnidades;
    }
}