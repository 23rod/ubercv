package vista.PlantillaVista.CuerposDelSistema;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class CuerpoConsultarSaldo extends JPanel {
   private JLabel Saldoactual, HistorialOperaciones;// una etiqueta que nos permita mostrar el texto de "Saldo actual" y otra para "Historial de Saldo"
   private int Saldo=0; // una variable que mostrara el saldo pero la inicializamos en 0
   private JTable TablaOperaciones;
   private JButton BotonRecargarMonedero;
   private DefaultTableModel FilasDatosOperacion;

   // funcion para retornar el boton

    public JButton getBotonREcargaMondero(){
        return BotonRecargarMonedero;
    }

    // Funcion para manipular el Saldo

   public void AsignarSaldo( int x){ // Esta funcion es para que puedan asignarle un valor de saldo y poder mostrarlo posteriormete
    this.Saldo= x;
   }
   //-----------------------------------------
   //OPERACIONES PARA EL LLENADO DEL HISTORIAL
   //----------------------------------
   // Para la asignaciond de las filas
   public String[] AsignarDatosFila(String Day, String Hour, String CelularEmisor, String NumOperacion, String MontoRealizado){
    String[] Fila= new String[5];
    Fila[0]=Day;
    Fila[1]=Hour;
    Fila[2]=CelularEmisor;
    Fila[3]=NumOperacion;
    Fila[4]=MontoRealizado;
    return Fila;
   }

   // para Validar que si hayan datos de la fila o no

   public boolean ValidadDatosFila(String a[]){
    int n=4;
    int contador=1;
    for( int i=0; i<n; i++){
        if(a[i]!="") contador++;
    }
    if(contador!=5) return false;
     return true;
   }
   //---------------------------
   // CONSTRUCCION DE LA INTERFAZ
   //-------------------------------
   public CuerpoConsultarSaldo(){
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Extencion a todo lo ancho disponible de la pagina
        this.setOpaque(true);
        this.setBackground(new ColorUIResource(255, 255, 255));
        //AsignarSaldo(x); aqui esta la funcion anterior para poder manejar el valor de la variable

        //Creamos un panel para que esten en la misma altura y no se solapen

            JPanel PanelSuperior = new JPanel();
            PanelSuperior.setLayout(new BoxLayout(PanelSuperior, BoxLayout.X_AXIS));
            PanelSuperior.setOpaque(false); // esto es para que tenga el mismo color de fondo del panel

            // Realizamos el boton para recargar saldo

            BotonRecargarMonedero=new JButton("Recargar Saldo");

            // Realizamos el label para mostrar el Saldo Actual

              Saldoactual= new JLabel("Saldo Acutal: " + Saldo+"   "); // Aparecera en la ventana  como "Saldo Actual: n" donde hasta que no pongan llamen a la funcion la variable sera 0
            
            // Añadimos el boton y el Label
             PanelSuperior.add(BotonRecargarMonedero);
             PanelSuperior.add(Box.createHorizontalGlue()); // esto es para que el boton este alineado a la izquierda
             PanelSuperior.add(Saldoactual);
             this.add(PanelSuperior);
          
        //Creamos etiqueta de Historial

        HistorialOperaciones= new JLabel("Historial de las Operaciones"); // Aparecera en la ventana  como "Saldo Actual: n" donde hasta que no pongan llamen a la funcion la variable sera 0
        HistorialOperaciones.setAlignmentX(JComponent.CENTER_ALIGNMENT); // la alineamos a la derecha
        this.add(HistorialOperaciones);

        // creamos la tabla que contiene la parte de las operaciones

        String[]nombreColumnas={"Dia", "Hora", "Telefono Emisor", "Numero de referencia", "Monto"}; // las columnas
        String[]ValoresFila =AsignarDatosFila("Lunes","03:04","0424123456","245879613","10.000$"); // aqui tendrian que poner las variables correspondientes 
        FilasDatosOperacion = new DefaultTableModel(nombreColumnas,0){ // Se editan las cantidad de filas
            @Override                                                                // Metodo que evita que las celdas sean modificadas por el usuario
            public boolean isCellEditable(int row, int column) {
            return false;
            }
        };
         if( ValidadDatosFila(ValoresFila)){
              FilasDatosOperacion.addRow(ValoresFila); // si estan los datos de la operacion la añadimos las filas
        }
        TablaOperaciones = new JTable(FilasDatosOperacion);
        TablaOperaciones.setRowHeight(30);
        TablaOperaciones.getTableHeader().setReorderingAllowed(false);
        TablaOperaciones.getTableHeader().setResizingAllowed(false);
        TablaOperaciones.getColumnModel().getColumn(0).setPreferredWidth(60);// Dia
        TablaOperaciones.getColumnModel().getColumn(1).setPreferredWidth(60); // hora
        TablaOperaciones.getColumnModel().getColumn(2).setPreferredWidth(120); // Numero Emisor
        TablaOperaciones.getColumnModel().getColumn(3).setPreferredWidth(240); // Numero de referencia
        TablaOperaciones.getColumnModel().getColumn(4).setPreferredWidth(240); // monto

        //Centramos los valores de la tabla

        DefaultTableCellRenderer CentrarCeldas= new DefaultTableCellRenderer(); // variable que nos permite centrar el texto de las celdas
        CentrarCeldas.setHorizontalAlignment(JLabel.CENTER);
        for(int i=0; i<TablaOperaciones.getColumnCount();i++){
            TablaOperaciones.getColumnModel().getColumn(i).setCellRenderer(CentrarCeldas);
        }
        DefaultTableCellRenderer centrarEncabezados = (DefaultTableCellRenderer)TablaOperaciones.getTableHeader().getDefaultRenderer();
        centrarEncabezados.setHorizontalAlignment(JLabel.CENTER);
        
        //Emcapsulamos la tabla

        JScrollPane scrollPane= new JScrollPane(TablaOperaciones); // encapsulamos la tabla para poder mostrarla en la ventana
        DimensionUIResource tamañofijo = new DimensionUIResource(850, 50);
        scrollPane.setPreferredSize(tamañofijo);
        scrollPane.setMaximumSize(tamañofijo);
        scrollPane.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        this.add(scrollPane);
   } 
}
