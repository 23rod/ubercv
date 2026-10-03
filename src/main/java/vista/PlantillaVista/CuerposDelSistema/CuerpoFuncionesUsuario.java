package vista.PlantillaVista.CuerposDelSistema;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.table.DefaultTableModel;

import vista.PlantillaVista.CrearSubPanel;

public class CuerpoFuncionesUsuario extends JPanel {
    
    private JTable tablaRutas;
    private DefaultTableModel filasTablaRutas;
    
    public CuerpoFuncionesUsuario(){
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Extencion a todo lo alto disponible de la pagina
        this.setOpaque(true);
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.setBackground(new ColorUIResource(255,255,255));

        // Creacion de la tabla de rutas
        String[] nombreColumnasRutas = {"Nombre de Ruta", "Tipo de Ruta", "Inicio de Jornada", "Final de Jornada", "Unidades Asignadas(Placa)"};
        filasTablaRutas = new DefaultTableModel(nombreColumnasRutas,3){ // Contenedor "dinamico" de las filas de la tabla (AQUI SE MODIFICA LA CANTIDAD DE FILAS)
            @Override                                                                // Metodo que evita que las celdas sean modificadas por el usuario
            public boolean isCellEditable(int row, int column) {
            return false;
            }
        };
        tablaRutas = new JTable(filasTablaRutas);
        tablaRutas.setRowHeight(30);
        tablaRutas.getTableHeader().setReorderingAllowed(false);
        tablaRutas.getTableHeader().setResizingAllowed(false);
        
        JScrollPane tablaConScrollRutas = new JScrollPane(tablaRutas); //Metodo para garantizar la graficacion y visualizacion de una tabla de N filas
        tablaConScrollRutas.setPreferredSize(new DimensionUIResource(800, 150));
        
        JPanel panelTablaRutas = CrearSubPanel.subPanel(); // Ingraso la tablas con scroll en un panel para modificar sus dumensiones y ubicacion.
        panelTablaRutas.add(tablaConScrollRutas);

        // --------------------------------------- disposicion del panel central del Cuerpo(FuncionesUsuarios) -------------------------------------------- 
        add(panelTablaRutas);
    }
    // ----------------------------------------- Acceso para los controladores ---------------------------------------------------------
    public DefaultTableModel getModeloTablaRutas(){
        return filasTablaRutas;
    }
}
