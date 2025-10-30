import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Simple test runner that opens a connection using DatabaseConnector and
 * executes a small verification query against the `atm_db` database.
 */
public class TestDatabaseConnection {
    public static void main(String[] args) {
        System.out.println("Starting Database connection test...");

        Connection conn = DatabaseConnector.getConnection();
        if (conn == null) {
            System.err.println("Connection was null. See previous errors for details.");
            System.exit(2);
        }

        try (Statement stmt = conn.createStatement()) {
            // Simple verification: count rows in ATM_details
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS cnt FROM ATM_details")) {
                if (rs.next()) {
                    int cnt = rs.getInt("cnt");
                    System.out.println("ATM_details row count: " + cnt);
                }
            }

            // Show one sample row (if present)
            try (ResultSet rs = stmt.executeQuery("SELECT atm_id, bank_name, pincode FROM ATM_details LIMIT 1")) {
                if (rs.next()) {
                    System.out.println("Sample ATM row -> id:" + rs.getInt("atm_id") +
                            ", bank:" + rs.getString("bank_name") +
                            ", pincode:" + rs.getString("pincode")
                    );
                } else {
                    System.out.println("No rows found in ATM_details.");
                }
            }

        } catch (SQLException e) {
            System.err.println("SQL error while running verification queries:");
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException ignore) {}
        }

        System.out.println("Test finished.");
    }
}
