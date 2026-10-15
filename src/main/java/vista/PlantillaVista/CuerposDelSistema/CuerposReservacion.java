package vista.PlantillaVista.CuerposDelSistema;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font; // libreia que nos permitira redimensionar el Label inicial
public class CuerposReservacion extends JPanel {
   private JTable TablaReservaciones;
   private JLabel LabelUnidad, LabelConductor, LabelHora, LabelRuta, LabelTrayecto; 
   private DefaultTableModel FilasDatosOperacion;
   //Asginamos los valores de la reservacion actual
   public void AgregarDatosReservacionActual(String Placa, String Conductor, String hora, String ruta, String Trayecto ){

    LabelUnidad.setText("Unidad Asignada: "+Placa+" ");
    LabelConductor.setText("Conductor: "+Conductor+" ");
    LabelHora.setText("Hora: "+hora+" ");
    LabelRuta.setText("Ruta: "+ruta+" ");
    LabelTrayecto.setText("Estado: " + Trayecto+" ");
   }
   // Funcion para Colocar los Valores dentro de la tabla
   public String[] AsignarDatosFila(String Unidad, String Conductor, String hora, String Ruta){
    String[] Fila= new String[5];
    Fila[0]=Unidad;
    Fila[1]=Conductor;
    Fila[2]=hora;
    Fila[3]=Ruta;
    return Fila;
   }

    public boolean ValidadDatosFila(String a[]){
    int n=4;
    int contador=0;
    for( int i=0; i<n; i++){
        if(a[i]!="") contador++;
    }
    if(contador!=4) return false;
     return true;
   }
   public CuerposReservacion(){
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Extencion a todo lo ancho disponible de la pagina
        this.setOpaque(true);
        this.setBackground(new ColorUIResource(225, 225, 255));
         //Creamos un panel para poder llevar el boton a la esquina superiro izquierda
        JPanel PanelEncabezado=new JPanel();
        PanelEncabezado.setLayout(new BoxLayout(PanelEncabezado, BoxLayout.X_AXIS));
        PanelEncabezado.setOpaque(false);
        JButton Regresar = new JButton("Regresar"); // creamos el boton
        PanelEncabezado.add(Regresar);
        PanelEncabezado.add(Box.createHorizontalGlue()); // si se mueve esto el boton va pal centro
        this.add(PanelEncabezado);

        //Creracion del label del Encabezado

        JLabel LabelEncabezado= new JLabel("Reservas Realizadas");
        Font MayorTamaño = LabelEncabezado.getFont(); // le ajustamos el tamaño para diferenciarlo del otro label
        LabelEncabezado.setFont(new Font(MayorTamaño.getName(),Font.BOLD,28));
        LabelEncabezado.setAlignmentX(CENTER_ALIGNMENT);
        this.add(LabelEncabezado);

        //Le Damos un espacio con Respecto al siguiente Label y creamos el Label de la Seccion " Trayecto Actual"

        this.add(Box.createRigidArea(new Dimension(0,20)));
        JLabel LabelReservacionActual = new JLabel("Trayecto Actual");
        MayorTamaño = LabelReservacionActual.getFont();
        LabelReservacionActual.setFont(new Font(MayorTamaño.getName(),MayorTamaño.getStyle(),20));
        LabelReservacionActual.setAlignmentX(CENTER_ALIGNMENT); // Para que el label Este en el centro
         this.add(LabelReservacionActual);
         this.add(Box.createRigidArea(new Dimension(0,35))); // ponemos esto para poder separarlo del label del trayecto actual


        // Creacion de un panel que tendra los Jlabel para las rutas actuales

        JPanel PanelContenedor = new JPanel(); // panel contenedor
        PanelContenedor.setLayout(new BoxLayout(PanelContenedor,BoxLayout.Y_AXIS));
        PanelContenedor.setOpaque(true);
        PanelContenedor.setBackground(new Color(126,177,245)); // para poder diferenciar donde estara ubicado
        PanelContenedor.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 45, 0, 45)); // para que los label no se peguen al panel    
        Dimension DimensionFija=new Dimension(900,50); // la dimension del panel para no ocupar toda la ventana
        PanelContenedor.setPreferredSize(DimensionFija);
        PanelContenedor.setMaximumSize(DimensionFija);
        PanelContenedor.setMinimumSize(DimensionFija);
        JPanel FilaSuperior = new JPanel(); // este panel es para poder separar las variables "fijas" las que no se alteran del label del trayecto que cambiara durante el trayecto
        FilaSuperior.setLayout(new BoxLayout(FilaSuperior,BoxLayout.X_AXIS));
        FilaSuperior.setOpaque(false);
        LabelUnidad = new JLabel();
        LabelConductor=new JLabel();
        LabelHora=new JLabel();
        LabelRuta = new JLabel();
        LabelTrayecto = new JLabel();
        AgregarDatosReservacionActual("1234556","Arturo","9:00","ExtraUrbana","En parada"); // estos valores son de ejemplo se pueden modificar despues
        FilaSuperior.add(LabelUnidad);
        FilaSuperior.add(Box.createHorizontalGlue()); // para separar del label siguiente
        FilaSuperior.add(LabelConductor);
        FilaSuperior.add(Box.createHorizontalGlue());
        FilaSuperior.add(LabelHora);
        FilaSuperior.add(Box.createHorizontalGlue());
        FilaSuperior.add(LabelRuta);
        PanelContenedor.add(FilaSuperior);
        PanelContenedor.add(Box.createRigidArea(new Dimension(0,6))); // esto es para poder separar un poco el panel de las variables "fijas" del label del trayecto
        LabelTrayecto.setAlignmentX(CENTER_ALIGNMENT); // centraamos el label del trayecto
        PanelContenedor.add(LabelTrayecto);
        this.add(PanelContenedor);

        //Creacion de la tabla con las Demas reservas
       
        String[]nombreColumnas={"Unidad Asignada", "Conductor", "Hora de Salida", "Ruta"}; // las columnas
        String[]ValoresFila =AsignarDatosFila("123456","29278836","0424123456","245879613"); // aqui tendrian que poner las variables correspondientes 
        FilasDatosOperacion = new DefaultTableModel(nombreColumnas,0){ // Se editan las cantidad de filas
            @Override                                                                // Metodo que evita que las celdas sean modificadas por el usuario
            public boolean isCellEditable(int row, int column) {
            return false;
            }
        };
         if( ValidadDatosFila(ValoresFila)){
              FilasDatosOperacion.addRow(ValoresFila); // si estan los datos de la operacion la añadimos las filas
        }
        TablaReservaciones = new JTable(FilasDatosOperacion);
        TablaReservaciones.setRowHeight(30);
        TablaReservaciones.getTableHeader().setReorderingAllowed(false);
        TablaReservaciones.getTableHeader().setResizingAllowed(false);
        TablaReservaciones.getColumnModel().getColumn(0).setPreferredWidth(90);// Dia
        TablaReservaciones.getColumnModel().getColumn(1).setPreferredWidth(120); // hora
        TablaReservaciones.getColumnModel().getColumn(2).setPreferredWidth(100); // Numero Emisor
        TablaReservaciones.getColumnModel().getColumn(3).setPreferredWidth(180); // Numero de referencia

        //Centramos los valores de la tabla

        DefaultTableCellRenderer CentrarCeldas= new DefaultTableCellRenderer(); // variable que nos permite centrar el texto de las celdas
        CentrarCeldas.setHorizontalAlignment(JLabel.CENTER);
        for(int i=0; i<TablaReservaciones.getColumnCount();i++){
            TablaReservaciones.getColumnModel().getColumn(i).setCellRenderer(CentrarCeldas);
        }
        DefaultTableCellRenderer centrarEncabezados = (DefaultTableCellRenderer)TablaReservaciones.getTableHeader().getDefaultRenderer();
        centrarEncabezados.setHorizontalAlignment(JLabel.CENTER);
        
        //Emcapsulamos la tabla

        JScrollPane scrollPane= new JScrollPane(TablaReservaciones); // encapsulamos la tabla para poder mostrarla en la ventana
        DimensionUIResource tamañofijo = new DimensionUIResource(900, 250);
        scrollPane.setPreferredSize(tamañofijo);
        scrollPane.setMaximumSize(tamañofijo);
        scrollPane.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        this.add(scrollPane);

        
       JButton BtnCancelarReservacion = new JButton("Cancelar Reservacion");
       BtnCancelarReservacion.setAlignmentX(CENTER_ALIGNMENT);
 // creamos la accion para la parte de la
BtnCancelarReservacion.addActionListener(e -> {
    // Se crea el campo de texto que pedirá el dato
    JTextField txtDato = new JTextField(15);
    
    // Agrupar el texto de alerta y el textfield en un arreglo de objetos
    Object[] mensaje = {
        "ALERTA! Estas eliminando una reservacion, porfavor ingresa la unidad que quieres eliminar",
        txtDato
    };

    // Creamos y definimos  los nombres de los botones personalizados
    Object[] botones = {"Confirmar", "Negar"};

    // Mostrarmos el diálogo de opciones
    int seleccion = JOptionPane.showOptionDialog(
        BtnCancelarReservacion, // Componente padre
        mensaje,                // Contenido (Texto + JTextField)
        "Eliminar Reservación", // Título de la ventana
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE,
        null,                   // Icono por defecto
        botones,                // Arreglo con los botones personalizados
        botones[1]              // Botón seleccionado por defecto (Negar)
    );

    // Evaluamos qué botón presionó el usuario
    if (seleccion == 0) { 
        // Botón "Confirmar" (índice 0)
        String dato = txtDato.getText(); // Obtenemos el dato del textfield
        
        // Aquí se abre el otro panel
        JOptionPane.showMessageDialog(this, "Acción confirmada. Dato ingresado: " + dato);
        
    } else { 
        // Botón "Negar" (índice 1) o si cierran la ventana con la "X"
        // Simplemente sale del panel sin hacer nada
        System.out.println("Operación cancelada.");
    }
});

this.add(BtnCancelarReservacion);
        this.add(BtnCancelarReservacion);
}
}