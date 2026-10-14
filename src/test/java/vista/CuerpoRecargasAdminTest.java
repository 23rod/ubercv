package vista;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vista.PlantillaVista.CuerposDelSistema.CuerpoRecargasAdmin;

/** Pantalla del administrador con las operaciones de pago móvil de cada usuario (HU05). */
class CuerpoRecargasAdminTest {

    @Test
    @DisplayName("Dado la pantalla recién abierta, cuando no se ha elegido a nadie, entonces no hay usuario seleccionado y la tabla está vacía")
    void alAbrirNoHayUsuarioElegido() {
        CuerpoRecargasAdmin cuerpo = new CuerpoRecargasAdmin();
        assertNull(cuerpo.getUsuarioSeleccionado());
        assertEquals(0, cuerpo.getModeloTabla().getRowCount());
    }

    @Test
    @DisplayName("Dado los usuarios con operaciones, cuando se cargan, entonces el selector ofrece 'Ninguno' y cada correo, y devuelve el elegido")
    void elSelectorOfreceLosUsuariosYDevuelveElElegido() {
        CuerpoRecargasAdmin cuerpo = new CuerpoRecargasAdmin();

        cuerpo.actualizarUsuarios(List.of("ana@ucv.com", "luis@ucv.com"));

        assertEquals(3, cuerpo.getPlegableUsuarios().getItemCount());
        assertEquals("Ninguno", cuerpo.getPlegableUsuarios().getItemAt(0));
        assertNull(cuerpo.getUsuarioSeleccionado()); // sigue en "Ninguno"
        cuerpo.getPlegableUsuarios().setSelectedItem("luis@ucv.com");
        assertEquals("luis@ucv.com", cuerpo.getUsuarioSeleccionado());
    }

    @Test
    @DisplayName("Dado la tabla de operaciones, cuando se consulta, entonces tiene 9 columnas y no se puede editar")
    void laTablaTieneLasColumnasEsperadasYNoEsEditable() {
        CuerpoRecargasAdmin cuerpo = new CuerpoRecargasAdmin();
        assertEquals(9, cuerpo.getModeloTabla().getColumnCount());
        assertEquals("Referencia", cuerpo.getModeloTabla().getColumnName(6));
        assertEquals("Resultado", cuerpo.getModeloTabla().getColumnName(8));
        cuerpo.getModeloTabla().addRow(new Object[9]);
        assertFalse(cuerpo.getModeloTabla().isCellEditable(0, 0));
    }
}
