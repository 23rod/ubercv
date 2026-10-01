import controlador.Control;
import javax.swing.SwingUtilities;

public class Main { 
    public static void main(String[] args) {
        // Un unico Control para toda la aplicacion: comparte usuarios y sesion entre todas las ventanas
        SwingUtilities.invokeLater(() -> new Control().abrirMenuInicio());
    }
}
