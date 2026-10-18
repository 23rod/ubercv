package persistencia.interfaces;

import java.time.LocalDate;
import java.util.List;
import modelo.Reserva;

/** Contrato de almacenamiento de reservas y consulta de disponibilidad. */
public interface IRepositorioReservas {
    void guardar(Reserva reserva);
    void actualizar(Reserva reserva);
    Reserva buscarPorCodigo(String codigoTicket);
    boolean existeCodigoTicket(String codigoTicket);
    List<Reserva> listar();
    List<Reserva> listarPorUsuario(String correo);
    List<Reserva> listarActivasPorUsuarioYFecha(String correo, LocalDate fechaServicio);
    List<Reserva> listarActivasPorRutaYFecha(String nombreRuta, LocalDate fechaServicio);
    List<Reserva> listarActivasPorUnidadYFecha(String placa, LocalDate fechaServicio);
}
