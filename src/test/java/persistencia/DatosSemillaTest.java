package persistencia;

import static org.junit.jupiter.api.Assertions.*;

import modelo.Mensajes;
import modelo.Rol;
import org.junit.jupiter.api.Test;
import persistencia.memoria.DatosSemilla;
import persistencia.memoria.RepositorioUsuarios;
import servicio.ServicioUsuarios;

class DatosSemillaTest {

    @Test
    void lasCuentasDeDemoPuedenIniciarSesionConSuRol() {
        RepositorioUsuarios repo = new RepositorioUsuarios();
        DatosSemilla.sembrarUsuarios(repo);
        ServicioUsuarios servicio = new ServicioUsuarios(repo);

        assertEquals(Rol.ADMIN_TRANSPORTE, servicio.iniciarSesion("admin@ucv.com", DatosSemilla.CLAVE_DEMO).getValor());
        assertEquals(Rol.ESTUDIANTE, servicio.iniciarSesion("estudiante@ucv.com", DatosSemilla.CLAVE_DEMO).getValor());
        assertEquals(Rol.EMPLEADO, servicio.iniciarSesion("empleado@ucv.com", DatosSemilla.CLAVE_DEMO).getValor());
        assertEquals(Rol.CONDUCTOR, servicio.iniciarSesion("conductor@ucv.com", DatosSemilla.CLAVE_DEMO).getValor());
    }

    @Test
    void noSePuedeRegistrarUnaCedulaYaSembrada() {
        RepositorioUsuarios repo = new RepositorioUsuarios();
        DatosSemilla.sembrarUsuarios(repo);
        ServicioUsuarios servicio = new ServicioUsuarios(repo);

        var r = servicio.registrar("otro@ucv.com", "Clave1234", "Clave1234", "12345678", Rol.ADMIN_TRANSPORTE);
        assertEquals(Mensajes.USUARIO_EXISTENTE, r.getMensaje());
    }

    @Test
    void elPadronConoceLosRolesYDevuelveNingunoParaDesconocidos() {
        assertEquals(Rol.CONDUCTOR, DatosSemilla.validarEnPadron("10000004"));
        assertEquals(Rol.NINGUNO, DatosSemilla.validarEnPadron("99999999"));
        assertEquals(Rol.NINGUNO, DatosSemilla.validarEnPadron(null));
    }

    @Test
    void laFlotaDeDemoTieneUnidadesYRutasYUnaUnidadAsignada() {
        persistencia.memoria.RepositorioUnidades unidades = new persistencia.memoria.RepositorioUnidades();
        persistencia.memoria.RepositorioRutas rutas = new persistencia.memoria.RepositorioRutas();
        DatosSemilla.sembrarFlota(unidades, rutas);

        assertEquals(3, unidades.listar().size());
        assertEquals(2, rutas.listar().size());
        assertEquals("UCV - Altamira", unidades.buscarPorPlaca("ABC123").getNombreRuta());
        assertEquals(modelo.EstadoUnidad.EN_MANTENIMIENTO, unidades.buscarPorPlaca("GHI789").getEstado());
    }
}
