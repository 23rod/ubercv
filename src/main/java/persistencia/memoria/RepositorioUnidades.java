package persistencia.memoria;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import modelo.UnidadTransporte;
import modelo.EstadoUnidad;
import persistencia.interfaces.IRepositorioUnidades; // Importante

public class RepositorioUnidades implements IRepositorioUnidades {
    private final Path archivo;
    private final Map<String, UnidadTransporte> porPlaca = new LinkedHashMap<>();

    public RepositorioUnidades() {
        this(null);
    }

    public RepositorioUnidades(Path archivo) {
        this.archivo = archivo;
        cargarUnidadesDesdeArchivo();
    }

    private void cargarUnidadesDesdeArchivo() {
        if (archivo == null || !Files.exists(archivo)) {
            return;
        }

        try {
            List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
            if (lineas.isEmpty()) {
                return;
            }

            for (String linea : lineas) {
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("placa,")) {
                    continue;
                }

                String[] partes = linea.split(",");
                if (partes.length < 4) {
                    continue;
                }

                String placa = partes[0].trim().toUpperCase();
                String modelo = partes[1].trim();
                int capacidad = Integer.parseInt(partes[2].trim());
                EstadoUnidad estado = AlmacenArchivo.enumONulo(EstadoUnidad.class, partes[3]);
                if (estado == null) {
                    estado = EstadoUnidad.desdeTexto(partes[3]);
                }
                if (estado == null) {
                    System.err.println("Advertencia: Estado no válido en la línea: " + linea);
                    continue;
                }

                UnidadTransporte unidad = new UnidadTransporte(placa, modelo, capacidad, estado);

                if (partes.length >= 5 && !partes[4].trim().isEmpty()) {
                    unidad.setNombreRuta(partes[4].trim());
                }
                if (partes.length >= 6 && !partes[5].trim().isEmpty()) {
                    unidad.setCedulaConductor(partes[5].trim());
                }

                porPlaca.put(placa, unidad);
            }

        } catch (Exception e) {
            System.err.println("Error al cargar unidades desde el archivo: " + e.getMessage());
        }
    }

    private void reescribirArchivo() {
        if (archivo == null) {
            return;
        }
        try {
            List<String> lineasAEscribir = new ArrayList<>();            

            for (UnidadTransporte unidad : porPlaca.values()) {
                String estadoStr = (unidad.getEstado() != null) ? unidad.getEstado().toString() : "Ninguno";

                String linea = String.format("%s,%s,%d,%s,%s,%s",
                        unidad.getPlaca(),
                        unidad.getModelo(),
                        unidad.getCapacidad(),
                        estadoStr,
                        (unidad.getNombreRuta() != null) ? unidad.getNombreRuta() : "",
                        (unidad.getCedulaConductor() != null) ? unidad.getCedulaConductor() : ""
                );
                lineasAEscribir.add(linea);
            }

            AlmacenArchivo.escribirAtomico(archivo, lineasAEscribir);

        } catch (IOException e) {
            System.err.println("Error al reescribir el archivo de unidades: " + e.getMessage());
        }
    }

    @Override
    public void guardar(UnidadTransporte unidad) {
        if (unidad == null || unidad.getPlaca() == null) return;
        porPlaca.put(unidad.getPlaca().trim().toUpperCase(), unidad);
        reescribirArchivo();
    }

    @Override
    public void modificar(UnidadTransporte unidad, EstadoUnidad nuevoEstado) {
        if (unidad == null || !existePlaca(unidad.getPlaca())) {
            System.err.println("La unidad no existe en el repositorio.");
            return;
        }

        unidad.setEstado(nuevoEstado);
        if (nuevoEstado != EstadoUnidad.ACTIVO) {
            unidad.setNombreRuta(null);
            unidad.setCedulaConductor(null);
        }

        porPlaca.put(unidad.getPlaca().trim().toUpperCase(), unidad);
        reescribirArchivo();
    }

    @Override
    public void actualizar(UnidadTransporte unidad) {
        guardar(unidad);
    }

    @Override
    public UnidadTransporte buscarPorPlaca(String placa) {
        return (placa == null) ? null : porPlaca.get(placa.trim().toUpperCase());
    }

    @Override
    public boolean existePlaca(String placa) {
        return buscarPorPlaca(placa) != null;
    }

    @Override
    public List<UnidadTransporte> listar() {
        return new ArrayList<>(porPlaca.values());
    }
}