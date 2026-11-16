package modelo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Consumo de la API REST Countries (versión 5, con API key).
 *
 *   Todos los países : GET {base}?limit=100&offset=0
 *   Por nombre       : GET {base}/names.common?q=ecuador
 *
 * La key viaja en la cabecera  Authorization: Bearer <key>.
 * Esta clase solo devuelve el JSON como texto; convertirlo a objetos Pais es trabajo del controlador.
 */
public class ConsumoAPI {

    public static final int LIMITE = 100;                       // máximo del plan gratuito
    private static final String BASE_POR_DEFECTO = "https://api.restcountries.com/countries/v5";
    private static final String CAMPOS = "names.common,capitals,region,population,currencies";

    private final Properties cfg = Configuracion.cargar("api.properties");

    /** Una página de países (de 100 en 100). */
    public String obtenerPagina(int offset) throws ApiException {
        return pedir("", "limit=" + LIMITE + "&offset=" + offset);
    }

    /** Búsqueda por nombre (contiene el texto, sin distinguir mayúsculas). */
    public String buscarPorNombre(String texto) throws ApiException {
        String q = URLEncoder.encode(texto.trim(), StandardCharsets.UTF_8);
        return pedir("/names.common", "q=" + q + "&limit=" + LIMITE);
    }

    /** Intenta pedir solo los campos necesarios; si la API lo rechaza (400) reintenta sin filtro. */
    private String pedir(String ruta, String consulta) throws ApiException {
        try {
            return get(ruta, consulta + "&response_fields=" + CAMPOS);
        } catch (ApiException e) {
            if (e.getEstado() == 400) {
                return get(ruta, consulta);
            }
            throw e;
        }
    }

    private String get(String ruta, String consulta) throws ApiException {
        String clave = obtenerClave();
        String base = cfg.getProperty("api.base", BASE_POR_DEFECTO);
        try {
            URI uri = URI.create(base + ruta + "?" + consulta);
            HttpURLConnection con = (HttpURLConnection) uri.toURL().openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(10_000);
            con.setReadTimeout(25_000);
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Authorization", "Bearer " + clave);
            con.setRequestProperty("User-Agent", "TallerMVC-Paises/1.0");

            int codigo = con.getResponseCode();
            boolean ok = codigo >= 200 && codigo < 300;
            String cuerpo = leer(ok ? con.getInputStream() : con.getErrorStream());
            con.disconnect();

            if (ok) {
                return cuerpo;
            }
            throw new ApiException(codigo, mensajeAmigable(codigo));
        } catch (IOException e) {
            throw new ApiException(0, "No se pudo conectar con la API (revisa tu internet).", e);
        }
    }

    private String obtenerClave() throws ApiException {
        String clave = System.getenv("RESTCOUNTRIES_API_KEY");
        if (clave == null || clave.isBlank()) {
            clave = cfg.getProperty("api.key", "");
        }
        if (clave.isBlank() || clave.startsWith("PON_AQUI")) {
            throw new ApiException(401, "Falta la API key. Colócala en config/api.properties.");
        }
        return clave.trim();
    }

    private static String mensajeAmigable(int codigo) {
        switch (codigo) {
            case 400:
                return "La API rechazó la solicitud (400).";
            case 401:
                return "API key inválida o vencida (401). Revisa config/api.properties.";
            case 403:
                return "Cuenta congelada o cuota mensual agotada (403).";
            case 404:
                return "No se encontró el recurso (404).";
            case 429:
                return "Demasiadas solicitudes seguidas (429). Espera unos segundos.";
            default:
                return "La API respondió con el código HTTP " + codigo + ".";
        }
    }

    private static String leer(InputStream in) throws IOException {
        if (in == null) {
            return "";
        }
        StringBuilder resultado = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                resultado.append(linea);
            }
        }
        return resultado.toString();
    }
}
