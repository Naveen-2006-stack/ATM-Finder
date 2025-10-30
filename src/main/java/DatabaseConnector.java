import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class to provide a JDBC Connection to the local MySQL database `atm_db`.
 *
 * Usage:
 *   Connection conn = DatabaseConnector.getConnection();
 *   if (conn != null) { ... }
 */
public class DatabaseConnector {

    /**
     * Return a java.sql.Connection to the configured MySQL database.
     *
     * Connection parameters are read from environment variables with sensible
     * defaults so you can override them without changing source code:
     *
     * - MYSQL_URL (default: jdbc:mysql://localhost:3007/atm_db)
     * - MYSQL_USER (default: admin)
     * - MYSQL_PASSWORD (default: admin)
     *
     * On failure this method prints a helpful message and returns null.
     */
    public static Connection getConnection() {
    // Default to the MySQL instance you ran earlier on port 3307
    String defaultUrl = "jdbc:mysql://localhost:3307/atm_db";
        String url = System.getenv().getOrDefault("MYSQL_URL", defaultUrl);
        String user = System.getenv().getOrDefault("MYSQL_USER", "admin");
        String password = System.getenv().getOrDefault("MYSQL_PASSWORD", "admin");

        try {
            // The driver class is no longer strictly required to be loaded manually
            // with newer JDBC drivers, but calling Class.forName keeps compatibility.
            Class.forName("com.mysql.cj.jdbc.Driver");

            return DriverManager.getConnection(url, user, password);

        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Add mysql-connector-java to the classpath or your Maven dependencies.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Failed to establish a database connection to " + url + " with user '" + user + "'.");
            e.printStackTrace();
        }

        return null;
    }
}
