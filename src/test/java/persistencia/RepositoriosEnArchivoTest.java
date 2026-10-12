package persistencia;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import modelo.EstadoUnidad;
import modelo.Rol;
import modelo.Ruta;
import modelo.TipoRuta;
import modelo.UnidadTransporte;
import modelo.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import persistencia.memoria.DatosSemilla;
import persistencia.memoria.RepositorioRutas;
import persistencia.memoria.RepositorioUnidades;
import persistencia.memoria.RepositorioUsuarios;
import servicio.ServicioFlota;
import servicio.ServicioUsuarios;

/** Los repositorios con archivo guardan y recuperan sus datos; las pruebas usan una carpeta temporal. */
class RepositoriosEnArchivoTest {

    @Test
    @DisplayName("Dado un archivo que no existe, cuando se crea el repositorio, entonces empieza vacío")
    void sinArchivoEmpiezaVacio(@TempDir Path carpeta) {
        assertTrue(new RepositorioUsuarios(carpeta.resolve("usuarios.txt")).listar().isEmpty());
        assertTrue(new RepositorioUnidades(carpeta.resolve("unidades.txt")).listar().isEmpty());
        assertTrue(new RepositorioRutas(carpeta.resolve("rutas.txt")).listar().isEmpty());
    }

    @Test
    @DisplayName("Dado un usuario guardado, cuando se abre otro repositorio sobre el mismo archivo, entonces el usuario sigue ahí")
    void losUsuariosSobrevivenAUnReinicio(@TempDir Path carpeta) {
        Path archivo = carpeta.resolve("datos").resolve("usuarios.txt"); // la subcarpeta se crea sola
        new RepositorioUsuarios(archivo)
                .guardar(new Usuario("ana@ucv.com", "hash", new byte[] {1, 2, 3}, "12345678", Rol.ESTUDIANTE));

        Usuario leido = new RepositorioUsuarios(archivo).buscarPorCorreo("ana@ucv.com");

        assertNotNull(leido);
        assertEquals("12345678", leido.getCedula());
        assertEquals(Rol.ESTUDIANTE, leido.getRol());
        assertArrayEquals(new byte[] {1, 2, 3}, leido.getSal());
    }

    @Test
    @DisplayName("Dado una unidad con ruta y conductor, cuando se reabre el repositorio, entonces conserva todos sus datos")
    void lasUnidadesSobrevivenAUnReinicio(@TempDir Path carpeta) {
        Path archivo = carpeta.resolve("unidades.txt");
        UnidadTransporte unidad = new UnidadTransporte("ABC123", "Yutong", 40, EstadoUnidad.ACTIVO);
        unidad.setNombreRuta("UCV - Altamira");
        unidad.setCedulaConductor("20000003");
        new RepositorioUnidades(archivo).guardar(unidad);

        UnidadTransporte leida = new RepositorioUnidades(archivo).buscarPorPlaca("abc123");

        assertNotNull(leida);
        assertEquals(40, leida.getCapacidad());
        assertEquals(EstadoUnidad.ACTIVO, leida.getEstado());
        assertEquals("UCV - Altamira", leida.getNombreRuta());
        assertEquals("20000003", leida.getCedulaConductor());
    }

    @Test
    @DisplayName("Dado cada rol, estado de unidad y tipo de ruta posibles, cuando se reabre el repositorio, entonces ninguno se pierde ni cambia")
    void todosLosValoresDeLosEnumsSobrevivenAUnReinicio(@TempDir Path carpeta) {
        Path archivoUsuarios = carpeta.resolve("usuarios.txt");
        Path archivoUnidades = carpeta.resolve("unidades.txt");
        Path archivoRutas = carpeta.resolve("rutas.txt");
        RepositorioUsuarios usuarios = new RepositorioUsuarios(archivoUsuarios);
        RepositorioUnidades unidades = new RepositorioUnidades(archivoUnidades);
        RepositorioRutas rutas = new RepositorioRutas(archivoRutas);

        int n = 0;
        for (Rol rol : Rol.values()) {
            usuarios.guardar(new Usuario("u" + n + "@ucv.com", "hash", new byte[16], "1000000" + n, rol));
            n++;
        }
        for (EstadoUnidad estado : EstadoUnidad.values()) {
            unidades.guardar(new UnidadTransporte("PLACA" + estado.ordinal(), "Modelo", 40, estado));
        }
        for (TipoRuta tipo : TipoRuta.values()) {
            rutas.guardar(new Ruta("Ruta " + tipo.name(), tipo, LocalTime.of(6, 0), LocalTime.of(20, 0)));
        }

        RepositorioUsuarios usuariosLeidos = new RepositorioUsuarios(archivoUsuarios);
        n = 0;
        for (Rol rol : Rol.values()) {
            assertEquals(rol, usuariosLeidos.buscarPorCorreo("u" + n + "@ucv.com").getRol(), "rol " + rol);
            n++;
        }
        RepositorioUnidades unidadesLeidas = new RepositorioUnidades(archivoUnidades);
        for (EstadoUnidad estado : EstadoUnidad.values()) {
            UnidadTransporte u = unidadesLeidas.buscarPorPlaca("PLACA" + estado.ordinal());
            assertNotNull(u, "unidad con estado " + estado);
            assertEquals(estado, u.getEstado());
        }
        RepositorioRutas rutasLeidas = new RepositorioRutas(archivoRutas);
        for (TipoRuta tipo : TipoRuta.values()) {
            Ruta r = rutasLeidas.buscarPorNombre("Ruta " + tipo.name());
            assertNotNull(r, "ruta de tipo " + tipo);
            assertEquals(tipo, r.getTipo());
        }
    }

    @Test
    @DisplayName("Dado una ruta guardada, cuando se reabre el repositorio, entonces conserva tipo y horario")
    void lasRutasSobrevivenAUnReinicio(@TempDir Path carpeta) {
        Path archivo = carpeta.resolve("rutas.txt");
        new RepositorioRutas(archivo)
                .guardar(new Ruta("UCV - Guarenas", TipoRuta.EXTRAURBANA, LocalTime.of(5, 30), LocalTime.of(19, 30)));

        Ruta leida = new RepositorioRutas(archivo).buscarPorNombre("ucv - guarenas");

        assertNotNull(leida);
        assertEquals(TipoRuta.EXTRAURBANA, leida.getTipo());
        assertEquals(LocalTime.of(5, 30), leida.getInicioJornada());
        assertEquals(LocalTime.of(19, 30), leida.getFinJornada());
    }

    @Test
    @DisplayName("Dado un guardado, cuando termina, entonces no queda ningún archivo temporal")
    void noQuedaArchivoTemporal(@TempDir Path carpeta) throws Exception {
        new RepositorioUnidades(carpeta.resolve("unidades.txt"))
                .guardar(new UnidadTransporte("ABC123", "Yutong", 40, EstadoUnidad.ACTIVO));

        try (var archivos = Files.list(carpeta)) {
            assertTrue(archivos.noneMatch(p -> p.getFileName().toString().endsWith(".tmp")));
        }
    }

    @Test
    @DisplayName("Dado un repositorio sin archivo, cuando se guarda algo, entonces funciona solo en memoria")
    void sinArchivoFuncionaEnMemoria() {
        RepositorioUnidades repo = new RepositorioUnidades();
        repo.guardar(new UnidadTransporte("ABC123", "Yutong", 40, EstadoUnidad.ACTIVO));
        assertEquals(1, repo.listar().size());
        assertTrue(new RepositorioUnidades().listar().isEmpty()); // otro repositorio en memoria no ve nada
    }

    @Test
    @DisplayName("Dado una unidad en una ruta, cuando el servicio la pasa a mantenimiento, entonces el estado y la liberación sobreviven a un reinicio")
    void cambiarElEstadoDesdeElServicioSePersiste(@TempDir Path carpeta) {
        Path archivo = carpeta.resolve("unidades.txt");
        RepositorioUsuarios repoUsuarios = new RepositorioUsuarios();
        DatosSemilla.sembrarUsuarios(repoUsuarios);
        ServicioUsuarios usuarios = new ServicioUsuarios(repoUsuarios);
        usuarios.iniciarSesion("admin@ucv.com", DatosSemilla.CLAVE_DEMO);
        RepositorioUnidades repo = new RepositorioUnidades(archivo);
        ServicioFlota flota = new ServicioFlota(repo, usuarios);
        flota.registrarUnidad("ABC123", "Yutong", "40", EstadoUnidad.ACTIVO);
        UnidadTransporte unidad = repo.buscarPorPlaca("ABC123");
        unidad.setNombreRuta("UCV - Altamira");
        unidad.setCedulaConductor("20000003");
        repo.actualizar(unidad);

        flota.cambiarEstado("ABC123", EstadoUnidad.EN_MANTENIMIENTO);

        UnidadTransporte leida = new RepositorioUnidades(archivo).buscarPorPlaca("ABC123");
        assertEquals(EstadoUnidad.EN_MANTENIMIENTO, leida.getEstado());
        assertFalse(leida.estaAsignada());
        assertFalse(leida.tieneConductor());
    }

    @Test
    @DisplayName("Dado varias unidades, cuando se consulta por ruta o por conductor, entonces solo se devuelven las que corresponden")
    void lasConsultasPorRutaYPorConductorFiltranBien() {
        RepositorioUnidades repo = new RepositorioUnidades();
        UnidadTransporte a = new UnidadTransporte("AAA111", "Modelo", 40, EstadoUnidad.ACTIVO);
        a.setNombreRuta("UCV - Altamira");
        a.setCedulaConductor("20000003");
        UnidadTransporte b = new UnidadTransporte("BBB222", "Modelo", 35, EstadoUnidad.ACTIVO);
        b.setNombreRuta("ucv - altamira"); // el nombre no distingue mayúsculas
        UnidadTransporte c = new UnidadTransporte("CCC333", "Modelo", 30, EstadoUnidad.ACTIVO);
        repo.guardar(a);
        repo.guardar(b);
        repo.guardar(c);

        assertEquals(2, repo.listarPorRuta("UCV - Altamira").size());
        assertEquals(0, repo.listarPorRuta("Otra ruta").size());
        assertEquals(0, repo.listarPorRuta(null).size());
        assertEquals(1, repo.listarPorConductor("20000003").size());
        assertEquals("AAA111", repo.listarPorConductor("20000003").get(0).getPlaca());
        assertEquals(0, repo.listarPorConductor(null).size());
    }
}
