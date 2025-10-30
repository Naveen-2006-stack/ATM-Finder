import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
// currency formatting removed because amounts are hidden for safety

/**
 * Finds ATMs with sufficient cash for a requested withdrawal.
 *
 * The main method of interest is:
 *   public List<String> findAvailableAtms(String pincode, double withdrawalAmount)
 *
 * It uses DatabaseConnector.getConnection() to obtain a JDBC connection.
 */
public class AtmFinder {

    public List<String> findAvailableAtms(String pincode, double withdrawalAmount) {
        List<String> results = new ArrayList<>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

    // New schema uses ATM_reports with atm_name and atm_address (no numeric cash amounts).
    // We'll search by pincode substring match inside atm_address and prefer reports that indicate availability.
    String sql = "SELECT atm_name, atm_address, cash_status, deposit_status, passbook_status "
        + "FROM ATM_reports "
        + "WHERE LOWER(atm_address) LIKE ? "
        + "AND (LOWER(cash_status) LIKE '%available%' OR LOWER(cash_status) LIKE '%yes%')";

        try {
            conn = DatabaseConnector.getConnection();
            if (conn == null) {
                System.err.println("Could not obtain a database connection.");
                return results;
            }

            ps = conn.prepareStatement(sql);
            // Use pincode as substring match against the stored address
            ps.setString(1, "%" + pincode.toLowerCase() + "%");

            rs = ps.executeQuery();
            while (rs.next()) {
        String name = rs.getString("atm_name");
        String addr = rs.getString("atm_address");
        String cash = rs.getString("cash_status");
        String dep = rs.getString("deposit_status");
        String pass = rs.getString("passbook_status");

        String cashStr = (cash != null && !cash.isBlank()) ? cash : "Unknown";
        String depositStr = (dep != null && (dep.equalsIgnoreCase("yes") || dep.equalsIgnoreCase("available"))) ? "Yes" : "No";
        String passbookStr = (pass != null && (pass.equalsIgnoreCase("yes") || pass.equalsIgnoreCase("available"))) ? "Yes" : "No";

        String entry = name + " - " + addr + "\n\t> Cash: " + cashStr
            + " | Deposit: " + depositStr + " | Passbook: " + passbookStr;

                results.add(entry);
            }

        } catch (SQLException e) {
            System.err.println("SQL error while finding ATMs:");
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException ignore) {}
            }
            if (ps != null) {
                try { ps.close(); } catch (SQLException ignore) {}
            }
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignore) {}
            }
        }

        return results;
    }

    // Small convenience main to demonstrate usage (optional).
    public static void main(String[] args) {
        String pincode = args.length > 0 ? args[0] : "600001";
        double amount = args.length > 1 ? Double.parseDouble(args[1]) : 1000.0;

        AtmFinder finder = new AtmFinder();
        List<String> atms = finder.findAvailableAtms(pincode, amount);

        if (atms.isEmpty()) {
            System.out.println("No ATMs found for pincode=" + pincode + " with amount=" + amount);
        } else {
            atms.forEach(System.out::println);
        }
    }
}
