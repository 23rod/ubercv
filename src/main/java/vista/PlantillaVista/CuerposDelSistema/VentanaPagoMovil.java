package vista.PlantillaVista.CuerposDelSistema;

import java.text.NumberFormat;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.DimensionUIResource;

import vista.PlantillaVista.CrearSubPanel;

public class VentanaPagoMovil extends JPanel {
    
    private JComboBox<String> bancoOrigen;
    private JComboBox<String> bancoDestino;
    private JTextField campoCedula;
    private JTextField campoTelefono;
    private JTextField campoReferencia;
    private JFormattedTextField campoMonto;
    private JButton botonConfirmar;
    private JPanel panelAyuda;
    private JScrollPane scrollAyuda;

    public VentanaPagoMovil(){
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Extencion a todo lo alto disponible de la pagina
        this.setOpaque(true);
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.setBackground(new ColorUIResource(255,255,255));

        // Creacion del placeholder Banco Origen
        JLabel plegableOrigenLabel = new JLabel("Elija el banco Origen: ");
        String[] bancoOrigenSeleccionado = {
            "0102 - Banco de Venezuela", "0104 - Banco Venezolano de Crédito", "0105 - Banco Mercantil", "0108 - Banco Provincial", 
            "0114 - Bancaribe", "0115 - Banco Exterior", "0128 - Banco Caroní", "0134 - Banesco", "0137 - Banco Sofitasa", 
            "0138 - Banco Plaza", "0146 - Banco de la Gente Emprendedora C.A.", "0156 - 100% Banco", "0157 - DelSur Banco Universal", 
            "0163 - Banco del Tesoro", "0166 - Banco Agrícola de Venezuela C.A", "0168 - Bancrecer", "0169 - R4, Banco Microfinanciero, C.A.", 
            "0171 - Banco Activo", "0172 - Bancamiga", "0173 - Banco Internacional de Desarrollo", "0174 - Banplus", "0175 - Banco Digital de los Trabajadores", 
            "0177 - Banco de la FANB", "0178 - N58 Banco Digital", "0191 - BNC Banco Nacional de Crédito", "0601 - Instituto Municipal de Crédito Popular"
        };
        bancoOrigen = new JComboBox<>(bancoOrigenSeleccionado);
        JPanel panelPlegableBancoOrigen = CrearSubPanel.subPanel(); 
        panelPlegableBancoOrigen.add(plegableOrigenLabel);
        panelPlegableBancoOrigen.add(bancoOrigen);
    
        // Creacion del placeholder Banco Destino
        JLabel plegableDestinoLabel = new JLabel("Elija el banco Destino: ");
        String[] bancoDestinoSeleccionado = {                                       
            "0102 - Banco de Venezuela", "0105 - Banco Mercantil", "0134 - Banesco", "0163 - Banco del Tesoro", 
            "0175 - Banco Digital de los Trabajadores", "0191 - BNC Banco Nacional de Crédito"
        };
        bancoDestino = new JComboBox<>(bancoDestinoSeleccionado);
        JPanel panelPlegableBancoDestino = CrearSubPanel.subPanel(); 
        panelPlegableBancoDestino.add(plegableDestinoLabel);
        panelPlegableBancoDestino.add(bancoDestino);
        
        // Creacion del placeholder Cedula
        JLabel cedulaLabel = new JLabel("Cédula: "); //disposición en el panel contenedor
        campoCedula = new JTextField(20);
        JPanel panelCedula = CrearSubPanel.subPanel();
        panelCedula.add(cedulaLabel);
        panelCedula.add(campoCedula);

        // Creacion del placeholder Telefono
        JLabel telefonoLabel = new JLabel("Teléfono: "); 
        campoTelefono = new JTextField(20);
        JPanel panelTelefono = CrearSubPanel.subPanel();
        panelTelefono.add(telefonoLabel);
        panelTelefono.add(campoTelefono);

        // Creacion del placeholder Referencia
        JLabel referenciaLabel = new JLabel("Referencia del Pago Movil: "); 
        campoReferencia = new JTextField(20);
        JPanel panelReferencia = CrearSubPanel.subPanel();
        panelReferencia.add(referenciaLabel);
        panelReferencia.add(campoReferencia);

        // Creacion del placeholder Monto 
        JLabel montoLabel = new JLabel("Indique el Monto a Recargar: ");
        NumberFormat formatoEntero = NumberFormat.getIntegerInstance();
        formatoEntero.setGroupingUsed(false); //Evita la division de los numeros por millares para evitar porblemas e compatibilidad en distintos dispositivos

        campoMonto = new JFormattedTextField(formatoEntero);
        campoMonto.setColumns(20);
        campoMonto.setValue(0); // Valor por defecto

        JPanel panelMonto = CrearSubPanel.subPanel(); 
        panelMonto.add(montoLabel);
        panelMonto.add(campoMonto);

        // Creacion del boton confirmar Recarga
        botonConfirmar = new JButton("Confirmar Recarga");
        JPanel panelBotonConfirmar = CrearSubPanel.subPanel();
        panelBotonConfirmar.add(botonConfirmar);
        
        // Creacion de la seccion de ayuda para la realizacion de los Pago Movil
        String ayudaPagoMovil = "<html><body style='width: 650px;'>" + 
            "<h2>Seccion de ayuda para realizar los Pago Movil:</h2>" + 
            "<h2>1. Guía de Formatos y Validación de Datos</h2>" +
            "<p>Restricciones y reglas de entrada para evitar el mensaje de <i>\"Datos de pago móvil incorrectos\"</i>:</p>" +
            "<ul>" +
            "  <li><b>Número de Teléfono:</b> Debe constar de 11 dígitos numéricos (ej. 04121234567), sin guiones, espacios ni código internacional (+58).</li>" +
            "  <li><b>Cédula de Identidad:</b> Debe contener hasta 8 dígitos numéricos, omitiendo letras (V-, E-), puntos o espacios.</li>" +
            "  <li><b>Número de Referencia:</b> Debe ser el código de confirmación emitido por la entidad bancaria, compuesto por 6 a 12 dígitos numéricos según el banco.</li>" +
            "  <li><b>Monto:</b> Debe ser un valor numérico superior a Bs.0 correspondiente al dinero transferido.</li>" +
            "</ul>" +
            "<h2>2. Casos de Falla y Soluciones Frecuentes</h2>" +
            "<ul>" +
            "  <li><b>Referencia ya registrada / duplicada:</b> Cada número de referencia es único y solo puede registrarse una vez en el sistema para evitar solicitudes duplicadas.</li>" +
            "  <li><b>Incoherencia de Banco de Origen:</b> Asegúrate de seleccionar exactamente la entidad bancaria desde la cual ejecutaste el pago móvil.</li>" +
            "  <li><b>Cuentas y Datos Receptor:</b> Verifica los datos oficiales de Pago Móvil de la institución (RIF/Cédula institucional, banco receptor y número de teléfono) para confirmar a dónde enviaste el dinero antes de enviar el formulario.</li>" +
            "  <li><b>Abono no reflejado de inmediato:</b> La solicitud ingresa a un proceso de validación y el saldo se sumará a tu cuenta una vez confirmados los datos por la plataforma.</li>" +
            "</ul>" +
            "<h2>3. Instrucciones Paso a Paso para el Usuario</h2>" +
            "<ol>" +
            "  <li>Realizar el Pago Móvil desde la aplicación de tu banco hacia la cuenta receptora institucional.</li>" +
            "  <li>Guardar el comprobante y copiar el número de referencia generado.</li>" +
            "  <li>Completar los campos en el formulario (banco, teléfono, cédula, referencia y monto).</li>" +
            "  <li>Presionar <b>\"Procesar Recarga\"</b> y verificar el mensaje de confirmación (<i>\"Solicitud aceptada\"</i>).</li>" +
            "</ol>" + 
            "</body></html>";

        JLabel ayudaLabel = new JLabel(ayudaPagoMovil);
        panelAyuda = CrearSubPanel.subPanel();
        panelAyuda.add(ayudaLabel);
        
        scrollAyuda = new JScrollPane(panelAyuda); // Contencion en panel con scroll
        scrollAyuda.setPreferredSize(new DimensionUIResource(700, 200));
        scrollAyuda.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollAyuda.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // --------------------------------------- disposicion del panel central del Cuerpo(VentanaPagoMovil) -------------------------------------------- 
        add(panelPlegableBancoOrigen);
        add(panelPlegableBancoDestino);
        add(panelCedula);
        add(panelTelefono);
        add(panelReferencia);
        add(panelMonto);
        add(panelBotonConfirmar);
        add(scrollAyuda);
    }

    // ----------------------------------------- Acceso para los controladores ---------------------------------------------------------
    public String getOrigenSeleccionado(){
         return (String) bancoOrigen.getSelectedItem(); 
    }
    public String getDestinoSeleccionado(){
         return (String) bancoDestino.getSelectedItem(); 
    }
    public JTextField getCampoCedula(){
        return campoCedula;
    }    
    public JTextField getCampoTelefono(){
        return campoTelefono;
    }    
    public JTextField getCampoReferencia(){
        return campoReferencia;
    }    
    public int getMonto(){
        try { // Confirmacion de que el texto en la casilla sea un digito 
            campoMonto.commitEdit(); // Fuerza a JFormattedTextField a guardar el texto actual como valor
        } catch (java.text.ParseException e) {
            // Si el texto escrito no es un número válido, se ignora la edición no confirmada
        }
        Object valor = campoMonto.getValue();
        if (valor instanceof Number) {
            return ((Number) valor).intValue();
        }
        return 0; // Caso que se confime la recarga sin ingresar un monto
    }  
    public JButton getBotonConfirmar(){
        return botonConfirmar;
    } 
}