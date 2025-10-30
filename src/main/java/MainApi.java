import static spark.Spark.*;

import com.google.gson.Gson;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class MainApi {

    private static final AtmService atmService = new AtmService();
    private static final Gson gson = new Gson();
    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    public static void main(String[] args) {
        // Allow overriding the port via system property or environment variable (PORT)
        String portProp = System.getProperty("PORT");
        if (portProp == null || portProp.trim().isEmpty()) {
            portProp = System.getenv("PORT");
        }
        int listenPort = 8080;
        if (portProp != null && !portProp.trim().isEmpty()) {
            try {
                listenPort = Integer.parseInt(portProp);
            } catch (NumberFormatException e) {
                System.err.println("Invalid PORT value '" + portProp + "', falling back to 8080");
            }
        }
        port(listenPort);

        // Serve static files from ./frontend/dist relative to the current working directory
        File distDir = new File(System.getProperty("user.dir"), "frontend/dist");
        if (!distDir.exists() || !distDir.isDirectory()) {
            System.err.println("Static folder not found at " + distDir.getAbsolutePath() + " - creating it.");
            boolean ok = distDir.mkdirs();
            if (!ok) {
                System.err.println("Failed to create static folder: " + distDir.getAbsolutePath());
            }
        }
        String staticDir = distDir.getAbsolutePath();
        staticFiles.externalLocation(staticDir);

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type,Authorization");
        });

        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });

        get("/api/atms", (req, res) -> {
            String pincode = req.queryParams("pincode");
            if (pincode == null) {
                pincode = "";
            }
            try {
                List<AtmLocation> list = atmService.findAtmsNearPincode(pincode);
                res.type("application/json");
                return gson.toJson(list);
            } catch (Exception ex) {
                // Log and return a JSON error so the frontend isn't given HTML
                System.err.println("Error in /api/atms: " + ex.getMessage());
                ex.printStackTrace();
                res.type("application/json");
                res.status(500);
                return gson.toJson(new SimpleStatus("error", ex.toString()));
            }
        });

        get("/api/test", (req, res) -> {
            res.type("application/json");
            System.out.println("Received request for /api/test");
            return "{\"status\":\"ok\"}";
        });

        post("/api/generate", (req, res) -> {
            System.out.println("Received request for /api/generate");
            res.type("application/json");

            String apiKey = System.getProperty("GEMINI_API_KEY", System.getenv("GEMINI_API_KEY"));
            if (apiKey == null || apiKey.trim().isEmpty()) {
                System.err.println("GEMINI_API_KEY is not set!");
                res.status(500);
                return "{\"error\":\"API key not configured. Please set GEMINI_API_KEY environment variable.\"}";
            }
            System.out.println("GEMINI_API_KEY found.");

            try {
                String bodyText = req.body();
                System.out.println("Request body: " + bodyText);

                JSONObject requestBody = new JSONObject(bodyText);
                String prompt = requestBody.getString("prompt");
                System.out.println("Extracted prompt: " + prompt);

                JSONObject contentPart = new JSONObject();
                contentPart.put("text", prompt);

                JSONArray contentsArray = new JSONArray();
                contentsArray.put(new JSONObject().put("parts", new JSONArray().put(contentPart)));

                JSONObject geminiRequestBody = new JSONObject();
                geminiRequestBody.put("contents", contentsArray);
                System.out.println("Constructed Gemini request body: " + geminiRequestBody);

                RequestBody requestBodyJson = RequestBody.create(geminiRequestBody.toString(), JSON_MEDIA_TYPE);

                Request request = new Request.Builder()
                        .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro-latest:generateContent")
                        .post(requestBodyJson)
                        .addHeader("X-goog-api-key", apiKey)
                        .addHeader("Content-Type", "application/json")
                        .build();

                System.out.println("Sending request to Gemini API...");
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.body() == null) {
                        System.err.println("Gemini API returned empty body");
                        res.status(502);
                        return "{\"error\":\"Empty response from Gemini API\"}";
                    }
                    String responseBody = response.body().string();
                    System.out.println("Received response from Gemini. Status: " + response.code());
                    if (!response.isSuccessful()) {
                        System.err.println("Gemini API error: " + responseBody);
                        res.status(response.code());
                        return responseBody;
                    }
                    System.out.println("Gemini response body: " + responseBody);
                    return responseBody;
                }
            } catch (Exception e) {
                System.err.println("Error in /api/generate endpoint:");
                e.printStackTrace();
                res.status(500);
                return "{\"error\":\"" + e.getMessage() + "\"}";
            }
        });

        post("/api/report", (req, res) -> {
            String body = req.body();
            AtmReportData data = gson.fromJson(body, AtmReportData.class);

            if (data == null || data.name == null || data.address == null) {
                res.status(400);
                return gson.toJson(new SimpleStatus("error", "missing name or address"));
            }

            atmService.submitReport(data.name, data.address, data.cashStatus, data.depositStatus, data.passbookStatus);
            res.type("application/json");
            return gson.toJson(new SimpleStatus("success", null));
        });
    }

    static class SimpleStatus {
        final String status;
        final String message;

        SimpleStatus(String status, String message) {
            this.status = status;
            this.message = message;
        }
    }
}
