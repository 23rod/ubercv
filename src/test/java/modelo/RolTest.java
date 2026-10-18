package modelo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RolTest {

    @Test
    void traduceLosTextosDelComboDeLaVista() {
        assertEquals(Rol.ESTUDIANTE, Rol.desdeTexto("Estudiante"));
        assertEquals(Rol.EMPLEADO, Rol.desdeTexto("Empleado"));
        assertEquals(Rol.CONDUCTOR, Rol.desdeTexto("Conductor"));
        assertEquals(Rol.ADMIN_TRANSPORTE, Rol.desdeTexto("Admin de Transporte"));
    }

    @Test
    void textoDesconocidoONingunoDevuelveNinguno() {
        assertEquals(Rol.NINGUNO, Rol.desdeTexto("Ninguno"));
        assertEquals(Rol.NINGUNO, Rol.desdeTexto(null));
        assertEquals(Rol.NINGUNO, Rol.desdeTexto("Piloto"));
    }

    @Test
    void laEtiquetaLegibleSePuedeTraducirDeVueltaAlRol() {
        assertEquals("Estudiante", Rol.ESTUDIANTE.getEtiqueta());
        assertEquals("Admin de Transporte", Rol.ADMIN_TRANSPORTE.getEtiqueta());
        assertEquals("Público General", Rol.PUBLICO_GENERAL.getEtiqueta());
        for (Rol rol : Rol.values()) {
            if (rol != Rol.NINGUNO) {
                assertEquals(rol, Rol.desdeTexto(rol.getEtiqueta()));
            }
        }
    }

    @Test
    void elPublicoGeneralSeTraduceConOSinAcento() {
        assertEquals(Rol.PUBLICO_GENERAL, Rol.desdeTexto("Público General"));
        assertEquals(Rol.PUBLICO_GENERAL, Rol.desdeTexto("publico general"));
    }
}
