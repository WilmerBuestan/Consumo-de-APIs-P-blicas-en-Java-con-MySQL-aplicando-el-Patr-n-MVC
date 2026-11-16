package modelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Conexión a MySQL / MariaDB. Usuario, clave y URL se leen de config/db.properties.
 * Valores por defecto = los del taller (root / 12345 en localhost:3306).
 */
public class Conexion {

    private static final Properties CFG = Configuracion.cargar("db.properties");

    private static String url() {
        return CFG.getProperty("db.url", "jdbc:mysql://localhost:3306/api_paises");
    }

    private static String usuario() {
        return CFG.getProperty("db.user", "root");
    }

    private static String clave() {
        return CFG.getProperty("db.password", "12345");
    }

    /** Abre una conexión nueva a la base api_paises. Quien la pide debe cerrarla. */
    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(url(), usuario(), clave());
    }

    /** Crea la base de datos y la tabla pais si todavía no existen (script del taller). */
    public static void inicializar() throws SQLException {
        String url = url();
        String parametros = "";
        int q = url.indexOf('?');
        if (q >= 0) {
            parametros = url.substring(q);
            url = url.substring(0, q);
        }
        int barra = url.lastIndexOf('/');
        String servidor = url.substring(0, barra) + parametros;
        String baseDatos = url.substring(barra + 1);
        if (!baseDatos.matches("[A-Za-z0-9_]+")) {
            throw new SQLException("Nombre de base de datos no válido en db.url: " + baseDatos);
        }

        try (Connection con = DriverManager.getConnection(servidor, usuario(), clave());
             Statement st = con.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + baseDatos
                    + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            st.executeUpdate("USE " + baseDatos);
            st.executeUpdate("CREATE TABLE IF NOT EXISTS pais("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "nombre VARCHAR(100),"
                    + "capital VARCHAR(100),"
                    + "region VARCHAR(100),"
                    + "poblacion BIGINT,"
                    + "moneda VARCHAR(100)"
                    + ") DEFAULT CHARSET = utf8mb4");
        }
    }
}
