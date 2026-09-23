import javax.swing.SwingUtilities;

import vista.PlantillaVista.VistaGeneral;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VistaGeneral vista = new VistaGeneral();
            vista.setVisible(true);
        });
    } 
}
