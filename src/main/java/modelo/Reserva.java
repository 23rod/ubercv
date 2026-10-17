package modelo;

import java.math.BigDecimal;
import java.time.LocalTime;
import modelo.Ticket;
import modelo.TipoRuta;


public class Reserva {
    private String nombreRuta;
    private String tipoRuta;
    private final LocalTime inicio;
    private final LocalTime fin;
    private String estadoUnidad;
    //--------------------------
    private String correoUsuario;
    private Ticket ticket;
    private String fechaReserva;
    private BigDecimal importeCobrado;



    public Reserva(String nombreRuta, String tipoRuta,
                String estadoUnidad, LocalTime inicio,
                LocalTime fin,Ticket ticket, String correoUsuario,
                BigDecimal importeCobrado){
        this.nombreRuta = nombreRuta;
        this.tipoRuta = tipoRuta;
        this.estadoUnidad = estadoUnidad;
        this.inicio = inicio;
        this.fin = fin;
        //Añadidos para validar duplicados, liberar cupos y reintegrar saldo
        this.ticket = ticket;
        this.fechaReserva = this.ticket.getFechaReserva();
        this.correoUsuario = correoUsuario;
        this.importeCobrado = importeCobrado;
    }

    //Getters 
    public String getNombreRuta(){
        return this.nombreRuta;
    }

    public String getTipoRuta(){
        return this.tipoRuta;
    }

    public String getEstadoUnidad(){
        return this.estadoUnidad;
    }

    public LocalTime getInicio(){
        return this.inicio;
    }

    public LocalTime getFin(){
        return this.fin;
    }

    public Ticket getTicketDeLaReserva(){
        return this.ticket;
    }

    public String getCorreoDeUsuario(){
        return this.correoUsuario;
    }

    public String getFechaDeReserva(){
        return this.ticket.getFechaReserva();
    }

}
