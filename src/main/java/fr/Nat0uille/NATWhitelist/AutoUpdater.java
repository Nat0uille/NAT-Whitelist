package fr.Nat0uille.NATWhitelist;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class AutoUpdater {

    private static final String MODRINTH_API = "https://api.modrinth.com/v2/project/nat-whitelist/version";

    private final Main plugin;

    public AutoUpdater(Main plugin) {
        this.plugin = plugin;
    }

    public void downloadUpdate() {
        try {
            String downloadUrl = fetchLatestDownloadUrl();
            if (downloadUrl == null) {
                plugin.getLogger().warning("[AutoUpdate] Could not find a download URL on Modrinth.");
                return;
            }

            File pluginFile = plugin.getPluginFile();
            File tempFile = new File(pluginFile.getParentFile(), pluginFile.getName() + ".tmp");

            HttpURLConnection connection = (HttpURLConnection) new URL(downloadUrl).openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("User-Agent", "NAT-Whitelist/" + plugin.getDescription().getVersion());

            try (InputStream in = connection.getInputStream()) {
                Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            Files.move(tempFile.toPath(), pluginFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            plugin.getLogger().info("[AutoUpdate] Plugin updated! Restart the server to apply it.");

        } catch (Exception e) {
            plugin.getLogger().severe("[AutoUpdate] Failed to download update: " + e.getMessage());
        }
    }

    private String fetchLatestDownloadUrl() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(MODRINTH_API).openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("User-Agent", "NAT-Whitelist/" + plugin.getDescription().getVersion());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            JsonArray versions = JsonParser.parseString(sb.toString()).getAsJsonArray();
            if (versions.isEmpty()) return null;

            JsonArray files = versions.get(0).getAsJsonObject().getAsJsonArray("files");
            for (int i = 0; i < files.size(); i++) {
                JsonObject file = files.get(i).getAsJsonObject();
                if (file.get("primary").getAsBoolean()) {
                    return file.get("url").getAsString();
                }
            }

            return files.isEmpty() ? null : files.get(0).getAsJsonObject().get("url").getAsString();
        }
    }
}
