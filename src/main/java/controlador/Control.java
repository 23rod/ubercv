package controlador;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Window;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import modelo.EstadoUnidad;
import modelo.Mensajes;
import modelo.Resultado;
import modelo.Rol;
import modelo.Ruta;
import modelo.TipoRuta;
import modelo.UnidadTransporte;
import modelo.Usuario;
import persistencia.memoria.DatosSemilla;
import persistencia.memoria.RepositorioRutas;
import persistencia.memoria.RepositorioUnidades;
import persistencia.memoria.RepositorioUsuarios;
import servicio.ServicioFlota;
import servicio.ServicioItinerarios;
import servicio.ServicioUsuarios;
import vista.PlantillaVista.MenuInicio;
import vista.PlantillaVista.PanelEstadoUnidad;
import vista.PlantillaVista.PanelGestionRutas;
import vista.PlantillaVista.VistaConcreta;
import vista.PlantillaVista.CuerposDelSistema.CuerpoFuncionesAdmin;
import vista.PlantillaVista.CuerposDelSistema.CuerpoFuncionesConductor;
import vista.PlantillaVista.CuerposDelSistema.CuerpoFuncionesUsuario;
import vista.PlantillaVista.CuerposDelSistema.CuerpoRegistro;
import vista.PlantillaVista.CuerposDelSistema.CuerpoRegistroRuta;
import vista.PlantillaVista.CuerposDelSistema.CuerpoRegistroUnidad;

/**
 * Puente entre las vistas Swing y la lógica. Las vistas solo llaman a estos métodos;
 * la lógica de negocio vive en los servicios (que no conocen Swing).
 * Main crea UN solo Control y se lo pasa a las ventanas, así todas comparten usuarios y sesión.
 */
public class Control {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final ServicioUsuarios servicioUsuarios;
    private final ServicioFlota servicioFlota;
    private final ServicioItinerarios servicioItinerarios;

    public Control() {
        RepositorioUsuarios repositorioUsuarios = new RepositorioUsuarios();
        DatosSemilla.sembrarUsuarios(repositorioUsuarios); // cuentas de demo
        this.servicioUsuarios = new ServicioUsuarios(repositorioUsuarios);

        RepositorioUnidades repositorioUnidades = new RepositorioUnidades();
        RepositorioRutas repositorioRutas = new RepositorioRutas();
        DatosSemilla.sembrarFlota(repositorioUnidades, repositorioRutas); // unidades y rutas de demo
        this.servicioFlota = new ServicioFlota(repositorioUnidades, servicioUsuarios);
        this.servicioItinerarios = new ServicioItinerarios(repositorioRutas, repositorioUnidades, servicioUsuarios);
    }

    // ------------------------------------------------------------------ Lógica
    public Resultado<Rol> iniciarSesion(String correo, String clave) {
        return servicioUsuarios.iniciarSesion(correo, clave);
    }

    public Resultado<Void> registrarUsuario(String correo, String clave, String confirmacion, String cedula, Rol rol) {
        return servicioUsuarios.registrar(correo, clave, confirmacion, cedula, rol);
    }

    public Rol getRolSesion() {
        return servicioUsuarios.getRolSesion();
    }

    // -------------------------------------------------------------- Navegación
    /** Pantalla de bienvenida (MenuInicio). */
    public void abrirMenuInicio() {
        mostrar(new MenuInicio(this));
    }

    /** HU01: ventana de registro. Al registrar con éxito regresa al menú principal. */
    public void abrirRegistro() {
        CuerpoRegistro cuerpo = new CuerpoRegistro();
        VistaConcreta vista = new VistaConcreta(conBotonVolver("Registro de Usuario", cuerpo, null, this::abrirMenuInicio));
        vista.getRootPane().setDefaultButton(cuerpo.getBotonConfirmar());
        prepararVentana(vista, false); // sin sesión: el botón Cerrar Sesión se oculta
        vista.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        cuerpo.getBotonConfirmar().addActionListener(e -> {
            Resultado<Void> r = registrarUsuario(
                    cuerpo.getCampoCorreo().getText(),
                    new String(cuerpo.getCampoContrasenia().getPassword()),
                    new String(cuerpo.getCampoConfirmarContrasenia().getPassword()),
                    cuerpo.getCampoCedula().getText(),
                    Rol.desdeTexto(cuerpo.getRolSeleccionado()));
            JOptionPane.showMessageDialog(vista, r.getMensaje());
            if (r.isOk()) {
                iniciarSesion(cuerpo.getCampoCorreo().getText(), new String(cuerpo.getCampoContrasenia().getPassword()));
                abrirVentanaPrincipal();
            }
        });
        vista.setVisible(true);
    }

    /** HU02: tras iniciar sesión, la pantalla depende del rol. */
    public void abrirVentanaPrincipal() {
        Rol rol = getRolSesion();
        if (rol == Rol.NINGUNO) { // sin sesión no hay ventana principal
            abrirMenuInicio();
            return;
        }

        CuerpoFuncionesAdmin menuAdmin = null;
        JPanel cuerpo;
        switch (rol) {
            case ADMIN_TRANSPORTE:
                menuAdmin = new CuerpoFuncionesAdmin();
                cuerpo = menuAdmin;
                break;
            case CONDUCTOR:
                CuerpoFuncionesConductor menuConductor = new CuerpoFuncionesConductor();
                llenarTablaConductor(menuConductor.getModeloTablaUnidades());
                cuerpo = menuConductor;
                break;
            default: // ESTUDIANTE y EMPLEADO
                CuerpoFuncionesUsuario menuUsuario = new CuerpoFuncionesUsuario();
                llenarTablaRutas(menuUsuario.getModeloTablaRutas(), false);
                cuerpo = menuUsuario;
        }

        VistaConcreta vista = new VistaConcreta(cuerpo);
        prepararVentana(vista, true);
        vista.getEncabezadoGeneral().getBotonCerrarSesion().addActionListener(e -> cerrarSesion());
        if (menuAdmin != null) {
            conectarMenuAdmin(vista, menuAdmin);
        }
        mostrar(vista);
    }

    public void cerrarSesion() {
        servicioUsuarios.cerrarSesion();
        abrirMenuInicio();
    }

    // ----------------------------------------------------------------- Interno
    /** Menú del admin: Flota e Itinerarios (solo ADMIN_TRANSPORTE). */
    private void conectarMenuAdmin(VistaConcreta vista, CuerpoFuncionesAdmin menu) {
        Runnable volverAlMenu = () -> vista.mostrarCuerpo(menu);

        menu.getBotonFlota().addActionListener(e -> {
            if (accesoAdmin(vista)) {
                CuerpoRegistroUnidad cuerpo = new CuerpoRegistroUnidad();
                PanelEstadoUnidad panel = new PanelEstadoUnidad();
                conectarFlota(cuerpo, panel);
                vista.mostrarCuerpo(conBotonVolver("Gestión de Flota", cuerpo, panel, volverAlMenu));
            }
        });
        menu.getBotonItinerario().addActionListener(e -> {
            if (accesoAdmin(vista)) {
                CuerpoRegistroRuta cuerpo = new CuerpoRegistroRuta();
                PanelGestionRutas panel = new PanelGestionRutas();
                conectarRutas(cuerpo, panel);
                vista.mostrarCuerpo(conBotonVolver("Control de Itinerarios", cuerpo, panel, volverAlMenu));
            }
        });
    }

    // ------------------------------------------------------ HU09: Gestión de Flota
    private void conectarFlota(CuerpoRegistroUnidad cuerpo, PanelEstadoUnidad panel) {
        Runnable refrescar = () -> {
            llenarTablaUnidades(cuerpo.getModeloTablaUnidades());
            panel.actualizarUnidades(placasDeUnidades());
        };
        refrescar.run();

        // HU09: registrar unidad
        cuerpo.getBotonConfirmar().addActionListener(e -> {
            Resultado<Void> r = servicioFlota.registrarUnidad(
                    cuerpo.getcampoPlaca().getText(),
                    cuerpo.getcampoModelo().getText(),
                    cuerpo.getcampoCapacidad().getText(),
                    EstadoUnidad.desdeTexto(cuerpo.getEstadoSeleccionado())); // "Ninguno" llega como null
            JOptionPane.showMessageDialog(cuerpo, r.getMensaje());
            if (r.isOk()) {
                cuerpo.getcampoPlaca().setText("");
                cuerpo.getcampoModelo().setText("");
                cuerpo.getcampoCapacidad().setText("");
                refrescar.run();
            }
        });

        // Gestión de flota: cambiar el estado operativo de una unidad registrada
        panel.getBotonCambiarEstado().addActionListener(e -> {
            Resultado<Void> r = servicioFlota.cambiarEstado(
                    panel.getUnidadElegida(), EstadoUnidad.desdeTexto(panel.getEstadoElegido()));
            JOptionPane.showMessageDialog(cuerpo, r.getMensaje());
            if (r.isOk()) {
                refrescar.run();
            }
        });
    }

    // ------------------------------------ HU10 y HU12: Control de Itinerarios
    private void conectarRutas(CuerpoRegistroRuta cuerpo, PanelGestionRutas panel) {
        Runnable refrescar = () -> {
            llenarTablaRutas(cuerpo.getModeloTablaRutas(), true);
            panel.actualizarRutas(nombresDeRutas());
            panel.actualizarUnidades(placasDeUnidades());
            panel.actualizarConductores(cedulasDeConductores());
        };
        refrescar.run();

        // HU10: crear ruta
        cuerpo.getBotonConfirmar().addActionListener(e -> {
            Resultado<Void> r = servicioItinerarios.crearRuta(
                    cuerpo.getcampoNombreRuta().getText(),
                    TipoRuta.desdeTexto(cuerpo.getTipoRutaSeleccionada()),
                    cuerpo.getcampoInicioJornada().getText(),
                    cuerpo.getcampoFinalJornada().getText());
            JOptionPane.showMessageDialog(cuerpo, r.getMensaje());
            if (r.isOk()) {
                limpiarFormularioRuta(cuerpo);
                refrescar.run();
            }
        });

        // HU10: redefinir la ruta elegida con los datos del formulario
        panel.getBotonRedefinir().addActionListener(e -> {
            Resultado<Void> r = servicioItinerarios.redefinirRuta(
                    panel.getRutaElegida(),
                    cuerpo.getcampoNombreRuta().getText(),
                    TipoRuta.desdeTexto(cuerpo.getTipoRutaSeleccionada()),
                    cuerpo.getcampoInicioJornada().getText(),
                    cuerpo.getcampoFinalJornada().getText());
            JOptionPane.showMessageDialog(cuerpo, r.getMensaje());
            if (r.isOk()) {
                limpiarFormularioRuta(cuerpo);
                refrescar.run();
            }
        });

        // HU12: asignar unidad a ruta (pide confirmación si la unidad ya está en otra ruta)
        panel.getBotonAsignar().addActionListener(e -> {
            String placa = panel.getUnidadElegida();
            String ruta = panel.getRutaElegida();
            boolean confirmado = false;
            if (placa != null && ruta != null && servicioItinerarios.requiereConfirmacion(placa, ruta)) {
                int opcion = JOptionPane.showConfirmDialog(cuerpo, Mensajes.CONFIRMAR_CAMBIO,
                        "Confirmar cambio", JOptionPane.YES_NO_OPTION);
                if (opcion != JOptionPane.YES_OPTION) {
                    return;
                }
                confirmado = true;
            }
            Resultado<Void> r = servicioItinerarios.asignarUnidad(placa, ruta, confirmado);
            JOptionPane.showMessageDialog(cuerpo, r.getMensaje());
            if (r.isOk()) {
                refrescar.run();
            }
        });

        // Asignación del conductor responsable de la unidad en la ruta seleccionada
        panel.getBotonAsignarConductor().addActionListener(e -> {
            String ruta = panel.getRutaElegida();
            String placa = panel.getUnidadElegida();
            String cedulaConductor = panel.getConductorElegido();

            Resultado<Void> r = servicioItinerarios.asignarConductor(
                    cedulaConductor,
                    placa,
                    ruta);

            JOptionPane.showMessageDialog(cuerpo, r.getMensaje());

            if (r.isOk()) {
                refrescar.run();
            }
        });
    }

    private void limpiarFormularioRuta(CuerpoRegistroRuta cuerpo) {
        cuerpo.getcampoNombreRuta().setText("");
        cuerpo.getcampoInicioJornada().setText("");
        cuerpo.getcampoFinalJornada().setText("");
    }

    private List<String> nombresDeRutas() {
        List<String> nombres = new ArrayList<>();
        for (Ruta ruta : servicioItinerarios.listarRutas()) {
            nombres.add(ruta.getNombre());
        }
        return nombres;
    }

    private List<String> placasDeUnidades() {
        List<String> placas = new ArrayList<>();
        for (UnidadTransporte unidad : servicioFlota.listarUnidades()) {
            placas.add(unidad.getPlaca());
        }
        return placas;
    }

    private List<String> cedulasDeConductores() {
        List<String> cedulas = new ArrayList<>();
        for (Usuario conductor : servicioItinerarios.listarConductores()) {
            cedulas.add(conductor.getCedula());
        }
        return cedulas;
    }

    // --------------------------------------------------------- Llenado de tablas
    private void llenarTablaUnidades(DefaultTableModel modelo) {
        modelo.setRowCount(0);
        for (UnidadTransporte u : servicioFlota.listarUnidades()) {
            modelo.addRow(new Object[] {u.getPlaca(), u.getModelo(), u.getCapacidad(), u.getEstado().getEtiqueta()});
        }
    }

    /** conColumnaModificar: la tabla del admin tiene una sexta columna "Modificar Ruta". */
    private void llenarTablaRutas(DefaultTableModel modelo, boolean conColumnaConductores) {
        modelo.setRowCount(0);
        for (Ruta ruta : servicioItinerarios.listarRutas()) {
            List<UnidadTransporte> asignadas = servicioItinerarios.unidadesDeRuta(ruta.getNombre());
            String textoUnidades;
            String textoConductores;

            if (asignadas.isEmpty()) {
                textoUnidades = "Sin unidades";
                textoConductores = "Sin conductores";
            } else {
                List<String> listaPlacas = new ArrayList<>();
                List<String> listaConductores = new ArrayList<>();
                for (UnidadTransporte unidad : asignadas) {
                    listaPlacas.add(unidad.getPlaca());
                    String cond = unidad.tieneConductor() ? unidad.getCedulaConductor() : "Sin asignar";
                    listaConductores.add(unidad.getPlaca() + ": " + cond);
                }
                textoUnidades = String.join(", ", listaPlacas) + " (Cupos: " + servicioItinerarios.cuposDeRuta(ruta.getNombre()) + ")";
                textoConductores = String.join(" | ", listaConductores);
            }

            Object[] fila = {
                ruta.getNombre(),
                ruta.getTipo().getEtiqueta(),
                ruta.getInicioJornada().format(HORA),
                ruta.getFinJornada().format(HORA),
                textoUnidades
            };

            if (conColumnaConductores) {
                fila = new Object[] {fila[0], fila[1], fila[2], fila[3], fila[4], textoConductores};
            }
            modelo.addRow(fila);
        }
    }

    private void llenarTablaConductor(DefaultTableModel modelo) {
        modelo.setRowCount(0);
        Usuario sesion = servicioUsuarios.getUsuarioSesion();
        if (sesion == null) {
            return;
        }
        for (UnidadTransporte u : servicioItinerarios.unidadesDeConductor(sesion.getCedula())) {
            String nombreRuta = u.estaAsignada() ? u.getNombreRuta() : "Sin ruta";
            String horario = "-";
            if (u.estaAsignada()) {
                for (Ruta r : servicioItinerarios.listarRutas()) {
                    if (r.getNombre().equalsIgnoreCase(nombreRuta)) {
                        horario = r.getInicioJornada().format(HORA) + " - " + r.getFinJornada().format(HORA);
                        break;
                    }
                }
            }
            modelo.addRow(new Object[] {
                u.getPlaca(),
                u.getModelo(),
                u.getCapacidad(),
                u.getEstado().getEtiqueta(),
                nombreRuta,
                horario
            });
        }
    }

    private void llenarTablaSalidasUsuario(DefaultTableModel modelo) {
        modelo.setRowCount(0);
        for (Ruta ruta : servicioItinerarios.listarRutas()) {
            for (UnidadTransporte u : servicioItinerarios.unidadesDeRuta(ruta.getNombre())) {
                String conductor = u.tieneConductor() ? u.getCedulaConductor() : "Por asignar";
                modelo.addRow(new Object[] {
                    u.getPlaca(),
                    conductor,
                    ruta.getInicioJornada().format(HORA),
                    ruta.getNombre()
                });
            }
        }
    }

    private boolean accesoAdmin(VistaConcreta vista) {
        Resultado<Void> acceso = servicioUsuarios.exigirRol(Rol.ADMIN_TRANSPORTE);
        if (!acceso.isOk()) {
            JOptionPane.showMessageDialog(vista, acceso.getMensaje());
        }
        return acceso.isOk();
    }

    /** Envuelve un cuerpo con una barra superior "Volver al menú" (y un panel inferior opcional) sin tocar el código de las vistas. */
    private JPanel conBotonVolver(String titulo, JPanel cuerpo, JPanel inferior, Runnable accionVolver) {
     JButton volver = new JButton("Volver al menú");
     volver.addActionListener(e -> accionVolver.run());

     JLabel etiquetaTitulo = new JLabel(titulo, SwingConstants.CENTER);
     etiquetaTitulo.setFont(new Font("Arial", Font.BOLD, 22));

     JPanel barra = new JPanel(new BorderLayout());
     barra.setBackground(Color.WHITE);
     barra.add(volver, BorderLayout.WEST);
     barra.add(etiquetaTitulo, BorderLayout.CENTER);
     // Panel vacío a la derecha del mismo ancho para que el título quede exactamente en el centro
     JPanel espaciador = new JPanel();
     espaciador.setOpaque(false);
     espaciador.setPreferredSize(volver.getPreferredSize());
     barra.add(espaciador, BorderLayout.EAST);

     JPanel contenedor = new JPanel(new BorderLayout());
     contenedor.setBackground(Color.WHITE);
     contenedor.add(barra, BorderLayout.NORTH);
     contenedor.add(cuerpo, BorderLayout.CENTER);
     if (inferior != null) {
         contenedor.add(inferior, BorderLayout.SOUTH);
     }
     return contenedor;
 }

    private void prepararVentana(VistaConcreta vista, boolean conSesion) {
        vista.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        vista.setLocationRelativeTo(null);
        vista.getEncabezadoGeneral().mostrarBotonCerrarSesion(conSesion);
    }

    /** Cierra las demás ventanas abiertas y muestra la nueva (una sola ventana activa a la vez). */
    private void mostrar(Window nueva) {
        for (Window ventana : Window.getWindows()) {
            if (ventana != nueva && ventana.isDisplayable()) {
                ventana.dispose();
            }
        }
        nueva.setVisible(true);
    }
}
