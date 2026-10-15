package vista.PlantillaVista.CuerposDelSistema;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.DimensionUIResource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import vista.PlantillaVista.CrearSubPanel;

public class VentanaPagoMovil extends JPanel {
    
    private JComboBox<String> bancoOrigen;
    private JComboBox<String> bancoDestino;
    private JComboBox<String> comboCodigoArea;
    private JTextField campoCedula;
    private JTextField campoTelefono;
    private JTextField campoReferencia;
    private JTextField campoMonto;
    private JButton botonConfirmar;
    private JPanel panelAyuda;
    private JScrollPane scrollAyuda;
    private JLabel ayudaLabel;

    public VentanaPagoMovil(){
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Extensión a todo lo alto disponible de la página
        this.setOpaque(true);
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.setBackground(new ColorUIResource(255,255,255));

        // Creación del placeholder Banco Origen
        JLabel plegableOrigenLabel = new JLabel("Elija el banco de origen: ");
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
    
        // Creación del placeholder Banco Destino
        JLabel plegableDestinoLabel = new JLabel("Elija el banco de destino: ");
        String[] bancoDestinoSeleccionado = {                                       
            "0102 - Banco de Venezuela", "0105 - Banco Mercantil", "0134 - Banesco", "0163 - Banco del Tesoro", 
            "0175 - Banco Digital de los Trabajadores", "0191 - BNC Banco Nacional de Crédito"
        };
        bancoDestino = new JComboBox<>(bancoDestinoSeleccionado);
        JPanel panelPlegableBancoDestino = CrearSubPanel.subPanel(); 
        panelPlegableBancoDestino.add(plegableDestinoLabel);
        panelPlegableBancoDestino.add(bancoDestino);
        
        // Creación del placeholder Cédula
        JLabel cedulaLabel = new JLabel("Cédula: "); // Disposición en el panel contenedor
        campoCedula = new JTextField(20);
        JPanel panelCedula = CrearSubPanel.subPanel();
        panelCedula.add(cedulaLabel);
        panelCedula.add(campoCedula);

        // Creación del placeholder Teléfono con JComboBox para código de área (anchura limitada)
        JLabel telefonoLabel = new JLabel("Teléfono: "); 
        String[] codigosArea = {"0412", "0414", "0416", "0424", "0426"};
        comboCodigoArea = new JComboBox<>(codigosArea);
        comboCodigoArea.setMaximumSize(new DimensionUIResource(80, 25)); // Anchura proporcional para 4-6 dígitos
        comboCodigoArea.setPreferredSize(new DimensionUIResource(80, 25));
        
        campoTelefono = new JTextField(15);
        JPanel panelTelefono = CrearSubPanel.subPanel();
        panelTelefono.add(telefonoLabel);
        panelTelefono.add(comboCodigoArea);
        panelTelefono.add(campoTelefono);

        // Creación del placeholder Referencia
        JLabel referenciaLabel = new JLabel("Referencia del pago móvil: "); 
        campoReferencia = new JTextField(20);
        JPanel panelReferencia = CrearSubPanel.subPanel();
        panelReferencia.add(referenciaLabel);
        panelReferencia.add(campoReferencia);

        // Creación del placeholder Monto 
        JLabel montoLabel = new JLabel("Indique el monto a recargar: ");
        campoMonto = new JTextField(20);
        JPanel panelMonto = CrearSubPanel.subPanel(); 
        panelMonto.add(montoLabel);
        panelMonto.add(campoMonto);

        // Creación del botón confirmar Recarga
        botonConfirmar = new JButton("Confirmar recarga");
        JPanel panelBotonConfirmar = CrearSubPanel.subPanel();
        panelBotonConfirmar.add(botonConfirmar);
        
        // Mapa con los datos ficticios de pago móvil para cada banco destino
        Map<String, String> datosBancosDestino = new HashMap<>();
        datosBancosDestino.put("0102 - Banco de Venezuela", "<p><b>Banco:</b> 0102 - Banco de Venezuela<br><b>Teléfono:</b> 0414-1111111<br><b>Cédula/RIF:</b> J-00000001-0</p>");
        datosBancosDestino.put("0105 - Banco Mercantil", "<p><b>Banco:</b> 0105 - Banco Mercantil<br><b>Teléfono:</b> 0412-2222222<br><b>Cédula/RIF:</b> J-00000002-0</p>");
        datosBancosDestino.put("0134 - Banesco", "<p><b>Banco:</b> 0134 - Banesco<br><b>Teléfono:</b> 0414-3333333<br><b>Cédula/RIF:</b> J-00000003-0</p>");
        datosBancosDestino.put("0163 - Banco del Tesoro", "<p><b>Banco:</b> 0163 - Banco del Tesoro<br><b>Teléfono:</b> 0416-4444444<br><b>Cédula/RIF:</b> J-00000004-0</p>");
        datosBancosDestino.put("0175 - Banco Digital de los Trabajadores", "<p><b>Banco:</b> 0175 - Banco Digital de los Trabajadores<br><b>Teléfono:</b> 0424-5555555<br><b>Cédula/RIF:</b> J-00000005-0</p>");
        datosBancosDestino.put("0191 - BNC Banco Nacional de Crédito", "<p><b>Banco:</b> 0191 - BNC Banco Nacional de Crédito<br><b>Teléfono:</b> 0412-6666666<br><b>Cédula/RIF:</b> J-00000006-0</p>");

        // Creación de la sección de ayuda para la realización de los pagos móviles
        String estructuraAyudaBase = "<html><body style='width: 650px;'>" + 
            "<h2>Sección de ayuda para realizar el pago móvil:</h2>" + 
            "<h2>1. Datos de los bancos disponibles de la universidad</h2>" + 
            "<p>(Elija el banco de destino para ver los datos a donde realizar el pago móvil)</p>" +
            "<div id='datosBancoContainer'>%s</div>" +
            "<h2>2. Instrucciones paso a paso para el usuario</h2>" +
            "<ol>" +
            "  <li>Realizar el pago móvil desde la aplicación de tu banco hacia la cuenta receptora institucional.</li>" +
            "  <li>Guardar el comprobante y copiar el número de referencia generado.</li>" +
            "  <li>Completar los campos en el formulario (banco, teléfono, cédula, referencia y monto).</li>" +
            "  <li>Presionar <b>\"Confirmar recarga\"</b> y verificar el mensaje de confirmación (<i>\"Solicitud aceptada\"</i>).</li>" +
            "</ol>" +
            "<h2>3. Casos de falla y soluciones frecuentes</h2>" +
            "<ul>" +
            "  <li><b>Referencia ya registrada / duplicada:</b> Cada número de referencia es único y solo puede registrarse una vez en el sistema para evitar solicitudes duplicadas.</li>" +
            "  <li><b>Incoherencia de banco de origen:</b> Asegúrate de seleccionar exactamente la entidad bancaria desde la cual ejecutaste el pago móvil.</li>" +
            "  <li><b>Cuentas y datos del receptor:</b> Verifica los datos oficiales de pago móvil de la institución (RIF/Cédula institucional, banco receptor y número de teléfono) para confirmar a dónde enviaste el dinero antes de enviar el formulario.</li>" +
            "  <li><b>El abono se reflejará de inmediato.</b></li>" +
            "</ul>" +
            "<h2>4. Guía de formatos y validación de datos</h2>" +
            "<ul>" +
            "<p>Restricciones y reglas de entrada para evitar el mensaje de <i>\"Datos de pago móvil incorrectos\"</i>:</p>" +
            "  <li><b>Número de teléfono:</b> Debe constar de 11 dígitos numéricos (ej. 04121234567), sin guiones, espacios ni código internacional (+58).</li>" +
            "  <li><b>Cédula de identidad:</b> Debe contener de 5 a 10 dígitos numéricos, omitiendo letras (V-, E-), puntos o espacios.</li>" +
            "  <li><b>Número de referencia:</b> Debe ser el código de confirmación emitido por la entidad bancaria, compuesto por 6 a 12 dígitos numéricos según el banco.</li>" +
            "  <li><b>Monto:</b> Debe ser un valor numérico superior a Bs. 0 correspondiente al dinero transferido.</li>" +
            "</ul>" + 
            "</body></html>";

        // Obtener el banco seleccionado inicialmente
        String bancoInicial = (String) bancoDestino.getSelectedItem();
        String datosIniciales = datosBancosDestino.getOrDefault(bancoInicial, "<p>Seleccione un banco destino.</p>");

        ayudaLabel = new JLabel(String.format(estructuraAyudaBase, datosIniciales));
        
        // Listener para actualizar los datos del banco destino en tiempo real dentro del panel de ayuda
        bancoDestino.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String seleccionado = (String) bancoDestino.getSelectedItem();
                String infoBanco = datosBancosDestino.getOrDefault(seleccionado, "<p>Seleccione un banco destino.</p>");
                ayudaLabel.setText(String.format(estructuraAyudaBase, infoBanco));
            }
        });

        panelAyuda = CrearSubPanel.subPanel();
        panelAyuda.add(ayudaLabel);
        
        scrollAyuda = new JScrollPane(panelAyuda); // Contención en panel con scroll
        scrollAyuda.setPreferredSize(new DimensionUIResource(700, 200));
        scrollAyuda.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollAyuda.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // --------------------------------------- Disposición del panel central del cuerpo (VentanaPagoMovil) -------------------------------------------- 
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
        // Retorna un JTextField que une el código de área seleccionado con el campo del número,
        // preservando exactamente el formato de salida esperado por las pruebas y controladores existentes.
        return new JTextField(comboCodigoArea.getSelectedItem() + campoTelefono.getText()) {
            @Override
            public String getText() {
                return comboCodigoArea.getSelectedItem() + campoTelefono.getText();
            }
        };
    }    
    public JTextField getCampoReferencia(){
        return campoReferencia;
    }    
    public String getMonto(){
        return campoMonto.getText();
    }  
    public JButton getBotonConfirmar(){
        return botonConfirmar;
    } 
}