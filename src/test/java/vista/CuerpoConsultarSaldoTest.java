package vista;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Component;
import java.awt.Container;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vista.PlantillaVista.CuerposDelSistema.CuerpoConsultarSaldo;

/** La pantalla de saldo debe poder mostrar el saldo y el historial que le pasa el controlador. */
class CuerpoConsultarSaldoTest {

    private static String textoDeLaEtiquetaDeSaldo(Container contenedor) {
        for (Component c : contenedor.getComponents()) {
            if (c instanceof JLabel etiqueta && etiqueta.getText() != null && etiqueta.getText().startsWith("Saldo")) {
                return etiqueta.getText();
            }
            if (c instanceof Container hijo) {
                String texto = textoDeLaEtiquetaDeSaldo(hijo);
                if (texto != null) {
                    return texto;
                }
            }
        }
        return null;
    }

    @Test
    @DisplayName("Dado una pantalla recién creada, cuando se abre, entonces el historial está vacío (sin filas de ejemplo)")
    void elHistorialEmpiezaVacio() {
        assertEquals(0, new CuerpoConsultarSaldo().getModeloTablaOperaciones().getRowCount());
    }

    @Test
    @DisplayName("Dado el saldo formateado por el controlador, cuando se muestra, entonces la etiqueta que ve el usuario cambia")
    void mostrarSaldoActualizaLaEtiqueta() {
        CuerpoConsultarSaldo cuerpo = new CuerpoConsultarSaldo();

        cuerpo.mostrarSaldo("125.50");

        String texto = textoDeLaEtiquetaDeSaldo(cuerpo);
        assertNotNull(texto);
        assertTrue(texto.startsWith("Saldo Actual:"), texto);
        assertTrue(texto.contains("125.50"), texto);
    }

    @Test
    @DisplayName("Dado el método anterior AsignarSaldo, cuando se usa, entonces también actualiza la etiqueta")
    void asignarSaldoTambienActualizaLaEtiqueta() {
        CuerpoConsultarSaldo cuerpo = new CuerpoConsultarSaldo();
        cuerpo.AsignarSaldo(40.0);
        assertTrue(textoDeLaEtiquetaDeSaldo(cuerpo).contains("40.0"));
    }

    @Test
    @DisplayName("Dado el historial de operaciones, cuando se consultan sus columnas, entonces describen recargas, cobros y reintegros")
    void lasColumnasDelHistorial() {
        DefaultTableModel modelo = new CuerpoConsultarSaldo().getModeloTablaOperaciones();
        assertEquals(5, modelo.getColumnCount());
        assertEquals("Tipo", modelo.getColumnName(2));
        assertEquals("Detalle", modelo.getColumnName(3));
        assertEquals("Monto", modelo.getColumnName(4));
        modelo.addRow(new Object[] {"14/10/2026", "11:30", "Recarga", "Recarga por pago móvil", "+25.00"});
        assertEquals(1, modelo.getRowCount());
    }
}
