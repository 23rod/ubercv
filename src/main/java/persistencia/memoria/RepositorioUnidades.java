package persistencia.memoria;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import modelo.UnidadTransporte;

/** "Base de datos" de unidades en memoria: un mapa placa -> unidad (la placa se guarda en mayúsculas). */
public class RepositorioUnidades {

    private final Map<String, UnidadTransporte> porPlaca = new LinkedHashMap<>();

    public void guardar(UnidadTransporte unidad) {
        porPlaca.put(unidad.getPlaca(), unidad);
    }

    public UnidadTransporte buscarPorPlaca(String placa) {
        return (placa == null) ? null : porPlaca.get(placa.trim().toUpperCase());
    }

    public boolean existePlaca(String placa) {
        return buscarPorPlaca(placa) != null;
    }

    public List<UnidadTransporte> listar() {
        return new ArrayList<>(porPlaca.values());
    }
}
