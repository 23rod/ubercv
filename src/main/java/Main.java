import controlador.Control;
import javax.swing.SwingUtilities;

public class Main { 
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Control().abrirMenuInicio());
    }
}