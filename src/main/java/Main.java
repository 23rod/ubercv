import javax.swing.SwingUtilities;

import vista.PlantillaVista.VistaConcreta;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VistaConcreta vista = new VistaConcreta();
            vista.setVisible(true);
        });
    } 
}
