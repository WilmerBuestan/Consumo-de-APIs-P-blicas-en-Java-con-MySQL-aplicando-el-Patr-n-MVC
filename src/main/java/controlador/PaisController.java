package controlador;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import modelo.ApiException;
import modelo.Conexion;
import modelo.ConsumoAPI;
import modelo.Pais;
import modelo.PaisDAO;
import modelo.Regiones;

/**
 * Controlador: coordina Vista y Modelo. No dibuja nada y no escribe SQL.
 *  - pide el JSON a ConsumoAPI,
 *  - lo convierte a objetos Pais (Gson),
 *  - valida y delega la persistencia en PaisDAO.
 */
public class PaisController {

    /** Para que la vista pueda animar el avance. fase: 1 API, 2 JSON, 3 objetos Java, 4 base de datos. */
    public interface Progreso {
        void avance(int fase, String mensaje, int porcentaje);
    }

    public record ResultadoImportacion(int total, int insertados, int actualizados,
                                       boolean desdeApi, String aviso) {
    }

    public record Estadisticas(int totalPaises, long poblacionTotal,
                               Map<String, Integer> porRegion, Pais masPoblado,
                               String regionTop, int regionTopCantidad) {
    }

    private final PaisDAO dao;
    private final ConsumoAPI api;

    public PaisController() {
        this(new PaisDAO(), new ConsumoAPI());
    }

    public PaisController(PaisDAO dao, ConsumoAPI api) {
        this.dao = dao;
        this.api = api;
    }

    // ------------------------------------------------------------ arranque
    public void inicializarBD() throws SQLException {
        Conexion.inicializar();
    }

    // ------------------------------------------------------------ importación (caso principal)
    public ResultadoImportacion importarPaises(Progreso progreso) throws SQLException, ApiException {
        List<String> paginas = new ArrayList<>();
        try {
            progreso.avance(1, "Conectando con REST Countries…", 2);
            int offset = 0;
            boolean hayMas = true;
            int descargados = 0;
            while (hayMas) {
                String json = api.obtenerPagina(offset);          // 1-2. consume la API y obtiene JSON
                paginas.add(json);
                JsonObject data = JsonParser.parseString(json).getAsJsonObject().getAsJsonObject("data");
                JsonObject meta = data.getAsJsonObject("meta");
                int enPagina = data.getAsJsonArray("objects").size();
                int total = meta.has("total") ? meta.get("total").getAsInt() : 0;
                int limite = meta.has("limit") ? meta.get("limit").getAsInt() : ConsumoAPI.LIMITE;
                descargados += enPagina;
                hayMas = enPagina > 0 && meta.has("more") && meta.get("more").getAsBoolean();
                offset += limite;
                int pct = 5 + (total > 0 ? Math.min(40, descargados * 40 / total) : 20);
                progreso.avance(2, "Descargando JSON… " + descargados + (total > 0 ? " / " + total : "") + " países", pct);
            }
        } catch (ApiException e) {
            // Respaldo local: si la API falla pero ya hay datos guardados, la app sigue funcionando.
            int locales = dao.contar();
            if (locales > 0) {
                return new ResultadoImportacion(locales, 0, 0, false,
                        e.getMessage() + " Se usan los " + locales + " países ya guardados en la base de datos.");
            }
            throw e;
        }

        progreso.avance(3, "Convirtiendo JSON a objetos Java…", 50);   // 3. JSON -> objetos
        Map<String, Pais> unicos = new LinkedHashMap<>();
        for (String json : paginas) {
            JsonArray objetos = JsonParser.parseString(json).getAsJsonObject()
                    .getAsJsonObject("data").getAsJsonArray("objects");
            for (Pais p : convertir(objetos)) {
                unicos.put(p.getNombre().toLowerCase(), p);
            }
        }
        List<Pais> lista = new ArrayList<>(unicos.values());

        progreso.avance(4, "Guardando en la base de datos…", 55);       // 4. guarda en MySQL
        int[] r = dao.importar(lista, (hechos, total) ->
                progreso.avance(4, "Guardando en la base de datos… " + hechos + " / " + total,
                        55 + hechos * 45 / Math.max(total, 1)));
        return new ResultadoImportacion(lista.size(), r[0], r[1], true, null);
    }

    // ------------------------------------------------------------ Actividad 1: consulta por nombre
    public List<Pais> consultarPorNombre(String nombre) throws ApiException {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Escribe el nombre de un país.");
        }
        try {
            String json = api.buscarPorNombre(nombre);
            JsonArray objetos = JsonParser.parseString(json).getAsJsonObject()
                    .getAsJsonObject("data").getAsJsonArray("objects");
            return convertir(objetos);
        } catch (ApiException e) {
            if (e.getEstado() == 404) {
                return new ArrayList<>();           // sin coincidencias
            }
            throw e;
        }
    }

    /** Guarda (o actualiza si ya existe) los países que llegaron de la API. @return {insertados, actualizados} */
    public int[] guardarDesdeApi(List<Pais> paises) throws SQLException {
        return dao.importar(paises, null);
    }

    // ------------------------------------------------------------ Actividad 2: CRUD
    public void crear(Pais pais) throws SQLException {
        validar(pais);
        if (dao.existeNombre(pais.getNombre(), 0)) {
            throw new IllegalArgumentException("Ya existe un país llamado \"" + pais.getNombre() + "\".");
        }
        dao.guardar(pais);
    }

    public void actualizar(Pais pais) throws SQLException {
        validar(pais);
        if (dao.existeNombre(pais.getNombre(), pais.getId())) {
            throw new IllegalArgumentException("Ya existe otro país llamado \"" + pais.getNombre() + "\".");
        }
        dao.actualizar(pais);
    }

    public void eliminar(int id) throws SQLException {
        dao.eliminar(id);
    }

    // ------------------------------------------------------------ Actividades 3 y 4: lista y filtros
    public List<Pais> listar(String texto, String region) throws SQLException {
        return dao.filtrar(texto, region);
    }

    public int contarPaises() throws SQLException {
        return dao.contar();
    }

    // ------------------------------------------------------------ Actividad 5: reportes
    public Estadisticas estadisticas() throws SQLException {
        Map<String, Integer> porRegion = dao.contarPorRegion();
        String regionTop = "—";
        int cantidad = 0;
        for (Map.Entry<String, Integer> e : porRegion.entrySet()) {      // viene ordenado de mayor a menor
            regionTop = e.getKey();
            cantidad = e.getValue();
            break;
        }
        return new Estadisticas(dao.contar(), dao.poblacionTotal(), porRegion,
                dao.paisMasPoblado(), regionTop, cantidad);
    }

    // ------------------------------------------------------------ utilidades
    private void validar(Pais p) {
        if (p.getNombre() == null || p.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (p.getRegion() == null || p.getRegion().isBlank()) {
            throw new IllegalArgumentException("Selecciona una región.");
        }
        if (p.getPoblacion() < 0) {
            throw new IllegalArgumentException("La población no puede ser negativa.");
        }
        p.setNombre(recortar(p.getNombre(), 100));
        p.setCapital(recortar(p.getCapital(), 100));
        p.setRegion(recortar(p.getRegion(), 100));
        p.setMoneda(recortar(p.getMoneda(), 100));
    }

    private static String recortar(String s, int max) {
        if (s == null) {
            return "";
        }
        s = s.trim();
        return s.length() > max ? s.substring(0, max) : s;
    }

    /** JSON de la API (v5: names.common, capitals[].name, currencies[]) -> objetos Pais. */
    List<Pais> convertir(JsonArray objetos) {
        List<Pais> lista = new ArrayList<>();
        for (JsonElement e : objetos) {
            JsonObject obj = e.getAsJsonObject();

            String nombre = textoAnidado(obj, "names", "common");
            if (nombre.isEmpty()) {
                nombre = textoAnidado(obj, "name", "common");        // formato antiguo v3.1
            }
            if (nombre.isEmpty()) {
                continue;
            }

            String capital = "";
            if (obj.has("capitals") && obj.get("capitals").isJsonArray()
                    && obj.getAsJsonArray("capitals").size() > 0) {
                JsonElement primera = obj.getAsJsonArray("capitals").get(0);
                capital = primera.isJsonObject() ? texto(primera.getAsJsonObject(), "name") : primera.getAsString();
            } else if (obj.has("capital") && obj.get("capital").isJsonArray()
                    && obj.getAsJsonArray("capital").size() > 0) {
                capital = obj.getAsJsonArray("capital").get(0).getAsString();   // formato antiguo
            }

            String region = Regiones.aEspanol(texto(obj, "region"));
            long poblacion = obj.has("population") && !obj.get("population").isJsonNull()
                    ? obj.get("population").getAsLong() : 0L;

            Pais pais = new Pais(nombre, recortar(capital, 100), region, poblacion,
                    recortar(monedas(obj), 100));
            lista.add(pais);
        }
        return lista;
    }

    private static String monedas(JsonObject obj) {
        if (!obj.has("currencies") || obj.get("currencies").isJsonNull()) {
            return "";
        }
        List<String> partes = new ArrayList<>();
        JsonElement cur = obj.get("currencies");
        if (cur.isJsonArray()) {                                      // v5: [{code,name,symbol}]
            for (JsonElement m : cur.getAsJsonArray()) {
                JsonObject o = m.getAsJsonObject();
                partes.add(formatoMoneda(texto(o, "name"), texto(o, "code")));
            }
        } else if (cur.isJsonObject()) {                              // v3.1: {"USD":{"name":..}}
            for (Map.Entry<String, JsonElement> m : cur.getAsJsonObject().entrySet()) {
                String nombre = m.getValue().isJsonObject() ? texto(m.getValue().getAsJsonObject(), "name") : "";
                partes.add(formatoMoneda(nombre, m.getKey()));
            }
        }
        return String.join(", ", partes);
    }

    private static String formatoMoneda(String nombre, String codigo) {
        if (nombre.isEmpty()) {
            return codigo;
        }
        return codigo.isEmpty() ? nombre : nombre + " (" + codigo + ")";
    }

    private static String texto(JsonObject obj, String clave) {
        return obj.has(clave) && !obj.get(clave).isJsonNull() && obj.get(clave).isJsonPrimitive()
                ? obj.get(clave).getAsString() : "";
    }

    private static String textoAnidado(JsonObject obj, String padre, String clave) {
        return obj.has(padre) && obj.get(padre).isJsonObject()
                ? texto(obj.getAsJsonObject(padre), clave) : "";
    }
}
