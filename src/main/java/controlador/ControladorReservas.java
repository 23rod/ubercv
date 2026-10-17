package controlador;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import modelo.UnidadTransporte;
import modelo.Usuario;
import persistencia.interfaces.IRepositorioUnidades;
import persistencia.interfaces.IRepositorioUsuarios;
import servicio.ServicioFlota;
import servicio.ServicioMonedero;
import servicio.ServicioUsuarios;
import javax.swing.JOptionPane;
import vista.PlantillaVista.CuerposDelSistema.CuerpoFuncionesUsuario;
import modelo.Ruta;
import modelo.EstadoUnidad;
import modelo.MensajesMonedero;
import modelo.Ticket;
import modelo.TipoRuta;
import modelo.Reserva;
import modelo.Rol;

public class ControladorReservas {    
    //Vista asignada a este controlador
    private CuerpoFuncionesUsuario cuerpoFuncionesUsuario;
    //--
    private ServicioFlota servicioFlota;
    private ServicioUsuarios servicioUsuarios;
    private CuerpoFuncionesUsuario vistaActual;
    private IRepositorioUnidades flota;
    private IRepositorioUsuarios usuarioIniciado;
    private ServicioMonedero servicioMonedero;

    public ControladorReservas(IRepositorioUnidades flota,
    IRepositorioUsuarios usuarioIniciado, CuerpoFuncionesUsuario vistaActual,
    ServicioFlota servicioFlota, ServicioUsuarios servicioUsuarios,
    CuerpoFuncionesUsuario cuerpoFuncionesUsuario ){
        this.flota = flota;
        this.usuarioIniciado = usuarioIniciado;
        this.vistaActual = vistaActual;
        this.servicioFlota = servicioFlota;
        this.servicioUsuarios = servicioUsuarios;
        this.cuerpoFuncionesUsuario = cuerpoFuncionesUsuario;
    }

    //Listar las placas de las unidades para llenar el listado de placas
    //Se utiliza para cargar las unidades con cupos disponibles.
    private String[] unidadesConCupo(){
        List<UnidadTransporte> unidades= flota.listar();
        List<String> unidadesConCupo = new ArrayList<>();
        
        for(int i=0; i<unidades.size(); i++){
            if(unidades.get(i).getCupos() > 0){
                unidadesConCupo.add(unidades.get(i).getPlaca()); 
            }
        }

        String[] arreglo=  unidadesConCupo.toArray(new String[unidadesConCupo.size()]);
        return arreglo;
    }

    //Lógica completa para reservar un cupo:
    //1 Activarse una vez se hace click en "Confirmar Reserva"
    //2 Revisar si el usuario tiene suficiente saldo
    //3 Modificar los cupos de la unidad  
    //4 Crear y guardar la reserva dada y exponerla al usuario: Nombre de Ruta, Tipo de Ruta, Inicio, Final, Placa, Estado de la Unidad
    //6 Generar el ticket con: código, fecha de reserva y ruta.
    //

    private void reservarCupo(Ruta ruta, String placaUnidad){
        
        if(!esfondoSuficiente(ruta)){
            JOptionPane.showMessageDialog(null, MensajesMonedero.SALDO_INSUFICIENTE);
            return;
        }
        //Buscamos la placa de la unidad y restamos un cupo
        esfondoSuficiente(ruta);
        restarCupo(placaUnidad);
    }

    //2 Lógica de pago: Tipo de Ruta y Tipo de Usuario
    // Solo se cobra si es una ruta extraurbana
    private boolean esfondoSuficiente(Ruta ruta){
        BigDecimal saldoUsuario = servicioMonedero.saldo(servicioUsuarios.getUsuarioSesion().getCorreo());
        Rol rolUsuario = servicioUsuarios.getUsuarioSesion().getRol();

        //Cobrar de una vez
        if(ruta.getTipo() == TipoRuta.desdeTexto("Extraurbana")){
            if(rolUsuario == Rol.desdeTexto("estudiante")){
                //Se debita del 10% al 20% del precio: 15%
            }
            else if(rolUsuario == Rol.desdeTexto("empleado")){
                //Se debita del 30% al 60% del precio: 45%
            }
            else if(rolUsuario == Rol.desdeTexto("publico general")){
                //Se debita el 100% del precio
            }
        }
        else{//Si es urbana 
            //Debitar con monto 0 servicioMonedero.debitar(correo,0,descripcion)
        }
        

        return true;
    }


        //3 Metodo a realizarse una vez se reserva un cupo
    private void restarCupo(String placaUnidad){
        UnidadTransporte aCambiar = flota.buscarPorPlaca(placaUnidad); 
        aCambiar.setCupos(aCambiar.getCupos()-1);
        //Reducir en uno los cupos disponibles de una unidad y actualizar en la persistencia vólatil y no volátil
        flota.actualizar(aCambiar);
    }

    //4 Crear y guardarla reserva : la tabla de arriba del todo de cuerpoFunciones usuarios
    //                     tiene el nombre de la ruta y el tipo. Hay que ver como tomar la fila y
    //                      usar esos datos para crear LA RESERVA Y EL TICKET
    //                      El importeCobrado vendrá de la lógica de debitar del servicio de monedero o una reutilizacion de esa logica
    //                      en esta clase.
    private Reserva crearReserva(Ruta ruta,EstadoUnidad estadoUnidad, BigDecimal importeCobrado){
        Reserva nuevaReserva = new Reserva(
                                        ruta.getNombre() ,
                                        ruta.getTipo().toString() ,
                                        estadoUnidad.toString() ,
                                        ruta.getInicioJornada(),ruta.getFinJornada() ,
                                        this.generarTicket(ruta.getNombre()) ,
                                        this.servicioUsuarios.getUsuarioSesion().getCorreo() ,
                                        importeCobrado);

    }

    //5 Guardar la reserva
    


    //6 Generar y guardar el ticket
    private Ticket generarTicket(String nombreRuta){
        return new Ticket(nombreRuta);
    }

    

    //==============================================HU7b==================================================================//
    //LOGICA DE PERMANENCIA DE LA RESERVA ACTUAL
    //UNA VEZ LA HORA ACTUAL SEA IGUAL A LA HORA DEL FINAL DE LA RUTA SE ELIMINA LA RESERVA ACTUAL====================//
    private boolean esFinalDeRuta(Ruta ruta, LocalTime horaActual){
        return ruta.getFinJornada() == horaActual;
    }

    private void eliminarReserva(){
        
    }

    



}
