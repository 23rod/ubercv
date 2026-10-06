package vista.PlantillaVista;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class prueba extends JFrame {

    public prueba() {
        setTitle("Ejemplo de Contraseña Visible");
        setSize(360, 180);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        // 1. Crear el JPasswordField
        JPasswordField passwordField = new JPasswordField(14);
        passwordField.setBorder(null); // Quitar borde para que use el del contenedor
        
        // Guardar el carácter de ocultación por defecto del sistema
        final char defaultEchoChar = passwordField.getEchoChar();

        // 2. Crear el botón toggle (icono/texto del ojo)
        JToggleButton toggleButton = new JToggleButton("👁");
        toggleButton.setPreferredSize(new Dimension(32, 24));
        toggleButton.setFocusPainted(false);
        toggleButton.setContentAreaFilled(false);
        toggleButton.setBorderPainted(false);
        toggleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleButton.setToolTipText("Mostrar/Ocultar contraseña");

        // 3. Alternar visibilidad al presionar
        toggleButton.addActionListener(e -> {
            if (toggleButton.isSelected()) {
                passwordField.setEchoChar((char) 0); // Texto visible
                toggleButton.setText("🙈");         // Cambiar símbolo/icono
            } else {
                passwordField.setEchoChar(defaultEchoChar); // Volver a ocultar
                toggleButton.setText("👁");
            }
        });

        // 4. Contenedor estilizado para simular un solo campo de texto
        JPanel fieldContainer = new JPanel(new BorderLayout());
        fieldContainer.setBackground(Color.WHITE);
        fieldContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                new EmptyBorder(4, 6, 4, 6)
        ));

        fieldContainer.add(passwordField, BorderLayout.CENTER);
        fieldContainer.add(toggleButton, BorderLayout.EAST);

        add(fieldContainer);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new prueba().setVisible(true));
    }

}