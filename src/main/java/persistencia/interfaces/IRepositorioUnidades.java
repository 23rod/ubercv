package persistencia.interfaces;

import java.util.List;
import modelo.UnidadTransporte;

/** Contrato de almacenamiento de unidades. Solo guarda y consulta: las reglas de negocio viven en los servicios. */
public interface IRepositorioUnidades {

    /** Guarda la unidad; si ya existe una con esa placa, la reemplaza. */
    void guardar(UnidadTransporte unidad);

    /** Persiste los cambios hechos a una unidad ya guardada. */
    void actualizar(UnidadTransporte unidad);

    UnidadTransporte buscarPorPlaca(String placa);

    boolean existePlaca(String placa);

    List<UnidadTransporte> listar();

    /** Unidades asignadas a la ruta indicada (el nombre no distingue mayúsculas). */
    List<UnidadTransporte> listarPorRuta(String nombreRuta);

    /** Unidades que tienen asignado al conductor con esa cédula. */
    List<UnidadTransporte> listarPorConductor(String cedulaConductor);
}
