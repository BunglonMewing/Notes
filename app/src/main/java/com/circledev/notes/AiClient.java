package com.circledev.notes;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class AiClient {

    private static final String API = "https://zoyanz-api.vercel.app/ai/copilot";
    private static final String FALLBACK = "Koneksi bermasalah 😅 Coba lagi ya!";

    private AiClient() {
    }

    public static String ask(String message) {
        HttpURLConnection conn = null;
        try {
            String url = API + "?message=" + URLEncoder.encode(message, "UTF-8") + "&model=default";
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(60000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int code = conn.getResponseCode();
            InputStream in = code >= 200 && code < 300
                    ? conn.getInputStream() : conn.getErrorStream();
            if (in == null) return FALLBACK;
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
            }
            if (code < 200 || code >= 300) return FALLBACK;
            String raw = sb.toString().trim();
            if (raw.isEmpty()) return "Maaf, tidak ada respons.";

            JSONObject root = new JSONObject(raw);
            Object result = root.opt("result");
            if (result instanceof JSONObject) {
                String text = ((JSONObject) result).optString("text", null);
                if (text != null && !text.isEmpty()) return text;
                return result.toString();
            }
            if (result instanceof String && !((String) result).isEmpty()) {
                return (String) result;
            }
            String text = root.optString("text", null);
            if (text != null && !text.isEmpty()) return text;
            return raw;
        } catch (Exception e) {
            return FALLBACK;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
