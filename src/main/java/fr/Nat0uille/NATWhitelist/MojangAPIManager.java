package fr.Nat0uille.NATWhitelist;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.UUID;

public class MojangAPIManager {

    private static final String MOJANG_API_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String MOJANG_SESSION_API_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final int TIMEOUT = 3000;

    public static String getCorrectUsernameFromMojang(String username) {
        try {
            URI uri = URI.create(MOJANG_API_URL + username);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);

            if (connection.getResponseCode() == 200) {
                JsonObject response = JsonParser.parseReader(
                    new InputStreamReader(connection.getInputStream())
                ).getAsJsonObject();
                return response.get("name").getAsString();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static UUID getUUIDFromUsername(String username) {
        try {
            URI uri = URI.create(MOJANG_API_URL + username);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);

            if (connection.getResponseCode() == 200) {
                JsonObject response = JsonParser.parseReader(
                    new InputStreamReader(connection.getInputStream())
                ).getAsJsonObject();
                String id = response.get("id").getAsString();
                // Convertir l'UUID sans tirets vers un UUID avec tirets
                return formatUUID(id);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static String getUsernameFromUUID(UUID uuid) {
        try {
            String uuidString = uuid.toString().replace("-", "");
            URI uri = URI.create(MOJANG_SESSION_API_URL + uuidString);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);

            if (connection.getResponseCode() == 200) {
                JsonObject response = JsonParser.parseReader(
                    new InputStreamReader(connection.getInputStream())
                ).getAsJsonObject();
                return response.get("name").getAsString();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static UUID formatUUID(String uuidString) {
        if (uuidString.length() != 32) {
            throw new IllegalArgumentException("Invalid UUID string");
        }
        String formatted = uuidString.substring(0, 8) + "-" +
                uuidString.substring(8, 12) + "-" +
                uuidString.substring(12, 16) + "-" +
                uuidString.substring(16, 20) + "-" +
                uuidString.substring(20, 32);
        return UUID.fromString(formatted);
    }
}

