package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * DAO (Data Access Object): único lugar de la aplicación que habla SQL.
 * El controlador y la vista nunca ven una consulta.
 */
public class PaisDAO {

    private static final String COLUMNAS = "id, nombre, capital, region, poblacion, moneda";

    private Pais mapear(ResultSet rs) throws SQLException {
        Pais p = new Pais();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("nombre"));
        p.setCapital(rs.getString("capital"));
        p.setRegion(rs.getString("region"));
        p.setPoblacion(rs.getLong("poblacion"));
        p.setMoneda(rs.getString("moneda"));
        return p;
    }

    // ---------------------------------------------------------------- CREATE
    public boolean guardar(Pais pais) throws SQLException {
        String sql = "INSERT INTO pais(nombre,capital,region,poblacion,moneda)"
                + " VALUES(?,?,?,?,?)";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, pais.getNombre());
            ps.setString(2, pais.getCapital());
            ps.setString(3, pais.getRegion());
            ps.setLong(4, pais.getPoblacion());
            ps.setString(5, pais.getMoneda());
            boolean ok = ps.executeUpdate() > 0;
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    pais.setId(claves.getInt(1));
                }
            }
            return ok;
        }
    }

    // ---------------------------------------------------------------- READ
    public List<Pais> listar() throws SQLException {
        return filtrar("", null);
    }

    public Pais buscarPorId(int id) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM pais WHERE id = ?";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /** Búsqueda local por nombre (contiene el texto). */
    public List<Pais> buscarPorNombre(String texto) throws SQLException {
        return filtrar(texto, null);
    }

    /** Países de una región (América, Europa, Asia, África, Oceanía). */
    public List<Pais> listarPorRegion(String region) throws SQLException {
        return filtrar("", region);
    }

    /** Filtro combinado: texto en el nombre y/o región (null o vacío = sin filtro). */
    public List<Pais> filtrar(String texto, String region) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT " + COLUMNAS + " FROM pais WHERE 1=1");
        List<String> parametros = new ArrayList<>();
        if (texto != null && !texto.isBlank()) {
            sql.append(" AND nombre LIKE ?");
            parametros.add("%" + texto.trim() + "%");
        }
        if (region != null && !region.isBlank()) {
            sql.append(" AND region = ?");
            parametros.add(region);
        }
        sql.append(" ORDER BY nombre");

        List<Pais> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setString(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** ¿Existe otro país con ese nombre? (excluirId = id que se está editando, 0 si es nuevo) */
    public boolean existeNombre(String nombre, int excluirId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pais WHERE nombre = ? AND id <> ?";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setInt(2, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // ---------------------------------------------------------------- UPDATE
    public boolean actualizar(Pais pais) throws SQLException {
        String sql = "UPDATE pais SET nombre=?, capital=?, region=?, poblacion=?, moneda=? WHERE id=?";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pais.getNombre());
            ps.setString(2, pais.getCapital());
            ps.setString(3, pais.getRegion());
            ps.setLong(4, pais.getPoblacion());
            ps.setString(5, pais.getMoneda());
            ps.setInt(6, pais.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // ---------------------------------------------------------------- DELETE
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM pais WHERE id = ?";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ---------------------------------------------------------------- IMPORTACIÓN MASIVA
    /**
     * Guarda una lista completa en una sola transacción. Si el país ya existe (mismo nombre)
     * lo actualiza en lugar de duplicarlo, así se puede importar varias veces sin repetidos.
     *
     * @return {insertados, actualizados}
     */
    public int[] importar(List<Pais> paises, BiConsumer<Integer, Integer> progreso) throws SQLException {
        int insertados = 0;
        int actualizados = 0;
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try {
                Map<String, Integer> existentes = new HashMap<>();
                try (Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery("SELECT id, nombre FROM pais")) {
                    while (rs.next()) {
                        existentes.put(rs.getString("nombre").toLowerCase(), rs.getInt("id"));
                    }
                }

                String sqlInsert = "INSERT INTO pais(nombre,capital,region,poblacion,moneda) VALUES(?,?,?,?,?)";
                String sqlUpdate = "UPDATE pais SET capital=?, region=?, poblacion=?, moneda=? WHERE id=?";
                try (PreparedStatement psIns = con.prepareStatement(sqlInsert);
                     PreparedStatement psUpd = con.prepareStatement(sqlUpdate)) {
                    int hechos = 0;
                    for (Pais p : paises) {
                        Integer id = existentes.get(p.getNombre().toLowerCase());
                        if (id == null) {
                            psIns.setString(1, p.getNombre());
                            psIns.setString(2, p.getCapital());
                            psIns.setString(3, p.getRegion());
                            psIns.setLong(4, p.getPoblacion());
                            psIns.setString(5, p.getMoneda());
                            psIns.executeUpdate();
                            insertados++;
                        } else {
                            psUpd.setString(1, p.getCapital());
                            psUpd.setString(2, p.getRegion());
                            psUpd.setLong(3, p.getPoblacion());
                            psUpd.setString(4, p.getMoneda());
                            psUpd.setInt(5, id);
                            psUpd.executeUpdate();
                            actualizados++;
                        }
                        hechos++;
                        if (progreso != null) {
                            progreso.accept(hechos, paises.size());
                        }
                    }
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
        return new int[]{insertados, actualizados};
    }

    // ---------------------------------------------------------------- REPORTES (Actividad 5)
    public int contar() throws SQLException {
        try (Connection con = Conexion.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM pais")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public long poblacionTotal() throws SQLException {
        try (Connection con = Conexion.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(SUM(poblacion),0) FROM pais")) {
            return rs.next() ? rs.getLong(1) : 0L;
        }
    }

    /** Número de países por región, de mayor a menor. */
    public Map<String, Integer> contarPorRegion() throws SQLException {
        String sql = "SELECT region, COUNT(*) AS total FROM pais GROUP BY region ORDER BY total DESC, region";
        Map<String, Integer> resultado = new LinkedHashMap<>();
        try (Connection con = Conexion.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                resultado.put(rs.getString("region"), rs.getInt("total"));
            }
        }
        return resultado;
    }

    public Pais paisMasPoblado() throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM pais ORDER BY poblacion DESC LIMIT 1";
        try (Connection con = Conexion.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? mapear(rs) : null;
        }
    }
}
