package vista;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import vista.PlantillaVista.EncabezadoGeneral;

class EncabezadoGeneralTest {

    @Test
    void sinSesionTodoElPanelDeUsuarioEstaOculto() {
        EncabezadoGeneral encabezado = new EncabezadoGeneral();

        assertFalse(encabezado.isInfoUsuarioVisible());
    }

    @Test
    void conSesionMuestraUsuarioRolYSaldoSinInventarUnNombre() {
        EncabezadoGeneral encabezado = new EncabezadoGeneral();

        encabezado.setInfoUsuario("estudiante@ucv.com", "10000001", "Estudiante", "15.00");
        encabezado.mostrarInfoUsuario(true);

        assertTrue(encabezado.isInfoUsuarioVisible());
        assertEquals("Usuario: estudiante@ucv.com", encabezado.getLabelNombre().getText());
        assertEquals("Cédula: 10000001", encabezado.getLabelCedula().getText());
        assertEquals("Rol: Estudiante", encabezado.getLabelRol().getText());
        assertEquals("Saldo: 15.00", encabezado.getLabelSaldo().getText());
    }

    @Test
    void sinCedulaLaEtiquetaQuedaVaciaSinFallar() {
        EncabezadoGeneral encabezado = new EncabezadoGeneral();

        encabezado.setInfoUsuario("estudiante@ucv.com", "Estudiante", "15.00");
        assertEquals("Cédula: ", encabezado.getLabelCedula().getText());

        encabezado.setInfoUsuario("estudiante@ucv.com", null, "Estudiante", "15.00");
        assertEquals("Cédula: ", encabezado.getLabelCedula().getText());
    }

    @Test
    void alCerrarSesionElPanelCompletoSeVuelveAOcultar() {
        EncabezadoGeneral encabezado = new EncabezadoGeneral();
        encabezado.mostrarInfoUsuario(true);

        encabezado.mostrarInfoUsuario(false);

        assertFalse(encabezado.isInfoUsuarioVisible());
    }

    @Test
    void elSaldoSePuedeOcultarParaRolesQueNoPaganPasajes() {
        EncabezadoGeneral encabezado = new EncabezadoGeneral();

        encabezado.mostrarSaldo(false);

        assertFalse(encabezado.getLabelSaldo().isVisible());
    }
}
