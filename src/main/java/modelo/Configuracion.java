package modelo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Lee archivos de configuración desde la carpeta config/ (junto al proyecto).
 * Así la API key y la clave de la base de datos NO quedan escritas en el código ni en GitHub.
 */
public final class Configuracion {

    private Configuracion() {
    }

    public static Properties cargar(String nombreArchivo) {
        Properties props = new Properties();
        Path ruta = Paths.get("config", nombreArchivo);
        if (Files.exists(ruta)) {
            try (InputStream in = Files.newInputStream(ruta);
                 InputStreamReader lector = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                props.load(lector);
            } catch (IOException e) {
                System.err.println("No se pudo leer " + ruta + ": " + e.getMessage());
            }
        }
        return props;
    }
}
