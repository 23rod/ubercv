import javax.swing.SwingUtilities;
import vista.PlantillaVista.MenuInicio;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MenuInicio().setVisible(true));
    }
}