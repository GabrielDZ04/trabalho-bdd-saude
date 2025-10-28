package bd.saude.consulta.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver MySQL não encontrado no classpath (lib/mysql-connector-j-*.jar).", e);
        }
    }

    private static final String URL  = getenvOr("DB_URL",
        "jdbc:mysql://localhost:3306/bdd_saude?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    private static final String USER = getenvOr("DB_USER", "root");
    private static final String PASS = getenvOr("DB_PASSWORD", "Gabriel_20!");

    private static String getenvOr(String key, String fallback) {
        String v = System.getenv(key);
        return (v != null && !v.isBlank()) ? v : fallback;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
