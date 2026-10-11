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

public class RepositorioUnidades {
    private final Path archivo; // null = solo memoria (lo usan las pruebas)
    private final Map<String, UnidadTransporte> porPlaca = new LinkedHashMap<>();

    /** Repositorio solo en memoria: no lee ni escribe archivos. */
    public RepositorioUnidades() {
        this(null);
    }

    /** Repositorio que guarda en el archivo indicado. Si el archivo no existe, empieza vacío. */
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
                //System.out.println("El archivo de unidades está vacío.");
                return;
            }

            for (String linea : lineas) {
                linea = linea.trim();
                // Ignorar líneas vacías o cabeceras
                if (linea.isEmpty() || linea.startsWith("placa,")) {
                    continue;
                }

                String[] partes = linea.split(",");
                if (partes.length < 4) {
                    continue; // Línea incompleta, ignorar para evitar ArrayIndexOutOfBoundsException
                }

                String placa = partes[0].trim().toUpperCase();
                String modelo = partes[1].trim();
                int capacidad = Integer.parseInt(partes[2].trim());
                // El archivo guarda el nombre (EN_MANTENIMIENTO); si no coincide, se prueba con la etiqueta
                EstadoUnidad estado = AlmacenArchivo.enumONulo(EstadoUnidad.class, partes[3]);
                if (estado == null) {
                    estado = EstadoUnidad.desdeTexto(partes[3]);
                }
                if (estado == null) {
                    System.err.println("Advertencia: Estado no válido en la línea: " + linea);
                    continue; // No cargar una unidad con estado corrupto/nulo
                }

                UnidadTransporte unidad = new UnidadTransporte(placa, modelo, capacidad, estado);

                // Asignar ruta y conductor si están presentes en la fila
                if (partes.length >= 5 && !partes[4].trim().isEmpty()) {
                    unidad.setNombreRuta(partes[4].trim());
                }
                if (partes.length >= 6 && !partes[5].trim().isEmpty()) {
                    unidad.setCedulaConductor(partes[5].trim());
                }

                // IMPORTANTE: Se guarda directo al mapa, NUNCA llamar a registrar/escribir aquí.
                // Como porPlaca es un Map, sobreescribe automáticamente cualquier placa duplicada anterior.
                porPlaca.put(placa, unidad);
            }

        } catch (Exception e) {
            System.err.println("Error al cargar unidades desde el archivo: " + e.getMessage());
        }
    }

    /**
     * Sobrescribe completamente el archivo con las unidades no repetidas almacenadas en el mapa.
     */
    private void reescribirArchivo() {
        if (archivo == null) {
            return;
        }
        try {
            List<String> lineasAEscribir = new ArrayList<>();            

            for (UnidadTransporte unidad : porPlaca.values()) {
            // Protección contra null en el estado:
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

    public void guardar(UnidadTransporte unidad) {
        if (unidad == null || unidad.getPlaca() == null) return;
        
        // Guardar o actualizar en el mapa
        porPlaca.put(unidad.getPlaca().trim().toUpperCase(), unidad);
        
        // Reescribir todo el archivo actualizado y sin duplicados
        reescribirArchivo();
    }

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

        // Actualizamos en memoria y reescribimos el archivo completo
        porPlaca.put(unidad.getPlaca().trim().toUpperCase(), unidad);
        reescribirArchivo();
    }

    public void actualizar(UnidadTransporte unidad) {
        guardar(unidad); // Al llamar a guardar(), actualiza el Map y reescribe el archivo txt
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