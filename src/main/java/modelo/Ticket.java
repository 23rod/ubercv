package modelo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.security.SecureRandom; 

public class Ticket {
    private final String codigo;
    private final String fechaReserva;
    private String nombreRuta;

    public Ticket(String nombreRuta){
        this.nombreRuta = nombreRuta;
        
        //Fecha actual       
        LocalDate fechaHoy = LocalDate.now();     
        // Darle un formato personalizado
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaFormateada = fechaHoy.format(formato);
        this.fechaReserva = fechaFormateada;

        //Codigo
        this.codigo = generarStringAleatorio(8);   


    }

    

    //Generar un codigo aleatorio 
    private static final String CARACTERES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static String generarStringAleatorio(int longitud) {
        StringBuilder sb = new StringBuilder(longitud);
        for (int i = 0; i < longitud; i++) {
            int indice = RANDOM.nextInt(CARACTERES.length());
            sb.append(CARACTERES.charAt(indice));
        }
        return sb.toString();
    }

    //Getters 
    public String getCodigo(){
        return this.codigo;
    }

    public String getFechaReserva(){
        return this.fechaReserva;
    }

    public String getNombreRuta(){
        return this.nombreRuta;
    }


}
