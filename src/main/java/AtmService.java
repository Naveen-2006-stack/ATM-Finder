import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to find ATMs using the Gemini API and to read/submit reports to local DB.
 *
 * Note: This class requires OkHttp and org.json on the classpath.
 */
public class AtmService {

    // Read API key from environment variable to avoid committing secrets.
    private static final String API_KEY = System.getProperty("GEMINI_API_KEY", System.getenv("GEMINI_API_KEY"));

    private final OkHttpClient http = new OkHttpClient.Builder()
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro-latest:generateContent";
    
    /**
     * Find ATMs near the given pincode by asking Gemini to list ATMs in the requested prompt format.
     */
    public List<AtmLocation> findAtmsNearPincode(String pincode) {
        List<AtmLocation> results = new ArrayList<>();
        try {
            String prompt = "Please find ATMs near pincode " + pincode + ". Provide a list where each ATM is on a new line, formatted exactly as: Name :: Address";

            JSONObject contentPart = new JSONObject();
            contentPart.put("text", prompt);

            JSONArray contentsArray = new JSONArray();
            contentsArray.put(new JSONObject().put("parts", new JSONArray().put(contentPart)));

            JSONObject geminiRequestBody = new JSONObject();
            geminiRequestBody.put("contents", contentsArray);

            RequestBody body = RequestBody.create(geminiRequestBody.toString(), JSON);
            Request.Builder requestBuilder = new Request.Builder().url(GEMINI_URL).post(body);
            if (API_KEY != null && !API_KEY.isBlank()) {
                requestBuilder.addHeader("X-goog-api-key", API_KEY);
            } else {
                System.err.println("Warning: GEMINI_API_KEY env var not set. Gemini calls will likely fail unless you provide credentials by another method.");
            }
            Request request = requestBuilder.build();

            try (Response resp = http.newCall(request).execute()) {
                if (!resp.isSuccessful()) {
                    System.err.println("Gemini HTTP error: " + resp.code());
                    return results;
                }
                String respBody = resp.body() != null ? resp.body().string() : null;
                if (respBody == null) return results;

                String generated = parseGeminiGeneratedText(respBody);
                if (generated == null || generated.isBlank()) return results;

                String[] lines = generated.split("\\r?\\n");
                for (String line : lines) {
                    String[] parts = line.split("::", 2);
                    if (parts.length < 2) continue;
                    String name = parts[0].trim();
                    String address = parts[1].trim();
                    AtmReport latest = getLatestReport(name, address);
                    results.add(new AtmLocation(name, address, latest));
                }
            }

        } catch (IOException e) {
            System.err.println("IO error calling Gemini: " + e.getMessage());
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Parse a likely Gemini response to extract the generated text.
     * The Gemini v1beta response commonly includes a 'candidates' array with content objects.
     */
    private String parseGeminiGeneratedText(String respBody) {
        JSONObject root = new JSONObject(respBody);
        JSONArray candidates = root.optJSONArray("candidates");
        if (candidates != null && candidates.length() > 0) {
            JSONObject first = candidates.getJSONObject(0);
            JSONArray content = first.optJSONArray("content");
            if (content != null) {
                for (int i = 0; i < content.length(); i++) {
                    JSONObject c = content.getJSONObject(i);
                    if (c.has("text")) return c.getString("text");
                    if (c.has("type") && "output_text".equals(c.optString("type")) && c.has("text")) return c.getString("text");
                }
            }
            // try common field
            if (first.has("output")) return first.optString("output");
        }
        // fallback to top-level text fields
        if (root.has("output_text")) return root.optString("output_text");
        return root.optString("text", null);
    }

    /**
     * Query the local DB for the latest report matching name+address.
     */
    public AtmReport getLatestReport(String name, String address) {
        String sql = "SELECT cash_status, deposit_status, passbook_status, report_timestamp FROM ATM_reports "
                + "WHERE atm_name = ? AND atm_address = ? ORDER BY report_timestamp DESC LIMIT 1";
        try (Connection conn = DatabaseConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return null;
            ps.setString(1, name);
            ps.setString(2, address);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String cash = rs.getString("cash_status");
                    String dep = rs.getString("deposit_status");
                    String pass = rs.getString("passbook_status");
                    Timestamp ts = rs.getTimestamp("report_timestamp");
                    return new AtmReport(cash, dep, pass, ts);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL error in getLatestReport: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Submit a report into the local DB.
     */
    public void submitReport(String name, String address, String cashStatus, String depositStatus, String passbookStatus) {
        String sql = "INSERT INTO ATM_reports (atm_name, atm_address, cash_status, deposit_status, passbook_status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) throw new SQLException("No DB connection");
            ps.setString(1, name);
            ps.setString(2, address);
            ps.setString(3, cashStatus);
            ps.setString(4, depositStatus);
            ps.setString(5, passbookStatus);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("SQL error in submitReport: " + e.getMessage());
            e.printStackTrace();
        }
    }

}





